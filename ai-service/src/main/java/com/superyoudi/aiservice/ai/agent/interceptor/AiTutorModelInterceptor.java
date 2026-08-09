package com.superyoudi.aiservice.ai.agent.interceptor;

import com.alibaba.cloud.ai.graph.agent.hook.HookPosition;
import com.alibaba.cloud.ai.graph.agent.hook.HookPositions;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelCallHandler;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelInterceptor;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelRequest;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelResponse;
import com.superyoudi.aiservice.ai.agent.AiTutorAgent;
import com.superyoudi.aiservice.pojo.dto.LearningPathDTO;
import com.superyoudi.aiservice.pojo.dto.StudentCourseInfoDTO;
import com.superyoudi.aiservice.pojo.entity.LearningPath;
import com.superyoudi.aiservice.service.AiTutorService;
import com.superyoudi.common.exception.LearningPathFinishedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;


/// 通过此interceptor在每次调用模型时都根据知识节点状态动态调整提示词。
/// - 注入系统提示词
/// - 注入指令
/// - 获取学习路径（如果不存在，则在调用的service方法内再调用大模型创建）
/// - 获取当前学生的学习路径，当前学习路径节点及状态
///
/// @author Sijayyx
@Slf4j
@Component
@RequiredArgsConstructor
@HookPositions({HookPosition.BEFORE_MODEL}) // 在调用模型之前执行
public class AiTutorModelInterceptor extends ModelInterceptor {

    private final AiTutorService aiTutorService;

    @Override
    public ModelResponse interceptModel(ModelRequest request, ModelCallHandler handler) {

        Long studentId = (Long) request.getContext().getOrDefault(AiTutorAgent.METADATA_STUDENT_ID, null);
        Long courseId = (Long) request.getContext().getOrDefault(AiTutorAgent.METADATA_COURSE_ID, null);

        // 我直接抛异常😍！！！！
        if (studentId == null || courseId == null) {
            log.error("student_id和courseId不在上下文中");
            throw new RuntimeException("student_id和courseId不在上下文中");
        }

        String systemPrompt = buildSystemPrompt(studentId, courseId);
        SystemMessage enhancedSystemMessage = new SystemMessage(systemPrompt);

        // 构造上下文
        ModelRequest modified = ModelRequest.builder(request)
                .systemMessage(enhancedSystemMessage)
                .build();
        return handler.call(modified);
    }

    private String buildSystemPrompt(Long studentId, Long courseId) {

        String basePrompt = """
                你是智教助手平台的AI导师，正在根据学习路径为一名学生讲解知识点。
                
                ## 基本信息
                - ID：{student_id}
                - 姓名：{student_name}
                
                ## 当前学习路径
                - ID：{learning_path_id}
                - 课程名称：{course_name}
                - 课程ID：{course_id}
                
                ## 当前路径点
                - ID: {node_id}
                - 名称：{node_name}
                
                ## 核心原则
                1. 不直接给出答案，通过提问引导学生自己思考
                2. 根据学生能力水平调整讲解深度
                3. 每次回复后等待学生回应，不要一次说太多
                4. 你的输出必须为Markdown格式
                
                """;

        StudentCourseInfoDTO studentInfo = aiTutorService.getStudentInfo(studentId);

        // 根据阶段拼接提示词
        LearningPathDTO learningPathInfo = aiTutorService.getLearningPathInfo(studentId, courseId);
        String stagePrompt = "";

        if (learningPathInfo.getCurrentStage().equals(LearningPath.STAGE_FINISHED)) {
            // 当前节点已完成，跳到下一节点
            try {
                aiTutorService.toNextLearningPathNode(learningPathInfo.getLearningPathId());
                learningPathInfo = aiTutorService.getLearningPathInfo(studentId, courseId);
            } catch (LearningPathFinishedException e) {
                stagePrompt = "所有学习路径的节点已全部完成。";
            }
        } else {
            stagePrompt = buildStagePrompt(learningPathInfo);
        }

        // 拼接字符串
        basePrompt += stagePrompt;

        // 格式化当前学习路径
        String learningPath = aiTutorService.formatLearningPath(learningPathInfo.getLearningPathId());

        // 拼接字符串
        basePrompt += learningPath;

        Map<String, Object> variables = Map.of(
                "student_id", studentInfo.getStudentId(),
                "student_name", studentInfo.getStudentName(),
                "course_name", learningPathInfo.getCourseName(),
                "course_id", learningPathInfo.getCourseId(),
                "learning_path_id", learningPathInfo.getLearningPathId(),
                "node_id", learningPathInfo.getCurrentPathNodeId(),
                "node_name", learningPathInfo.getCurrentNodeName()
        );

        PromptTemplate pt = new PromptTemplate(basePrompt);

        String prompt = pt.render(variables);
        log.info("AI导师提示词：\n{}", prompt);

        return prompt;
    }

    public String buildStagePrompt(LearningPathDTO learningPathDTO) {

        return switch (learningPathDTO.getCurrentStage()) {
            case LearningPath.STAGE_INTRODUCING -> """
                    ## 当前阶段：概念引入
                    
                    ## 任务
                    激活学生的先验知识，建立与新知识点的联系，激发学习兴趣。
                    
                    ## 输出要求
                    1. 用一个生活中的例子或直观比喻引入知识点
                    2. 提出1-2个引导性问题，让学生思考
                    3. 简要说明这个知识点在整个知识体系中的位置
                    4. 告知学生本阶段的学习目标
                    
                    ## 完成后
                    将当前阶段设置为 explaining
                    """;
            case LearningPath.STAGE_EXPLAINING -> """
                    ## 当前阶段：知识讲解
                    
                    ## 任务
                    系统性地讲解知识点的核心概念、定义、性质和公式。根据学生互动风格和掌握度调整深度。
                    
                    ## 输出要求
                    1. 从定义/概念出发，逐步深入
                    2. 使用适当的数学符号和公式（LaTeX格式）
                    3. 对于关键概念，用**加粗**强调
                    4. 每讲解1-2个要点后，提出一个确认性问题（"这里理解了吗？"）
                    
                    ## 完成后
                    将当前阶段设置为 finished
                    """;
            // TODO 生成测验
            default -> {
                log.error("知识路径节点状态错误。");
                yield "知识路径节点状态错误。";
            }
        };

    }

    @Override
    public String getName() {
        return "ai-tutor-interceptor";
    }
}
