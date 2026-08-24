package com.superyoudi.aiservice.service;

import com.superyoudi.aiservice.pojo.entity.LearningPath;
import com.superyoudi.aiservice.pojo.entity.LearningPathNode;

import java.util.List;

/**
 * @author Sijayyx
 */
public interface LearningPathService {
    Long createLearningPath(Long studentId, Long courseId);

    List<LearningPathNode> getLearningPathNodes(Long studentId, Long courseId);

    List<LearningPathNode> getLearningPathNodes(Long pathId);

    Long getNextLearningPathNode(Long pathId, Long currentNodeId);

    LearningPath getLearningPathById(Long learningPathId);

    LearningPath getLearningPathByStudentAndCourse(Long studentId, Long courseId);

    LearningPathNode getLearningPathNodeById(Long nodeId);

    void updateLearningPathNodeStatus(Long nodeId, String status);

    void updateLearningPathStage(Long pathId, String stage);

    void updateLearningPath(LearningPath learningPath);

    Long setLearningPathToStartNode(Long pathId);
}
