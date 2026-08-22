package com.superyoudi.businessservice.service;

import com.superyoudi.aiservice.mapper.LearningPathNodeMapper;
import com.superyoudi.businessservice.mapper.*;
import com.superyoudi.businessservice.pojo.entity.StudentCourseKey;
import com.superyoudi.common.dto.StudentCourseProgress;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentCourseProgressService {

    private static final Logger log = LoggerFactory.getLogger(StudentCourseProgressService.class);

    private final CourseStudentRelMapper courseStudentMapper;
    private final LearningPathNodeMapper learningPathNodeMapper;
    private final StudentLearningAttitudeMapper studentLearningAttitudeMapper;
    private final StudentSubmissionMapper studentSubmissionMapper;
    private final StudentKnowledgeMasteryMapper studentKnowledgeMasteryMapper;
    private final SectionKnowledgeRelMapper sectionKnowledgeRelMapper;
    private final TaskMapper taskMapper;
    private final StudentCourseProgressMapper studentCourseProgressMapper;


    public int syncAll() {
        List<StudentCourseKey> keys = courseStudentMapper.selectActiveStudentCourses();
        int count = 0;
        for (StudentCourseKey key : keys) {
            try {
                syncOne(key.getStudentId(), key.getCourseId());
                count++;
            } catch (Exception ex) {
                log.error("sync student_course_progresses failed, studentId={}, courseId={}",
                        key.getStudentId(), key.getCourseId(), ex);
            }
        }
        return count;
    }

    public void syncOne(Long studentId, Long courseId) {
        Long totalNodes = valueOrZero(learningPathNodeMapper.countNodes(studentId, courseId));
        Long completedNodes = valueOrZero(learningPathNodeMapper.countCompletedNodes(studentId, courseId));
        double totalProgress = totalNodes == 0
                ? 0.0
                : completedNodes * 100.0 / totalNodes;

        Long interactionCount = valueOrZero(studentLearningAttitudeMapper.sumInteractionCount(studentId));
        Double avgTaskScore = valueOrZero(studentSubmissionMapper.selectAvgTaskScore(studentId, courseId));

        Long coveredKnowledge = valueOrZero(studentKnowledgeMasteryMapper.countCoveredKnowledge(studentId, courseId));
        Long totalKnowledge = valueOrZero(sectionKnowledgeRelMapper.countCourseKnowledge(courseId));
        double knowledgeCoverRate = totalKnowledge == 0
                ? 0.0
                : coveredKnowledge * 100.0 / totalKnowledge;

        Long publishedTasks = valueOrZero(taskMapper.countPublishedTasksByCourseId(courseId));
        Long completedTasks = valueOrZero(studentSubmissionMapper.countCompletedTasks(studentId, courseId));
        double taskCompletionRate = publishedTasks == 0
                ? 0.0
                : completedTasks * 100.0 / publishedTasks;

        StudentCourseProgress progress = new StudentCourseProgress();
        progress.setStudentId(studentId);
        progress.setCourseId(courseId);
        progress.setTotalProgress(totalProgress);
        progress.setInteractionCount(interactionCount);
        progress.setAvgTaskScore(avgTaskScore);
        progress.setKnowledgeCoverRate(knowledgeCoverRate);
        progress.setTaskCompletionRate(taskCompletionRate);

        studentCourseProgressMapper.upsert(progress);
    }

    private Long valueOrZero(Long value) {
        return value == null ? 0L : value;
    }

    private Double valueOrZero(Double value) {
        return value == null ? 0.0 : value;
    }
}