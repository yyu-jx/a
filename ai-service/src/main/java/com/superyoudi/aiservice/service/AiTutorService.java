package com.superyoudi.aiservice.service;

import com.superyoudi.aiservice.pojo.dto.LearningPathDTO;
import com.superyoudi.aiservice.pojo.dto.StudentCourseInfoDTO;
import com.superyoudi.aiservice.pojo.dto.UserChatMessage;
import com.superyoudi.common.dto.ChatHistoryDTO;
import com.superyoudi.common.exception.LearningPathFinishedException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * @author Sijayyx
 */
@Service
public interface AiTutorService {

    Flux<String> callAiTutor(UserChatMessage userChatMessage);

    ChatHistoryDTO getChatHistory(Long studentId, Long courseId);

    void toNextLearningPathNode(Long learningPathId) throws LearningPathFinishedException;

    LearningPathDTO getLearningPathInfo(Long studentId, Long courseId);

    StudentCourseInfoDTO getStudentInfo(Long studentId);

    String formatLearningPath(Long learningPathId);

    void updateLearningPathStage(Long pathId, String stage);

    String queryKnowledgePoint(String kpName);

    String retriveKnowledgeGraph(String query);

    String retrieveDocuments(String query);
}
