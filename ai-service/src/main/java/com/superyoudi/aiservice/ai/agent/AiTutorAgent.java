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

    public static final String METADATA_STUDENT_ID = "student_id";
    public static final String METADATA_COURSE_ID = "course_id";

    /// 自定义Metadata
    public static final String METADATA_CUSTOM_METADATA = "custom_metadata";

    private final ReactAgent agent;

    public AiTutorAgent(AiTutorModelInterceptor aiTutorModelInterceptor,
                        AiTutorMessageAgentHook aiTutorMessageAgentHook,
                        AiTutorTools aiTutorTools, ChatModel chatModel, RagTools ragTools) {

        this.agent = ReactAgent.builder()
                .name("ai-tutor")
                .model(chatModel)
//                .saver(new MemorySaver())
//                .interceptors(aiTutorModelInterceptor) // 提示词在这里设置
                .hooks(aiTutorMessageAgentHook)
                .methodTools(ragTools)
                .build();
    }

    /**
     * 调用AI导师
     *
     * @param userInput 用户输入
     * @param config    运行配置，它的Metadata需要student_id和course_id两个值。
     * @return 节点六十输出
     */
    public Flux<NodeOutput> callAgent(String userInput, RunnableConfig config) throws GraphRunnerException {
        return agent.stream(userInput, config);
    }


}
