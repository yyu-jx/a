package com.superyoudi.businessservice.service.impl;

import com.superyoudi.aiservice.pojo.dto.LearningPathDTO;
import com.superyoudi.aiservice.pojo.dto.StudentCourseInfoDTO;
import com.superyoudi.aiservice.pojo.dto.UserChatMessage;
import com.superyoudi.aiservice.pojo.entity.LearningPath;
import com.superyoudi.aiservice.pojo.entity.LearningPathNode;
import com.superyoudi.aiservice.service.AiTutorService;
import com.superyoudi.aiservice.service.LearningPathService;
import com.superyoudi.businessservice.mapper.CourseMapper;
import com.superyoudi.businessservice.mapper.UserMapper;
import com.superyoudi.businessservice.pojo.entity.Course;
import com.superyoudi.businessservice.pojo.entity.User;
import com.superyoudi.kdbservice.dto.KnowledgePointDTO;
import com.superyoudi.kdbservice.service.KnowledgePointService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * @author Sijayyx
 */
@Service
public class AiTutorServiceImpl implements AiTutorService {

    private final CourseMapper courseMapper;
    private final LearningPathService learningPathService;
    private final KnowledgePointService knowledgePointService;
    private final UserMapper userMapper;

    public AiTutorServiceImpl(CourseMapper courseMapper, LearningPathService learningPathService, KnowledgePointService knowledgePointService, UserMapper userMapper) {
        this.courseMapper = courseMapper;
        this.learningPathService = learningPathService;
        this.knowledgePointService = knowledgePointService;
        this.userMapper = userMapper;
    }

    @Override
    public Flux<String> callAiTutor(UserChatMessage userChatMessage) {
        return null;
    }

    @Override
    public LearningPathDTO getLearningPathInfo(Long studentId, Long courseId) {
        LearningPath learningPath = learningPathService.getLearningPathByStudentAndCourse(studentId, courseId);
        Long currentNodeId = learningPath.getCurrentNodeId();
        LearningPathNode learningPathNode = learningPathService.getLearningPathNodeById(currentNodeId);
        Course course = courseMapper.selectById(courseId);
        KnowledgePointDTO kpDto = knowledgePointService.getKnowledgePoint(learningPathNode.getKnowledgeId());

        return new LearningPathDTO(courseId, course.getName(), learningPath.getId(),
                currentNodeId, kpDto.getKnowledgeTitle(), learningPath.getCurrentStage());

    }

    @Override
    public StudentCourseInfoDTO getStudentInfo(Long studentId) {
        User student = userMapper.selectById(studentId);
        return new StudentCourseInfoDTO(student.getId().toString(), student.getRealname());
    }
}
