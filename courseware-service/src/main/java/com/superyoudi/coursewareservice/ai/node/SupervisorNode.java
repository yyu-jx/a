package com.superyoudi.coursewareservice.ai.node;

import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.streaming.OutputType;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.superyoudi.coursewareservice.ai.tool.RagTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.PromptTemplate;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

import static com.superyoudi.coursewareservice.config.GraphConfig.*;


/**
 * 监督者节点，调度下游的所有节点，负责和用户对话
 *
 * @author Sijayyx
 */
@Slf4j
public class SupervisorNode implements NodeAction {

    private final ReactAgent supervisorAgent;
    private final ChatClient chatClient;

    public SupervisorNode(ChatModel chatModel, ChatClient.Builder chatClient, RagTools ragTools) {

        this.supervisorAgent = ReactAgent.builder()
                .name("supervisor")
                .model(chatModel)
                .methodTools(ragTools)
                .saver(new MemorySaver())
                .build();

        this.chatClient = chatClient.build();
    }

    /// ## 监督者节点执行逻辑
    ///
    /// 监督者负责维护三个状态：`response`, `should_generate`和`generation_info`。
    ///
    /// 主监督者负责与用户交流。为保证用户体验，必须进行流式输出。这使得对另外两个状态的维护变得十分困难。
    /// 我尝试让它生成结构化数据，但是解析器会变得特别复杂且不稳定。
    ///
    /// 所以，我加入了另外两个AI调用，一个使用不带记忆的ChatClient，负责判断主监督者的输出，维护`should_generate`的状态。
    ///
    /// 另一个用相同的`supervisorAgent`，但是重新设计了指令和提示词，需要用到它的记忆功能来总结用户的生成需求。
    /// 主监督者的两次调用都会重新设置指令，保证运行的顺利。它维护`generation_info`的状态。
    ///
    /// 主监督者在流式返回信息时，通过`OutputType`判断信息状态。
    /// 当处于流式返回时，信息会被包装成`ChatResponse`返回给图。当调用完时，所有的响应会一次性输出。此时会先调用`ChatClient`，
    /// 让它总结判断接下来是否会调度进生成节点。如果是，则会调用`supervisorAgent`，从它的记忆力总结信息，并将两个状态设置进`OverAllState`，
    /// 使下游节点获取。
    ///
    /// 实测下来，这套配置还是挺准确的。缺点在于会增加Token的消耗，且可能会撞到记忆的上下文窗口。我不知道还有什么更好的方法。
    ///
    /// @author Sijayyx
    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        // 获取状态
        Integer userId = state.value(KEY_USER_ID, -1);
        String input = state.value(KEY_INPUT, "用户无输入");
        String additionalData = state.value(KEY_USER_ADDITIONAL_DATA, "无附加数据");
        List<String> agentsInfo = state.value(KEY_AGENTS_INFO, List.of("节点无数据"));

        log.info("SupervisorNode开始执行:userId: {}, input: {}, additionalData: {}, agentsInfo: {}",
                userId, input, additionalData, agentsInfo);


        supervisorAgent.setInstruction("""
                你是一个监督者，可以分析用户意图并给出对应的响应。
                
                当你处于测试模式时，你应该严格遵守指令。
                当用户是在与你聊天或没有生成教学资料（PPT课件和/或教案）的意图，你应该回应用户输入。
                
                当用户要求生成教学资料时，你需要结合用户输入，判断你是否明确了这些要点：
                课程主题;
                教学信息：如授课对象，年级，课程时长;
                教学目标：应该掌握的具体知识点;
                核心知识点：最核心的1-3个知识点;
                重难点：需要明确讲授或强调的内容;
                教学流程：较详细的课堂教学安排;
                互动设计: 可选，课堂安排中的互动设计，比如提问与小组讨论等;
                
                如果有任何一个要点没有明确，你需要引导式地向用户提问，以明确这些要点。
                除了向用户询问时，否则关于任何要点的详细内容，你只能总结用户的输入，而不能对其进行推断、举例或分析。
                
                如果明确了所有要点，你需要总结以上要点，并且向用户确认.
                当用户确认完毕后，你需要总结这些要点。
                
                当用户要求修改PPT或者教案时，如果用户的要求不明确，你需要对不明确的地方发起引导式的提问。
                
                你可以调用相关工具获取用户上传的参考资料，以做出相关回答。
                不要告诉用户你的内部执行逻辑，不要自行生成PPT或教案及其大纲。
                你可以向下游节点发送消息以执行PPT和教案的生成任务。
                """
        );

        // 构造提示词
        String prompt = String.format("""
                现在你需要与用户对话，你的语气必须是礼貌的，和善的。
                只需要响应用户输入。
                
                用户ID: %d
                以下是用户的输入: %s
                以下是用户的附加信息: %s
                
                若用户未输入，请根据节点的状态数据告诉用户是否成功完成了任务。
                以下是节点的数据: %s
                """, userId, input, additionalData, agentsInfo);

