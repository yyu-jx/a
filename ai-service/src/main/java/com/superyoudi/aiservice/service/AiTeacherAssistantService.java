package com.superyoudi.aiservice.service;

import com.superyoudi.common.dto.CoursewareGenerationRequest;
import com.superyoudi.common.dto.TaskArrangementRequest;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

/**
 * @author Sijayyx
 */
@Service
public interface AiTeacherAssistantService {

    Long callCoursewareGeneration(long userId, CoursewareGenerationRequest request);

    void correctTask(long taskId);

    void composeTask(TaskArrangementRequest request);

    String getCourseList(Long userId);

    String getCourseInfo(Long courseId);

    String getStudentAnalysis(@Nullable Long studentId, @Nullable String studentName);

    String getCourseTaskList(@NonNull Long courseId);

    String getTaskAnalysis(@NonNull Long taskId);
}
