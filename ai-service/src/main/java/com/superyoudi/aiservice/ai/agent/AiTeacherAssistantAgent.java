package com.superyoudi.aiservice.ai.agent;

import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.superyoudi.aiservice.ai.agent.tools.AiTeacherAssistantTools;
import com.superyoudi.aiservice.ai.tools.RagTools;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * @author Sijayyx
 */
@Component
public class AiTeacherAssistantAgent {

    // TODO 聊天记录

    private final ReactAgent agent;

    public AiTeacherAssistantAgent(AiTeacherAssistantTools teacherAssistantTools, ChatModel chatModel, RagTools ragTools) {

        this.agent = ReactAgent.builder()
                .name("ai-tutor")
                .model(chatModel)
                .systemPrompt("""
                        你是一位AI智教助手，专为教师设计，定位是教师的智能协作者。
                        
                        ## 核心原则
                        1. 你是教师的助手，而非替代者——所有决策最终由教师确认
                        2. 理解教师的自然语言指令，准确识别意图并执行对应操作
                        3. 提供专业、准确、可操作的教育建议
                        
                        ## 能力范围
                        1. 课件/教案生成
                        2. 任务布置与作业批改
                        
                        ## 规范
                        - 当意图明确时，直接执行并返回结果
                        - 当意图模糊时，主动向教师追问澄清
                        - 每次回复末尾，可以提供1-2个相关的追问建议
                        """)
                .methodTools(teacherAssistantTools, ragTools)
                .build();
    }

    /**
     * 调用AI智教
     *
     * @param input  用户输入
     * @param config 需要 user_id 的 metadata
     * @return 流式响应
     */
    public Flux<NodeOutput> call(String input, RunnableConfig config) throws GraphRunnerException {
        return agent.stream(input, config);
    }

}
