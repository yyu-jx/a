package com.superyoudi.businessservice.job;


import com.superyoudi.businessservice.service.MissionAnalysisService;
import com.superyoudi.businessservice.service.StudentCourseProgressService;
import com.superyoudi.businessservice.service.TeacherCourseAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class AnalysisScheduledJob {



    private final TeacherCourseAnalysisService teacherCourseAnalysisSyncService;
    private final StudentCourseProgressService studentCourseProgressSyncService;
    private final MissionAnalysisService missionAnalysisSyncService;



    @Scheduled(cron = "${analysis.cron.teacher-course:0 0/30 7-23 * * *}", zone = "${analysis.schedule-zone:Asia/Shanghai}")
    public void syncTeacherCourseAnalysis() {
        log.info("[scheduled] teacher_course_analysises sync begin");
        int rows = teacherCourseAnalysisSyncService.syncAll();
        log.info("[scheduled] teacher_course_analysises sync done, rows={}", rows);
    }

    @Scheduled(cron = "${analysis.cron.student-course:0 0/15 7-23 * * *}", zone = "${analysis.schedule-zone:Asia/Shanghai}")
    public void syncStudentCourseProgress() {
        log.info("[scheduled] student_course_progresses sync begin");
        int rows = studentCourseProgressSyncService.syncAll();
        log.info("[scheduled] student_course_progresses sync done, rows={}", rows);
    }

    @Scheduled(cron = "${analysis.cron.mission:0 0/10 * * * *}", zone = "${analysis.schedule-zone:Asia/Shanghai}")
    public void syncMissionAnalysis() {
        log.info("[scheduled] mission_analysises sync begin");
        int rows = missionAnalysisSyncService.syncAll();
        log.info("[scheduled] mission_analysises sync done, rows={}", rows);
    }
}