package com.superyoudi.aiservice.service;

import com.superyoudi.aiservice.pojo.dto.LearningPathDTO;
import com.superyoudi.aiservice.pojo.dto.StudentCourseInfoDTO;
import com.superyoudi.aiservice.pojo.dto.UserChatMessage;
import com.superyoudi.common.dto.ChatHistoryDTO;
import com.superyoudi.common.exception.LearningPathFinishedException;
import jakarta.annotation.Nonnull;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

/**
 * @author Sijayyx
 */
@Service
public interface AiTutorService {

    Flux<String> call(UserChatMessage<Long> userChatMessage);

    ChatHistoryDTO getChatHistory(Long studentId, Long courseId);

    void toNextLearningPathNode(Long learningPathId) throws LearningPathFinishedException;

    LearningPathDTO getLearningPathInfo(Long studentId, Long courseId);

    StudentCourseInfoDTO getStudentInfo(Long studentId);

    String formatLearningPath(Long learningPathId);

    void updateLearningPathStage(Long pathId, String stage);

    void composePathNodeHomework(long pathNodeId,
                                 long courseId,
                                 long studentId,
                                 List<String> knowledgePoints,
                                 String title,
                                 Map<String, Integer> questionConfig,
                                 int totalScore);

    boolean checkPathNodeHomeWorkExists(long pathNodeId, long courseId, long studentId);

    boolean checkPathNodeHomeworkFinished(long pathNodeId, long studentId);

    <T> void notifyTaskCompletion(Long userId, @Nonnull T result, @Nonnull String taskType);
}
