package com.superyoudi.businessservice.service.impl;

import com.superyoudi.aiservice.service.AiTeacherAssistantService;
import com.superyoudi.businessservice.mapper.StudentCourseProgressMapper;
import com.superyoudi.businessservice.mapper.UserMapper;
import com.superyoudi.businessservice.pojo.dto.StudentStudyOverview;
import com.superyoudi.businessservice.pojo.entity.*;
import com.superyoudi.businessservice.service.CourseService;
import com.superyoudi.businessservice.service.TaskService;
import com.superyoudi.common.dto.CoursewareGenerationRequest;
import com.superyoudi.common.dto.TaskArrangementRequest;
import com.superyoudi.coursewareservice.pojo.entity.CoursewareRecord;
import com.superyoudi.coursewareservice.service.CoursewareInteractionService;
import com.superyoudi.coursewareservice.service.CoursewareRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

/**
 * @author Sijayyx
 */
@Service
@RequiredArgsConstructor
public class AiTeacherAssistantServiceImpl implements AiTeacherAssistantService {

    private final CoursewareInteractionService coursewareInteractionService;
    private final CoursewareRecordService coursewareRecordService;
    private final TaskService taskService;
    private final CourseService courseService;
    private final UserMapper userMapper;
    private final StudentCourseProgressMapper studentCourseProgressMapper;

    @Override
    public Long callCoursewareGeneration(long userId, CoursewareGenerationRequest request) {
        // 新建一个coursewareRecord
        Long newCoursewareRecordId = coursewareRecordService.insert(CoursewareRecord.builder().userId(userId).build());
        // 调用生成方法
        coursewareInteractionService.callCoursewareGeneration(userId, newCoursewareRecordId, request);
        // 返回coursewareRecordId
        return newCoursewareRecordId;
    }

    @Override
    public void correctTask(long taskId) {
        // FIXME: 耗时任务

        List<StudentSubmission> submissions = taskService.getSubmissions(taskId);
        for (StudentSubmission submission : submissions) {
            taskService.executeSubmissionCorrection(submission, true);
        }
    }

    @Override
    public void composeTask(TaskArrangementRequest request) {
        Assert.notNull(request, "TaskResubscriptionRequest 不能为null");
        taskService.composeTask(
                request.getAssignerId(),
                request.getCourseId(),
                request.getKnowledgePoints(),
                request.getTitle(),
                request.getDescription(),
                request.getType(),
                request.getQuestionConfig(),
                request.getTotalScore()
        );
    }

    @Override
    public String getCourseList(@NonNull Long userId) {
        List<Course> courses = courseService.getCoursesByTeacherId(userId);
        StringBuilder sb = new StringBuilder();
        for (Course course : courses) {
            sb.append("课程ID: ").append(course.getId()).append(", 课程名: ").append(course.getName()).append("\n");
        }
        return sb.toString(); // 返回课程列表
    }

    @Override
    public String getCourseInfo(@NonNull Long courseId) {
        Course course = courseService.getCourseById(courseId);
        TeacherCourseAnalysis analysis = courseService.getTeacherCourseAnalysis(courseId);

        if (course == null || analysis == null) {
            return "课程不存在或暂无分析数据";
        }

        return "课程基本信息:\n" +
                "课程ID: " + course.getId() + "\n" +
                "课程名称: " + course.getName() + "\n" +
                "\n" +
                "课程统计信息\n" +
                "学生总数: " + analysis.getTotalStudents() + "\n" +
                "平均成绩: " + analysis.getAvgScoreRate() + "\n" +
                "平均进度: " + analysis.getAvgProgress() + "\n" +
                "任务总数: " + analysis.getTaskCount() + "\n" +
                "提交率: " + analysis.getSubmissionRate() + "\n" +
                "薄弱知识点: " + (analysis.getWeakestKnowledge() != null ? analysis.getWeakestKnowledge() : "无") + "\n";

    }


    // 参数均为可选
    @Override
    public String getStudentAnalysis(@Nullable Long studentId, @Nullable String studentName) {
        if (studentId == null && studentName == null)
            return "未提供学生ID或学生姓名";

        if (studentId == null) {
            User user = userMapper.selectByRealName(studentName).get(0);
            studentId = user.getId();
            studentName = user.getRealname();
        }

        StudentStudyOverview studyOverview = studentCourseProgressMapper.getStudyOverview(studentId);
        if (studyOverview == null) {
            return "未找到学生分析数据";
        }

        return "学生信息:\n" +
                "学生ID: " + studentId + "\n" +
                "学生姓名: " + studentName + "\n" +
                "\n" +
                "学生统计信息:\n" +
                "总课程数: " + studyOverview.getStudyCourseTotal() + "\n" +
                "平均总进度: " + studyOverview.getAvgTotalProgress() + "\n" +
                "平均交互次数: " + studyOverview.getAvgInteractionCount() + "\n" +
                "平均任务得分: " + studyOverview.getAvgTaskScore() + "\n" +
                "平均知识覆盖率: " + studyOverview.getAvgKnowledgeCoverRate() + "\n" +
                "平均任务完成率: " + studyOverview.getAvgTaskCompletionRate() + "\n";

    }

    @Override
    public String getCourseTaskList(@NonNull Long courseId) {
        List<Task> courseRelatedTasks = taskService.getCourseRelatedTasks(courseId);
        StringBuilder sb = new StringBuilder();
        for (Task task : courseRelatedTasks) {
            sb.append("任务ID: ").append(task.getId()).append(", 任务标题: ").append(task.getTitle()).append("\n");
        }
        return sb.toString(); // 返回课程任务列表
    }

    @Override
    public String getTaskAnalysis(@NonNull Long taskId) {
        Task task = taskService.getCourseRelatedTasks(taskId).get(0);
        TaskAnalysis taskAnalysis = taskService.getTaskAnalysis(taskId);
        if (task == null || taskAnalysis == null)
            return "任务数据不存在";

        return "任务基本信息:\n" +
                "任务ID: " + task.getId() + "\n" +
                "任务标题: " + task.getTitle() + "\n" +
                "\n" +
                "任务统计信息:\n" +
                "总提交数: " + taskAnalysis.getTotalSubmitted() + "\n" +
                "平均得分: " + taskAnalysis.getAvgScore() + "\n" +
                "最高分: " + taskAnalysis.getMaxScore() + "\n" +
                "最低分: " + taskAnalysis.getMinScore() + "\n" +
                "平均时间花费: " + taskAnalysis.getAvgTimeSpent() + "\n";
    }
}
