package com.superyoudi.businessservice.service;


import com.superyoudi.businessservice.mapper.*;
import com.superyoudi.businessservice.pojo.entity.TeacherCourseAnalysis;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeacherCourseAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(TeacherCourseAnalysisService.class);

    private final CourseMapper courseMapper;
    private final CourseStudentRelMapper courseStudentMapper;
    private final TaskMapper taskMapper;
    private final StudentSubmissionMapper studentSubmissionMapper;
    private final StudentCourseProgressMapper studentCourseProgressMapper;
    private final StudentKnowledgeMasteryMapper studentKnowledgeMasteryMapper;
    private final TeacherCourseAnalysisMapper teacherCourseAnalysisMapper;

    public TeacherCourseAnalysis getTeacherCourseAnalysis(Long courseId) {
        return teacherCourseAnalysisMapper.selectByCourseId(courseId);
    }

    public int syncAll() {
        List<Long> courseIds = courseMapper.selectActiveCourseIds();
        int count = 0;
        for (Long courseId : courseIds) {
            try {
                syncOne(courseId);
                count++;
            } catch (Exception ex) {
                log.error("sync teacher_course_analysises failed, courseId={}", courseId, ex);
            }
        }
        return count;
    }

    public void syncOne(Long courseId) {
        Long totalStudents = valueOrZero(courseStudentMapper.countStudentsByCourseId(courseId));
        Long taskCount = valueOrZero(taskMapper.countPublishedTasksByCourseId(courseId));
        Double avgScoreRate = valueOrZero(studentSubmissionMapper.selectAvgScoreRateByCourseId(courseId));
        Double avgProgress = valueOrZero(studentCourseProgressMapper.selectAvgProgressByCourseId(courseId));
        Long submittedCount = valueOrZero(studentSubmissionMapper.countSubmittedByCourseId(courseId));

        long denominator = totalStudents * taskCount;
        double submissionRate = denominator == 0
                ? 0.0
                : submittedCount * 100.0 / denominator;

        Long alertStudentCount = valueOrZero(studentCourseProgressMapper.countAlertStudentsByCourseId(courseId));
        String weakestKnowledge = studentKnowledgeMasteryMapper.selectWeakestKnowledgeByCourseId(courseId);

        TeacherCourseAnalysis analysis = new TeacherCourseAnalysis();
        analysis.setCourseId(courseId);
        analysis.setTotalStudents(totalStudents);
        analysis.setAvgScoreRate(avgScoreRate);
        analysis.setAvgProgress(avgProgress);
        analysis.setTaskCount(taskCount);
        analysis.setSubmissionRate(submissionRate);
        analysis.setAlertStudentCount(alertStudentCount);
        analysis.setWeakestKnowledge(weakestKnowledge);

        teacherCourseAnalysisMapper.upsert(analysis);
    }

    private Long valueOrZero(Long value) {
        return value == null ? 0L : value;
    }

    private Double valueOrZero(Double value) {
        return value == null ? 0.0 : value;
    }
}