package com.superyoudi.aiservice.ai.agent;

import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.superyoudi.aiservice.ai.agent.hook.AiTutorMessageAgentHook;
import com.superyoudi.aiservice.ai.agent.interceptor.AiTutorModelInterceptor;
import com.superyoudi.aiservice.ai.agent.tools.AiTutorTools;
import com.superyoudi.aiservice.ai.tools.RagTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * @author Sijayyx
 */
@Slf4j
@Component
public class AiTutorAgent {

    private final ReactAgent agent;

    public AiTutorAgent(AiTutorModelInterceptor aiTutorModelInterceptor,
                        AiTutorMessageAgentHook aiTutorMessageAgentHook,
                        AiTutorTools aiTutorTools, ChatModel chatModel, RagTools ragTools) {

        this.agent = ReactAgent.builder()
                .name("ai-tutor")
                .model(chatModel)
                .interceptors(aiTutorModelInterceptor) // 提示词在这里设置
                .hooks(aiTutorMessageAgentHook)
                .methodTools(ragTools, aiTutorTools)
                .build();
    }

    /**
     * 调用AI导师
     *
     * @param userInput 用户输入
     * @param config    运行配置，它的Metadata需要 student_id, course_id, custom_metadata 三个值。
     * @return 节点流式输出
     */
    public Flux<NodeOutput> callAgent(String userInput, RunnableConfig config) throws GraphRunnerException {
        return agent.stream(userInput, config);
    }

}
