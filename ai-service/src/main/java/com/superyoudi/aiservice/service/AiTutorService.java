package com.superyoudi.aiservice.service;

import com.superyoudi.aiservice.pojo.dto.LearningPathDTO;
import com.superyoudi.aiservice.pojo.dto.StudentCourseInfoDTO;
import com.superyoudi.aiservice.pojo.dto.UserChatMessage;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * @author Sijayyx
 */
@Service
public interface AiTutorService {

    Flux<String> callAiTutor(UserChatMessage userChatMessage);

    LearningPathDTO getLearningPathInfo(Long studentId, Long courseId);

    StudentCourseInfoDTO getStudentInfo(Long studentId);
}