        // 通过运行配置启用线程隔离，使AI的记忆可以区分用户
        RunnableConfig config = RunnableConfig.builder()
                .threadId("user_thread_" + userId)
                .build();

        Flux<NodeOutput> sharedStream = supervisorAgent.stream(prompt, config).share();

        // 提取纯文本 String 的共享流
        Flux<String> contentOnlyStream = sharedStream
                .filter(output -> output instanceof StreamingOutput<?>)
                .map(output -> {
                            StreamingOutput<?> streamingOutput = (StreamingOutput<?>) output;
                            // 流式传输
                            if (streamingOutput.getOutputType() == OutputType.AGENT_MODEL_STREAMING) {
                                // chunk 就是内容
                                Object chunk = streamingOutput.chunk();
                                return chunk != null ? chunk.toString() : "";
                            }
                            // 传输完成，返回最终输出
                            else if (streamingOutput.getOutputType() == OutputType.AGENT_MODEL_FINISHED) {
                                // 获取最终输出
                                Object allChunk = streamingOutput.chunk();
                                log.info("SupervisorNode响应：{}", allChunk);

                                // 判断是否可以生成PPT，如果可以，生成PPT的Json传给下游节点。
                                if (allChunk != null) {
                                    String readyToGenerate = predicateAbleToGenerate(allChunk.toString());
                                    state.updateState(Map.of(KEY_SHOULD_GENERATE, readyToGenerate));

                                    if (readyToGenerate.equals("TRUE")) {
                                        String json = generatePptJson(config);
                                        state.updateState(Map.of(KEY_GENERATION_INFO, json));
                                    }

                                    log.info("SupervisorNode是否生成资料：{}", readyToGenerate);
                                }
                            }
                            return "";
                        }
                )
                .filter(c -> !c.isBlank())
                .share(); // 再次共享

        // 流式转换为ChatResponse供前端解析
        Flux<ChatResponse> responseFlux = contentOnlyStream
                .map(content -> new ChatResponse(List.of(
                        new Generation(new AssistantMessage(content))
                )));

        return Map.of(KEY_RESPONSE, responseFlux);
    }

    /**
     * 使用AI判断是否可以生成PPT, 如果可以生成，则返回TRUE，否则返回FALSE
     *
     * @param input 负责AI对话的上一次输出
     * @return TRUE | FALSE
     * @author Sijayyx
     */
    private String predicateAbleToGenerate(String input) {
        PromptTemplate promptTemplate = new PromptTemplate(
                """
                        只根据给出的话语，判断其接下来是否会生成或修改PPT或者教案。
                        只有在表明"接下来会生成或修改"的意思时，条件才满足。如果是在向用户提问等，则不满足条件。
                        如果是，请返回TRUE，否则返回FALSE。不要返回其它内容。
                        这是给出的话语：
                        {response}
                        """
        );

        String prompt = promptTemplate.render(Map.of("response", input));
        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }

    /**
     * 使用AI总结并生成PPT的Json
     *
     * @return Json
     * @author Sijayyx
     */
    private String generatePptJson(RunnableConfig config) {
        String instr = """
                你是一位总结专家，可以总结需要生成的资料的各个要点，以及需要生成哪些资料，并以Json格式输出。
                
                Json文本中包含以下几项:
                userId为用户ID，必填
                wordAction为对教案执行的动作，generate表示新生成一份教案，edit表示对已有教案进行修改，other表示其它操作，none表示不执行任何操作。
                wordActionInfo为对教案执行动作的信息与要求，若无任何动作，则设置为null。
                pptAction为对PPT执行的动作，generate表示新生成一个PPT，edit表示对已有PPT进行修改，other表示其它操作，none表示不执行任何操作。
                pptActionInfo为对PPT执行动作的信息与要求，若无任何动作，则设置为null。
                title为课程标题
                basicInfo为教学信息：如授课对象，年级，课程时长;
                teachingTarget为教学目标：应该掌握的具体知识点;
                coreKnowledges为核心知识点：最核心的1-3个知识点;
                hardKnowlegdes为重难点：需要明确讲授或强调的内容;
                teachingFlow为教学流程：较详细的课堂教学安排;
                interactionDesign为互动设计: 可选，课堂安排中的互动设计，比如提问与小组讨论等;
                additionalInfo为额外信息
                
                当只需要生成教案时，wordAction和wordActionInfo应设置为none和null
                当只需要生成PPT时，pptAction和pptActionInfo应设置为none和null
                只需要返回Json的内容，不要返回其它数据。
                """;
        // 重置指令
        supervisorAgent.setInstruction(instr);
        try {
            String result = supervisorAgent.call("请根据你的记忆进行总结。", config).getText();

            log.info("SupervisorNode总结：{}", result);
            return result;
        } catch (GraphRunnerException e) {
            return "错误";
        }

    }
}
