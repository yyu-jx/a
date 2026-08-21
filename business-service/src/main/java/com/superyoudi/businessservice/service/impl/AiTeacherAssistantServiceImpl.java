package com.superyoudi.businessservice.service.impl;

import com.superyoudi.aiservice.service.AiTeacherAssistantService;
import com.superyoudi.businessservice.pojo.entity.StudentSubmission;
import com.superyoudi.businessservice.service.TaskService;
import com.superyoudi.common.dto.CoursewareGenerationRequest;
import com.superyoudi.common.dto.TaskArrangementRequest;
import com.superyoudi.coursewareservice.pojo.entity.CoursewareRecord;
import com.superyoudi.coursewareservice.service.CoursewareInteractionService;
import com.superyoudi.coursewareservice.service.CoursewareRecordService;
import lombok.RequiredArgsConstructor;
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
}
