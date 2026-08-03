package com.superyoudi.aiservice.ai.agent.interceptor;

import com.alibaba.cloud.ai.graph.agent.hook.HookPosition;
import com.alibaba.cloud.ai.graph.agent.hook.HookPositions;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelCallHandler;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelInterceptor;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelRequest;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelResponse;
import com.superyoudi.aiservice.pojo.dto.LearningPathDTO;
import com.superyoudi.aiservice.pojo.dto.StudentCourseInfoDTO;
import com.superyoudi.aiservice.service.AiTutorService;
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

        Long studentId = (Long) request.getContext().getOrDefault("student_id", null);
        Long courseId = (Long) request.getContext().getOrDefault("course_id", null);

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
        String prompt = """
                你是智教助手平台的AI导师，正在为一名学生讲解知识点。
                
                ## 学生信息
                - 姓名：{student_name}
                - 课程：{course_name}
                
                ## 当前知识点
                - 名称：{node_name}
                
                ## 核心原则
                1. 不直接给出答案，通过提问引导学生自己思考
                2. 根据学生能力水平调整讲解深度
                3. 每次回复后等待学生回应，不要一次说太多
                """;

        PromptTemplate pt = new PromptTemplate(prompt);

        StudentCourseInfoDTO studentInfo = aiTutorService.getStudentInfo(studentId);
        LearningPathDTO learningPathInfo = aiTutorService.getLearningPathInfo(studentId, courseId);

        Map<String, Object> variables = Map.of(
                "student_name", studentInfo.getStudentName(),
                "course_name", learningPathInfo.getCourseName(),
                "node_name", learningPathInfo.getCurrentNodeName()
        );

        // TODO 根据阶段拼接提示词
        // TODO 格式化当前学习路径

        return pt.render(variables);
    }

    @Override
    public String getName() {
        return "ai-tutor-interceptor";
    }
}
