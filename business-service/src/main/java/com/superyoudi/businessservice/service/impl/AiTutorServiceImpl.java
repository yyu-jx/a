package com.superyoudi.businessservice.service.impl;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.utils.TypeRef;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.superyoudi.aiservice.ai.agent.AiTutorAgent;
import com.superyoudi.aiservice.pojo.dto.AiCitation;
import com.superyoudi.aiservice.pojo.dto.LearningPathDTO;
import com.superyoudi.aiservice.pojo.dto.StudentCourseInfoDTO;
import com.superyoudi.aiservice.pojo.dto.UserChatMessage;
import com.superyoudi.aiservice.pojo.entity.ChatMessage;
import com.superyoudi.aiservice.pojo.entity.ChatSession;
import com.superyoudi.aiservice.pojo.entity.LearningPath;
import com.superyoudi.aiservice.pojo.entity.LearningPathNode;
import com.superyoudi.aiservice.service.AiTutorService;
import com.superyoudi.aiservice.service.ChatPersistenceService;
import com.superyoudi.aiservice.service.LearningPathService;
import com.superyoudi.aiservice.utils.AiMessageUtils;
import com.superyoudi.businessservice.mapper.CourseMapper;
import com.superyoudi.businessservice.mapper.UserMapper;
import com.superyoudi.businessservice.pojo.dto.AgentTaskResultDTO;
import com.superyoudi.businessservice.pojo.entity.*;
import com.superyoudi.businessservice.service.QuestionBaseService;
import com.superyoudi.businessservice.service.TaskService;
import com.superyoudi.businessservice.websocket.AgentTaskResultMessageEndpoint;
import com.superyoudi.common.constants.AgentConstants;
import com.superyoudi.common.dto.ChatHistoryDTO;
import com.superyoudi.common.exception.LearningPathFinishedException;
import com.superyoudi.kdbservice.service.KnowledgePointService;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Sijayyx
 */
@Slf4j
@Service
public class AiTutorServiceImpl implements AiTutorService {

    private final AiTutorAgent aiTutorAgent;

    private final CourseMapper courseMapper;
    private final LearningPathService learningPathService;
    private final KnowledgePointService knowledgePointService;
    private final UserMapper userMapper;
    private final ChatPersistenceService chatPersistenceService;
    private final QuestionBaseService questionBaseService;
    private final TaskService taskService;


    public AiTutorServiceImpl(@Lazy AiTutorAgent aiTutorAgent, // 延迟注入ai tutor agent，否则会造成循环依赖
                              CourseMapper courseMapper,
                              LearningPathService learningPathService,
                              KnowledgePointService knowledgePointService,
                              UserMapper userMapper,
                              ChatPersistenceService chatPersistenceService, QuestionBaseService questionBaseService, TaskService taskService) {
        this.courseMapper = courseMapper;
        this.learningPathService = learningPathService;
        this.knowledgePointService = knowledgePointService;
        this.userMapper = userMapper;
        this.aiTutorAgent = aiTutorAgent;
        this.chatPersistenceService = chatPersistenceService;
        this.questionBaseService = questionBaseService;
        this.taskService = taskService;
    }

    @Override
    public Flux<String> call(UserChatMessage<Long> userChatMessage) {
        Long courseId = userChatMessage.getAdditionalData();

        // 设置config的metadata以进行context engineering
        RunnableConfig runnableConfig = RunnableConfig.builder()
                .threadId("ai_tutor_thread_" + userChatMessage.getUserId() + "_" + courseId)
                .addMetadata(AgentConstants.METADATA_USER_ID, userChatMessage.getUserId())
                .addMetadata(AgentConstants.METADATA_COURSE_ID, courseId)
                .addMetadata(AgentConstants.METADATA_CUSTOM_METADATA, new HashMap<String, Object>())
                .build();

        try {
            return aiTutorAgent.callAgent(userChatMessage.getMessage(), runnableConfig)
                    .map(nodeOutput ->
                            AiMessageUtils.handleStreamingOutput(
                                    nodeOutput,
                                    null,
                                    message -> log.info("AI Tutor回复: {}", message), // 打日志
                                    () -> {
                                        // 添加引用消息
                                        Map<String, Object> customMetadata = runnableConfig.metadata(AgentConstants.METADATA_CUSTOM_METADATA, new TypeRef<Map<String, Object>>() {
                                        }).orElse(null);

                                        if (customMetadata != null) {
                                            List<AiCitation> citations = (List<AiCitation>) customMetadata.getOrDefault(AgentConstants.CUSTOM_METADATA_CITATIONS, List.of());
                                            try {
                                                return AiMessageUtils.LENIENT_MAPPER.writeValueAsString(citations);
                                            } catch (JsonProcessingException e) {
                                                return null;
                                            }
                                        }
                                        return null;
                                    }));
        } catch (GraphRunnerException e) {
            log.error("调用AI Tutor失败", e);
            throw new RuntimeException("调用AI Tutor失败", e);
        }
    }

    @Override
    public ChatHistoryDTO getChatHistory(Long studentId, Long courseId) {
        ChatSession session = chatPersistenceService.getOrCreateChatSessionByCourseRel(studentId, courseId);
        List<ChatMessage> chatMessages = chatPersistenceService.getChatMessages(session.getId());
        return chatPersistenceService.constructChatHistory(session.getId(), studentId, chatMessages);
    }

