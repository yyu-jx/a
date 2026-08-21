package com.superyoudi.aiservice.service;

import com.superyoudi.common.dto.CoursewareGenerationRequest;
import com.superyoudi.common.dto.TaskArrangementRequest;
import io.a2a.spec.TaskResubscriptionRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author Sijayyx
 */
@Service
public interface AiTeacherAssistantService {

    Long callCoursewareGeneration(long userId, CoursewareGenerationRequest request);

    void correctTask(long taskId);

    void composeTask(TaskArrangementRequest request);
}
