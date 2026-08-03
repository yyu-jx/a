package com.superyoudi.aiservice.service;

import com.superyoudi.aiservice.mapper.LearningPathMapper;
import com.superyoudi.aiservice.mapper.LearningPathNodeMapper;
import com.superyoudi.aiservice.pojo.entity.LearningPath;
import com.superyoudi.aiservice.pojo.entity.LearningPathNode;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Sijayyx
 */
@Service
public class LearningPathService {

    private final LearningPathMapper learningPathMapper;
    private final LearningPathNodeMapper learningPathNodeMapper;

    public LearningPathService(LearningPathMapper learningPathMapper, LearningPathNodeMapper learningPathNodeMapper) {
        this.learningPathMapper = learningPathMapper;
        this.learningPathNodeMapper = learningPathNodeMapper;
    }

    public void createLearningPath() {
        // TODO
    }

    /**
     * 根据学生和课程获取学习路径节点
     */
    public List<LearningPathNode> getLearningPathNodesByCourseId(Long studentId, Long courseId) {
        LearningPath learningPath = learningPathMapper.selectByStudentAndCourse(studentId, courseId);
        return learningPathNodeMapper.selectByPathId(learningPath.getId());
    }

    public LearningPath getLearningPathByStudentAndCourse(Long studentId, Long courseId) {
        return learningPathMapper.selectByStudentAndCourse(studentId, courseId);
    }

    public LearningPathNode getLearningPathNodeById(Long nodeId) {
        return learningPathNodeMapper.selectById(nodeId);
    }
}
