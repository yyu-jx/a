package com.superyoudi.aiservice.ai.agent;

import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.superyoudi.aiservice.ai.agent.interceptor.AiTutorModelInterceptor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * @author Sijayyx
 */
@Component
public class AiTutorAgent {

    private final ReactAgent agent;

    public AiTutorAgent(AiTutorModelInterceptor aiTutorModelInterceptor) {
        this.agent = ReactAgent.builder()
                .name("ai-tutor")
                .saver(new MemorySaver()) // TODO
                .interceptors(aiTutorModelInterceptor) // 提示词在这里设置
                .tools()// TODO
                .build();
    }

    public Flux<NodeOutput> callAgent(String userInput, RunnableConfig config) throws GraphRunnerException {
        // 设置config的metadata以进行context engineering
        return agent.stream(userInput, config);
    }


}
