package com.superyoudi.businessservice.service.impl;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.superyoudi.aiservice.ai.agent.AiTutorAgent;
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
import com.superyoudi.businessservice.pojo.dto.TaskDTO;
import com.superyoudi.businessservice.pojo.entity.*;
import com.superyoudi.businessservice.service.QuestionBaseService;
import com.superyoudi.businessservice.service.TaskService;
import com.superyoudi.common.dto.ChatHistoryDTO;
import com.superyoudi.common.exception.LearningPathFinishedException;
import com.superyoudi.kdbservice.dto.KnowledgePointDTO;
import com.superyoudi.kdbservice.service.KnowledgePointService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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
    public Flux<String> callAiTutor(UserChatMessage userChatMessage) {
        Long courseId = (Long) userChatMessage.getAdditionalData();

        // 设置config的metadata以进行context engineering
        RunnableConfig runnableConfig = RunnableConfig.builder()
                .threadId("ai_tutor_thread_" + userChatMessage.getUserId() + "_" + courseId)
                .addMetadata(AiTutorAgent.METADATA_STUDENT_ID, userChatMessage.getUserId())
                .addMetadata(AiTutorAgent.METADATA_COURSE_ID, courseId)
                .addMetadata(AiTutorAgent.METADATA_CUSTOM_METADATA, new HashMap<String, Object>())
                .build();

        try {
            Flux<String> result = aiTutorAgent.callAgent(userChatMessage.getMessage(), runnableConfig)
                    .map(nodeOutput ->
                            AiMessageUtils.handleStreamingOutput(
                                    nodeOutput,
                                    null,
                                    message -> log.info("AI Tutor回复: {}", message), // 打日志
                                    () -> {
                                        ChatSession chatSession = chatPersistenceService.getOrCreateChatSession(userChatMessage.getUserId(), courseId);
                                        if (chatSession == null)
                                            return null; // 如果聊天会话不存在，返回null

                                        ChatMessage latestMsg = chatPersistenceService.getLatestAiMessageByChatId(chatSession.getId());

                                        if (latestMsg == null)
                                            return null;

                                        return latestMsg.getId().toString(); // 附加AI对话的消息ID
                                    }));


            return result;
        } catch (GraphRunnerException e) {
            log.error("调用AI Tutor失败", e);
            throw new RuntimeException("调用AI Tutor失败", e);
        }
    }

    @Override
    public ChatHistoryDTO getChatHistory(Long studentId, Long courseId) {
        ChatSession session = chatPersistenceService.getOrCreateChatSession(studentId, courseId);
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
            learningPathService.createLearningPath(studentId, courseId);
        }

        Long currentNodeId = learningPath.getCurrentNodeId();
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

                // 获取知识点信息
                List<KnowledgePointDTO> kpDtoList = knowledgePointService.batchGetKnowledgePoints(kpIds);
                StringBuilder knowledgeTitle = new StringBuilder();

                for (KnowledgePointDTO kpDto : kpDtoList) {
                    if (kpDto == null || kpDto.getKnowledgeTitle() == null || kpDto.getKnowledgeTitle().isEmpty())
                        continue;

                    knowledgeTitle.append(kpDto.getKnowledgeTitle()).append(", ");
                }

                // 格式化节点信息
                sb.append("节点ID：").append(node.getId())
                        .append("，知识点列表：").append(knowledgeTitle)
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
    public void composePathNodeHomework(long pathNodeId,
                                        long courseId,
                                        long studentId,
                                        List<String> knowledgePoints,
                                        String title,
                                        Map<String, Integer> questionConfig,
                                        int totalScore) {
        /*
            组题步骤:
            1. 根据知识点查question set
            2. 根据题型分组成question lists
            3. 检查每个题型是否有足够的题目，如果没有，调用AI生成。
            4. 组装题目，生成任务，进行知识节点的关联，向前端推送。
         */

        Set<Question> candidateQs = new HashSet<>();
        for (String knowledgePoint : knowledgePoints) {
            candidateQs.addAll(questionBaseService.getQuestionsByKnowledge(knowledgePoint));
        }

        Map<String, List<Question>> grouped = candidateQs.stream().collect(Collectors.groupingBy(Question::getType));

        Map<String, List<Question>> questions = new HashMap<>();
        Random random = new Random();
        for (Map.Entry<String, List<Question>> typedQuestions : grouped.entrySet()) {
            int diff = questionConfig.get(typedQuestions.getKey()) - typedQuestions.getValue().size();
            // 如果题型数量不足，调用AI生成题目
            if (diff > 0) {
                List<Question> generated = questionBaseService.generateQuestions(1L, courseId, knowledgePoints,
                        typedQuestions.getKey(), diff);

                typedQuestions.getValue().addAll(generated);
            }

            // 每个题型随机选几个题目
            List<Question> randomSelection = random.ints(0, typedQuestions.getValue().size()) // 生成无限流
                    .distinct()
                    .limit(questionConfig.get(typedQuestions.getKey()))
                    .mapToObj(typedQuestions.getValue()::get)
                    .toList();

            questions.put(typedQuestions.getKey(), randomSelection);
        }


        int totalQuestionCount = questions.values().stream().mapToInt(List::size).sum();
        int scoreEach = totalScore / totalQuestionCount;

        // 题号计数
        AtomicInteger index = new AtomicInteger(0);

        List<TaskDTO.QuestionRelInfo> result = questions.values().stream()
                .flatMap(List::stream)
                .map(q -> {
                    Assert.notNull(q.getId(), "题目ID不能为空");
                    return new TaskDTO.QuestionRelInfo(null, q.getId(), index.getAndIncrement(), scoreEach);
                })
                .toList();

        // 存表，构造关联
        TaskDTO build = TaskDTO.builder().id(null).courseId(null).assignerId(null).title(title).description(null).type("homework")
                .totalScore(totalScore).startTime(LocalDateTime.now()).deadline(LocalDateTime.MAX)
                .status("published").afterSubmitStrategy(Task.SUBMIT_STRATEGY_AUTO_ALL)
                .questionRelInfos(result).build();

        Long taskId = taskService.saveTaskData(build);
        Assert.notNull(taskId, "任务ID不能为空");

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
}