    @Override
    public void toNextLearningPathNode(Long learningPathId) throws LearningPathFinishedException {
        LearningPath learningPath = learningPathService.getLearningPathById(learningPathId);
        Long currentNodeId = learningPath.getCurrentNodeId();
        Long nextNodeId = learningPathService.getNextLearningPathNode(learningPathId, currentNodeId);

        // 将当前节点状态设置为已完成
        learningPathService.updateLearningPathNodeStatus(currentNodeId, LearningPathNode.NODE_STATUS_COMPLETED);

        // 下一节点的ID最大，表示学习路径已完成
        if (nextNodeId.equals(currentNodeId)) {
            log.info("学习路径 {} 已完成!", learningPathId);
            throw new LearningPathFinishedException("学习路径已完成");
        }

        // 下一节点变成当前节点，状态设为进行中
        learningPathService.updateLearningPathNodeStatus(nextNodeId, LearningPathNode.NODE_STATUS_PROCEEDING);

        // 更新节点
        learningPath.setCurrentNodeId(nextNodeId);
        learningPath.setCurrentStage(LearningPath.STAGE_INTRODUCING);
        learningPathService.updateLearningPath(learningPath);
    }

    @Override
    public LearningPathDTO getLearningPathInfo(@NonNull Long studentId, @NonNull Long courseId) {

        LearningPath learningPath = learningPathService.getLearningPathByStudentAndCourse(studentId, courseId);
        if (learningPath == null) {
            Long lpId = learningPathService.createLearningPath(studentId, courseId);
            learningPath = learningPathService.getLearningPathById(lpId);
        }

        Long currentNodeId = learningPath.getCurrentNodeId();

        // 如果当前currentNodeId为null，表示学习路径尚未开始，需要设置为开始节点
        if (currentNodeId == null)
            currentNodeId = learningPathService.setLearningPathToStartNode(learningPath.getId());

        LearningPathNode learningPathNode = learningPathService.getLearningPathNodeById(currentNodeId);


        Course course = courseMapper.selectById(courseId);

        return new LearningPathDTO(courseId, course.getName(), learningPath.getId(),
                currentNodeId, learningPathNode.getNodeName(), learningPathNode.getKnowledgeIds(), learningPath.getCurrentStage());
    }


    @Override
    public StudentCourseInfoDTO getStudentInfo(Long studentId) {
        User student = userMapper.selectById(studentId);
        return new StudentCourseInfoDTO(student.getId().toString(), student.getRealname());
    }

    @Override
    public String formatLearningPath(Long learningPathId) {
        ObjectMapper mapper = new ObjectMapper();
        List<LearningPathNode> learningPathNodes = learningPathService.getLearningPathNodes(learningPathId);

        if (learningPathNodes == null || learningPathNodes.isEmpty()) {
            return "该学习路径暂无任何节点内容。";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("学习路径 %d：\n", learningPathId));

        for (LearningPathNode node : learningPathNodes) {
            try {
                List<String> kpIds = mapper.readValue(node.getKnowledgeIds(), new TypeReference<>() {
                });

                // 格式化节点信息
                sb.append("节点ID：").append(node.getId())
                        .append("，知识点列表：").append(String.join(", ", kpIds))
                        .append("，状态：").append(node.getStatus())
                        .append("\n");
            } catch (JsonProcessingException e) {
                log.error("解析节点知识点失败", e);

            }


        }

        return sb.toString();
    }

    @Override
    public void updateLearningPathStage(Long pathId, String stage) {
        learningPathService.updateLearningPathStage(pathId, stage);
    }

    @Override
    public void composePathNodeHomework(
            long pathNodeId,
            long courseId,
            long studentId,
            List<String> knowledgePoints,
            String title,
            Map<String, Integer> questionConfig,
            int totalScore) {

        Long taskId = taskService.composeTask(0, courseId, knowledgePoints, title, null,
                "homework", questionConfig, totalScore);

        // 通知前端
        Task task = taskService.getTaskById(taskId);
        notifyTaskCompletion(studentId, task, AgentTaskResultDTO.TYPE_TASK);

        taskService.addPathNodeTaskRel(taskId, pathNodeId, studentId);
    }

    @Override
    public boolean checkPathNodeHomeWorkExists(long pathNodeId, long courseId, long studentId) {
        PathNodeTaskRel pathNodeTaskRel = taskService.getPathNodeTaskRel(pathNodeId);
        return pathNodeTaskRel != null;
    }

    @Override
    public boolean checkPathNodeHomeworkFinished(long pathNodeId, long studentId) {
        StudentSubmission studentSubmission = taskService.tryGetTaskSubmissionByPathNodeId(pathNodeId, studentId);

        if (studentSubmission == null) return false;

        return studentSubmission.getStatus().equals("submitted") || studentSubmission.getStatus().equals("passed");
    }

    @Override
    public <T> void notifyTaskCompletion(Long userId, @Nonnull T result, @Nonnull String taskType) {
        AgentTaskResultMessageEndpoint.notifyTaskCompletion(userId, result, taskType, chatPersistenceService);
    }
}
