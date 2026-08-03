package com.superyoudi.coursewareservice.service;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.InterruptionMetadata;
import com.alibaba.cloud.ai.graph.streaming.OutputType;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.superyoudi.aiservice.pojo.dto.UserChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.concurrent.ForkJoinPool;

import static com.superyoudi.coursewareservice.config.GraphConfig.*;

/**
 * @author Sijayyx
 */
@Slf4j
@Service
public class CoursewareInteractionService {


    private final CompiledGraph compiledGraph;

    public CoursewareInteractionService(CompiledGraph compiledGraph) {
        this.compiledGraph = compiledGraph;
    }

    /**
     * 将用户输入传入工作流图中，并返回结果
     *
     * @param userChatMessage 聊天信息
     * @return 用户流式输出
     * @author Sijayyx
     */
    public Flux<String> executeWorkflowGraph(UserChatMessage userChatMessage) {
        Integer userId = userChatMessage.getUserId();
        String input = userChatMessage.getMessage();

        Long coursewareId = (Long) userChatMessage.getAdditionalData();

        // 构建线程ID
        String threadId = constructThreadId(userId);

        // 流式输出
        // 创建并行节点的图配置
        RunnableConfig config = RunnableConfig.builder()
                .threadId(threadId)
                .addParallelNodeExecutor(NODE_WORD_GENERATION, ForkJoinPool.commonPool())
                .addParallelNodeExecutor(NODE_PPT_GENERATION, ForkJoinPool.commonPool())
                .build();

        return compiledGraph.stream(Map.of(
                        KEY_USER_ID, userId,
                        KEY_INPUT, input,
                        KEY_COURSEWARE_ID, coursewareId
                ), config)
                .map(output -> handleStreamingOutput(output, threadId));
    }

    public Flux<String> resumeWorkflowGraph(UserChatMessage userChatMessage) {
        Integer userId = userChatMessage.getUserId();
        String resumeStatus = "APPROVE"; // APPROVE | EDIT | REJECT
        String resumeUserFeedback = "修改状态下的修改意见。"; // TODO 解析这个
//        try {
//            resumeStatus = mapper.writeValueAsString(input);
//        } catch (JsonProcessingException e) {
//            log.error("UserChatMessage JSON转换错误: {}", e.getMessage());
//        }

        // 构建线程ID
        String threadId = constructThreadId(userId);

        RunnableConfig resumeConfig = RunnableConfig.builder()
                .threadId(threadId)
                .addMetadata(RunnableConfig.HUMAN_FEEDBACK_METADATA_KEY, resumeStatus)
                .addMetadata(KEY_HITL_USER_FEEDBACK, resumeUserFeedback)
                .addParallelNodeExecutor(NODE_WORD_GENERATION, ForkJoinPool.commonPool())
                .addParallelNodeExecutor(NODE_PPT_GENERATION, ForkJoinPool.commonPool())
                .build();

        return compiledGraph.stream(null, resumeConfig)
                .map(output -> handleStreamingOutput(output, threadId));
    }

    public String handleStreamingOutput(NodeOutput output, String threadId) {

        // 处理HITL中断
        if (output instanceof InterruptionMetadata interruptionMetadata) {
            String nodeName = interruptionMetadata.node();
            String outline = interruptionMetadata.metadata(KEY_PPT_OUTLINE).get().toString();

            log.error("节点执行中断！{}, outline: {}", nodeName, outline);

            return String.format("""
                    节点%s已中断。
                    PPT大纲:
                    %s
                    
                    [DONE]
                    """, nodeName, outline);

        }
        // 处理流式输出
        else if (output instanceof StreamingOutput<?> streamingOutput) {
            OutputType outputType = streamingOutput.getOutputType();
            Message message = streamingOutput.message();

            // 判断节点输出类型

            if (outputType == OutputType.GRAPH_NODE_STREAMING && message != null) {
                String text = message.getText();
                if (text != null)
                    return formatOutput(text);
            } else if (outputType == OutputType.GRAPH_NODE_FINISHED && message != null) {
                return formatOutput("[DONE]"); // 输出结束
            }
        }
        return formatOutput("");
    }

    private String formatOutput(String output) {
        return output;
    }

    private String constructThreadId(Integer userId) {
        return "user_graph_thread_" + userId;
    }

}
