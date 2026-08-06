package com.superyoudi.businessservice.service.impl;

import com.superyoudi.aiservice.mapper.LearningPathMapper;
import com.superyoudi.aiservice.mapper.LearningPathNodeMapper;
import com.superyoudi.aiservice.pojo.entity.LearningPath;
import com.superyoudi.aiservice.pojo.entity.LearningPathNode;
import com.superyoudi.aiservice.service.LearningPathService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Sijayyx
 */
@Service
public class LearningPathServiceImpl implements LearningPathService {

    private final LearningPathMapper learningPathMapper;
    private final LearningPathNodeMapper learningPathNodeMapper;

    public LearningPathServiceImpl(LearningPathMapper learningPathMapper, LearningPathNodeMapper learningPathNodeMapper) {
        this.learningPathMapper = learningPathMapper;
        this.learningPathNodeMapper = learningPathNodeMapper;
    }

    @Override
    public void createLearningPath() {
        // TODO 调用AI生成学习路径
    }

    /**
     * 根据学生和课程获取学习路径节点
     */
    @Override
    public List<LearningPathNode> getLearningPathNodes(Long studentId, Long courseId) {
        LearningPath learningPath = learningPathMapper.selectByStudentAndCourse(studentId, courseId);
        return learningPathNodeMapper.selectByPathId(learningPath.getId());
    }

    /**
     * 根据学生和课程获取学习路径节点
     */
    @Override
    public List<LearningPathNode> getLearningPathNodes(Long pathId) {
        return learningPathNodeMapper.selectByPathId(pathId);
    }

    @Override
    public Long getNextLearningPathNode(Long pathId, Long currentNodeId) {
        return learningPathNodeMapper.selectNextNodeId(pathId, currentNodeId);
    }

    @Override
    public LearningPath getLearningPathById(Long learningPathId) {
        return learningPathMapper.selectById(learningPathId);
    }

    @Override
    public LearningPath getLearningPathByStudentAndCourse(Long studentId, Long courseId) {
        return learningPathMapper.selectByStudentAndCourse(studentId, courseId);
    }

    @Override
    public LearningPathNode getLearningPathNodeById(Long nodeId) {
        return learningPathNodeMapper.selectById(nodeId);
    }

    @Override
    public void updateLearningPathNodeStatus(Long nodeId, String status) {
        learningPathNodeMapper.updateStatus(nodeId, status);
    }

    @Override
    public void updateLearningPathStage(Long pathId, String stage) {
        learningPathMapper.updateCurrentStage(pathId, stage);
    }

    @Override
    public void updateLearningPath(LearningPath learningPath) {
        learningPathMapper.update(learningPath);
    }
}
