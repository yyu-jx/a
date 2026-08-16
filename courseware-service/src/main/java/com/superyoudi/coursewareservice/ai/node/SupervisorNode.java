package com.superyoudi.coursewareservice.ai.node;

import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.superyoudi.aiservice.pojo.entity.ChatMessage;
import com.superyoudi.aiservice.utils.AiMessageUtils;
import com.superyoudi.common.constants.CoursewareGenConstants;
import com.superyoudi.coursewareservice.ai.hook.SupervisorMessageAgentHook;
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


/**
 * 监督者节点，调度下游的所有节点，负责和用户对话
 *
 * @author Sijayyx
 */
@Slf4j
public class SupervisorNode implements NodeAction {

    private final ReactAgent supervisorAgent;
    private final ChatClient chatClient;
    private final SupervisorMessageAgentHook supervisorMessageAgentHook;

    public SupervisorNode(ChatModel chatModel, ChatClient.Builder chatClient, SupervisorMessageAgentHook supervisorMessageAgentHook, RagTools ragTools) {

        this.supervisorAgent = ReactAgent.builder()
                .name("supervisor")
                .model(chatModel)
                .hooks(supervisorMessageAgentHook)
                .methodTools(ragTools)
                .build();

        this.chatClient = chatClient.build();
        this.supervisorMessageAgentHook = supervisorMessageAgentHook;
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
        Long userId = state.value(CoursewareGenConstants.KEY_USER_ID, Long.class).orElseThrow();
        Long coursewareRecId = state.value(CoursewareGenConstants.KEY_COURSEWARE_REC_ID, Long.class).orElseThrow();

        String input = state.value(CoursewareGenConstants.KEY_INPUT, "用户无输入");
        String additionalData = state.value(CoursewareGenConstants.KEY_USER_ADDITIONAL_DATA, "无附加数据");
        List<String> agentsInfo = state.value(CoursewareGenConstants.KEY_AGENTS_INFO, List.of("节点无数据"));

        log.info("SupervisorNode开始执行:userId: {}, input: {}, additionalData: {}, agentsInfo: {}",
                userId, input, additionalData, agentsInfo);

        // 线程隔离
        RunnableConfig config = RunnableConfig.builder()
                .threadId("user_thread_" + userId + "_" + coursewareRecId)
                .addMetadata(CoursewareGenConstants.KEY_COURSEWARE_REC_ID, coursewareRecId)
                .addMetadata(CoursewareGenConstants.KEY_USER_ID, userId)
                .build();

        Flux<NodeOutput> sharedStream = supervisorAgent.stream(input, config).share();

        // 提取纯文本 String 的共享流
        Flux<String> contentOnlyStream = sharedStream
                .map(output -> AiMessageUtils.handleStreamingOutput(
                        output,
                        null,
                        allChunk -> {
                            // 流式输出结束的回调
                            log.info("SupervisorNode响应：{}", allChunk);

                            // 判断是否可以生成PPT
                            String readyToGenerate = predicateAbleToGenerate(allChunk);
                            state.updateState(Map.of(CoursewareGenConstants.KEY_SHOULD_GENERATE, readyToGenerate));

                            if (readyToGenerate.equals("TRUE")) {
                                String json = generatePptJson(userId, coursewareRecId);
                                state.updateState(Map.of(CoursewareGenConstants.KEY_GENERATION_INFO, json));
                            }

                            log.info("SupervisorNode是否生成资料：{}", readyToGenerate);
                        }
                        , null))
                .filter(msg -> !msg.isBlank() && !msg.equals("[DONE]"))
                .share();

        // 流式转换为ChatResponse供前端解析
        Flux<ChatResponse> responseFlux = contentOnlyStream
                .map(content -> new ChatResponse(List.of(
                        new Generation(new AssistantMessage(content))
                )));

        return Map.of(CoursewareGenConstants.KEY_RESPONSE, responseFlux);
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
    private String generatePptJson(long userId, long coursewareRecId) {


        String systemPrompt = """
                你是一位总结专家，可以根据对话历史总结需要生成的资料的各个要点，以及需要生成哪些资料，并以严格的Json格式输出。
                
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
                
                对话历史: %s
                用户ID: %d
                """;

        return chatClient.prompt(String.format(systemPrompt, userId, getFormattedChatHistory(userId, coursewareRecId)))
                .call()
                .content();
    }

    public String getFormattedChatHistory(long userId, long coursewareRecId) {
        List<ChatMessage> chatHistory = supervisorMessageAgentHook.getChatHistory(userId, coursewareRecId);
        return AiMessageUtils.formatChatHistory(chatHistory);
    }

}
