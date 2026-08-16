package com.superyoudi.coursewareservice.service;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.superyoudi.aiservice.pojo.dto.UserChatMessage;
import com.superyoudi.aiservice.pojo.entity.ChatMessage;
import com.superyoudi.aiservice.pojo.entity.ChatSession;
import com.superyoudi.aiservice.service.ChatPersistenceService;
import com.superyoudi.aiservice.utils.AiMessageUtils;
import com.superyoudi.common.constants.CoursewareGenConstants;
import com.superyoudi.common.dto.ChatHistoryDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;

/**
 * @author Sijayyx
 */
@Slf4j
@Service
public class CoursewareInteractionService {

    private final CompiledGraph compiledGraph;
    private final ChatPersistenceService chatPersistenceService;

    public CoursewareInteractionService(CompiledGraph compiledGraph, ChatPersistenceService chatPersistenceService) {
        this.compiledGraph = compiledGraph;
        this.chatPersistenceService = chatPersistenceService;
    }

    /**
     * 将用户输入传入工作流图中，并返回结果
     *
     * @param userChatMessage 聊天信息
     * @return 用户流式输出
     * @author Sijayyx
     */
    public Flux<String> executeWorkflowGraph(UserChatMessage userChatMessage) {
        Long userId = userChatMessage.getUserId();
        String input = userChatMessage.getMessage();
        Long coursewareRecId = (Long) userChatMessage.getAdditionalData();

        Assert.notNull(userId, "userId不能为null");
        Assert.notNull(coursewareRecId, "coursewareId不能为null");

        // 构建线程ID
        String threadId = constructThreadId(userId);

        // 流式输出
        // 创建并行节点的图配置
        RunnableConfig config = RunnableConfig.builder()
                .threadId(threadId)
                .addParallelNodeExecutor(CoursewareGenConstants.NODE_WORD_GENERATION, ForkJoinPool.commonPool())
                .addParallelNodeExecutor(CoursewareGenConstants.NODE_PPT_GENERATION, ForkJoinPool.commonPool())
                .build();

        return compiledGraph.stream(Map.of(
                        CoursewareGenConstants.KEY_USER_ID, userId,
                        CoursewareGenConstants.KEY_INPUT, input,
                        CoursewareGenConstants.KEY_COURSEWARE_REC_ID, coursewareRecId
                ), config)
                .map(output -> AiMessageUtils.handleStreamingOutput(
                        output,
                        interruptionMetadata -> {
                            String nodeName = interruptionMetadata.node();
                            String outline = interruptionMetadata.metadata(CoursewareGenConstants.KEY_PPT_OUTLINE).orElseThrow().toString();

                            log.error("节点执行中断！{}, outline: {}", nodeName, outline);

                            return String.format("""
                                    节点%s已中断。
                                    PPT大纲:
                                    %s
                                    
                                    [DONE]
                                    """, nodeName, outline);
                        },
                        null,
                        () -> {
                            Long latestChatMessageId = getLatestChatMessageId(coursewareRecId);
                            if (latestChatMessageId == null) return null;
                            return latestChatMessageId.toString();
                        }
                ));
    }

    public Flux<String> resumeWorkflowGraph(UserChatMessage userChatMessage) {
        Long userId = userChatMessage.getUserId();
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
                .addMetadata(CoursewareGenConstants.KEY_HITL_USER_FEEDBACK, resumeUserFeedback)
                .addParallelNodeExecutor(CoursewareGenConstants.NODE_WORD_GENERATION, ForkJoinPool.commonPool())
                .addParallelNodeExecutor(CoursewareGenConstants.NODE_PPT_GENERATION, ForkJoinPool.commonPool())
                .build();

        return compiledGraph.stream(null, resumeConfig)
                .map(output -> AiMessageUtils.handleStreamingOutput(
                        output,
                        interruptionMetadata -> {
                            String nodeName = interruptionMetadata.node();
                            String outline = interruptionMetadata.metadata(CoursewareGenConstants.KEY_PPT_OUTLINE).get().toString();

                            log.error("节点执行中断！{}, outline: {}", nodeName, outline);

                            return String.format("""
                                    节点%s已中断。
                                    PPT大纲:
                                    %s
                                    
                                    [DONE]
                                    """, nodeName, outline);
                        },
                        null,
                        null
                ));
    }

    public ChatHistoryDTO getChatHistory(Long coursewareRecId) {
        ChatSession session = chatPersistenceService.getChatSessionByCoursewareRecId(coursewareRecId);
        List<ChatMessage> chatMessages = chatPersistenceService.getChatMessages(session.getId());
        return chatPersistenceService.constructChatHistory(session.getId(), session.getUserId(), chatMessages);
    }

    private Long getLatestChatMessageId(Long coursewareRecId) {
        ChatSession chatSession = chatPersistenceService.getChatSessionByCoursewareRecId(coursewareRecId);
        if (chatSession == null) {
            log.error("未找到与课件生成对应的聊天记录！");
            return null;
        }

        return chatPersistenceService.getLatestAiMessageByChatId(chatSession.getId()).getId();
    }

    private String constructThreadId(Long userId) {
        return "user_graph_thread_" + userId;
    }

}
