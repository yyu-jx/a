package com.superyoudi.aiservice.ai.agent.tools;

import com.superyoudi.aiservice.service.AiTeacherAssistantService;
import com.superyoudi.common.constants.AgentConstants;
import com.superyoudi.common.dto.CoursewareGenerationRequest;
import com.superyoudi.common.dto.TaskArrangementRequest;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Sijayyx
 */
@Component
public class AiTeacherAssistantTools {

    private final AiTeacherAssistantService aiTeacherAssistantService;

    public AiTeacherAssistantTools(AiTeacherAssistantService aiTeacherAssistantService) {
        this.aiTeacherAssistantService = aiTeacherAssistantService;
    }

    @Tool(name = "test")
    public void test(ToolContext context) {
        Map<String, Object> metadata = (Map<String, Object>) context.getContext().get(AgentConstants.METADATA_CUSTOM_METADATA);

        metadata.put("test", "hahaha");

        aiTeacherAssistantService.notifyTaskCompletion(1L, "test message","task");
    }

    // 课件生成工具
    @Tool(name = "generate_courseware", description = "生成课件")
    @SuppressWarnings("unused")
    public String generateCourseware(
            @ToolParam(description = """
                    wordAction为对教案执行的动作，generate表示新生成一份教案，edit表示对已有教案进行修改，other表示其它操作，none表示不执行任何操作。
                    wordActionInfo为对教案执行动作的信息与要求，若无任何动作，则设置为null。
                    pptAction为对PPT执行的动作，generate表示新生成一个PPT，edit表示对已有PPT进行修改，other表示其它操作，none表示不执行任何操作。
                    pptActionInfo为对PPT执行动作的信息与要求，若无任何动作，则设置为null。
                    title为课程标题
                    basicInfo为教学信息：如授课对象，年级，课程时长;
                    teachingTarget为教学目标：应该掌握的具体知识点;
                    coreKnowledges为核心知识点：最核心的1-3个知识点;
                    hardKnowlegdes为重难点：需要明确讲授或强调的内容;
                    teachingFlow为教学流程：较详细的课堂教学安排;
                    interactionDesign为互动设计: 可选，课堂安排中的互动设计，比如提问与小组讨论等;
                    additionalInfo为额外信息
                    """)
            CoursewareGenerationRequest request, ToolContext context) {

        Long userId = (Long) context.getContext().get(AgentConstants.METADATA_USER_ID);


      aiTeacherAssistantService.callCoursewareGeneration(userId, request);
        return "成功";
    }

    @Tool(name = "correct_task", description = "批改教师布置的任务")
    @SuppressWarnings("unused")
    public String correctTask(Long taskId, ToolContext context) {
        Long userId = (Long) context.getContext().get(AgentConstants.METADATA_USER_ID);

        aiTeacherAssistantService.correctTask(userId, taskId);
        return "成功";
    }

    @Tool(name = "arrange_task", description = "布置教师任务")
    @SuppressWarnings("unused")
    public String arrangeTask(
            @ToolParam(description = """
                    assignerId为null
                    courseId为任务对应的课程ID
                    knowledgePoints为知识点列表
                    title为任务标题
                    description为任务描述
                    type为任务类型，可选: homework, exam
                    questionConfig为题目配置，key表示题目类型，value表示题目数量。
                        可选题目类型有:
                            choice 单选或多选题
                            fill 填空题
                            subjective 主观题
                            true_false 判断题
                    totalScore为任务总分
                    """) TaskArrangementRequest request,
            ToolContext context) {

        Long userId = (Long) context.getContext().get(AgentConstants.METADATA_USER_ID);

        request.setAssignerId(userId);
        aiTeacherAssistantService.composeTask(userId, request);
        return "成功";
    }

    /*
        课程表 courses
        学生表 students
        任务表 tasks
        教师-课程分析表 teacher_course_analysises
        学生-课程学习进度表 student_course_progresses
        任务-分析表 mission_analysises
     */

    @Tool(name = "get_course_list", description = "获取课程列表")
    @SuppressWarnings("unused")
    public String getCourseList(ToolContext context) {
        Long userId = (Long) context.getContext().get(AgentConstants.METADATA_USER_ID);
        return aiTeacherAssistantService.getCourseList(userId);
    }

    @SuppressWarnings("unused")
    @Tool(name = "get_course_info", description = "获取单个课程分析数据")
    public String getCourseInfo(@ToolParam(description = "课程ID") Long courseId) {
        return aiTeacherAssistantService.getCourseInfo(courseId);
    }

    @SuppressWarnings("unused")
    @Tool(name = "get_student_analysis", description = "获取单个学生分析数据，两个参数选择其一即可")
    public String getStudentAnalysis(@ToolParam(description = "学生ID") Long studentId, @ToolParam(description = "学生姓名") String studentName) {
        return aiTeacherAssistantService.getStudentAnalysis(studentId, studentName);
    }

    @SuppressWarnings("unused")
    @Tool(name = "get_course_task_list", description = "获取单个课程下的任务列表")
    public String getCourseTaskList(@ToolParam(description = "课程ID") Long courseId) {
        return aiTeacherAssistantService.getCourseTaskList(courseId);
    }

    @SuppressWarnings("unused")
    @Tool(name = "get_task_analysis", description = "获取单个任务分析数据")
    public String getTaskAnalysis(@ToolParam(description = "任务ID") Long taskId) {
        return aiTeacherAssistantService.getTaskAnalysis(taskId);
    }
}
