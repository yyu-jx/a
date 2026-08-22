package com.superyoudi.businessservice.service;


import com.superyoudi.businessservice.mapper.*;
import com.superyoudi.businessservice.pojo.entity.TeacherCourseAnalysis;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeacherCourseAnalysisServiceTest {

    @Mock
    private CourseMapper courseMapper;

    @Mock
    private CourseStudentRelMapper courseStudentMapper;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private StudentSubmissionMapper studentSubmissionMapper;

    @Mock
    private StudentCourseProgressMapper studentCourseProgressMapper;

    @Mock
    private StudentKnowledgeMasteryMapper studentKnowledgeMasteryMapper;

    @Mock
    private TeacherCourseAnalysisMapper teacherCourseAnalysisMapper;

    @InjectMocks
    private TeacherCourseAnalysisService service;

    @Test
    void syncOne_computesSubmissionRate_andUpserts() {
        when(courseStudentMapper.countStudentsByCourseId(1L)).thenReturn(10L);
        when(taskMapper.countPublishedTasksByCourseId(1L)).thenReturn(5L);
        when(studentSubmissionMapper.selectAvgScoreRateByCourseId(1L)).thenReturn(80.0);
        when(studentCourseProgressMapper.selectAvgProgressByCourseId(1L)).thenReturn(60.0);
        when(studentSubmissionMapper.countSubmittedByCourseId(1L)).thenReturn(40L);
        when(studentCourseProgressMapper.countAlertStudentsByCourseId(1L)).thenReturn(2L);
        when(studentKnowledgeMasteryMapper.selectWeakestKnowledgeByCourseId(1L)).thenReturn("kp-1");

        service.syncOne(1L);

        ArgumentCaptor<TeacherCourseAnalysis> captor =
                ArgumentCaptor.forClass(TeacherCourseAnalysis.class);
        verify(teacherCourseAnalysisMapper).upsert(captor.capture());

        TeacherCourseAnalysis analysis = captor.getValue();
        assertThat(analysis.getCourseId()).isEqualTo(1L);
        assertThat(analysis.getTotalStudents()).isEqualTo(10L);
        assertThat(analysis.getTaskCount()).isEqualTo(5L);
        assertThat(analysis.getAvgScoreRate()).isEqualTo(80.0);
        assertThat(analysis.getAvgProgress()).isEqualTo(60.0);
        assertThat(analysis.getSubmissionRate()).isEqualTo(80.0);
        assertThat(analysis.getAlertStudentCount()).isEqualTo(2L);
        assertThat(analysis.getWeakestKnowledge()).isEqualTo("kp-1");
    }

    @Test
    void syncOne_handlesNullValues_andZeroDenominator() {
        when(courseStudentMapper.countStudentsByCourseId(1L)).thenReturn(null);
        when(taskMapper.countPublishedTasksByCourseId(1L)).thenReturn(null);
        when(studentSubmissionMapper.selectAvgScoreRateByCourseId(1L)).thenReturn(null);
        when(studentCourseProgressMapper.selectAvgProgressByCourseId(1L)).thenReturn(null);
        when(studentSubmissionMapper.countSubmittedByCourseId(1L)).thenReturn(null);
        when(studentCourseProgressMapper.countAlertStudentsByCourseId(1L)).thenReturn(null);
        when(studentKnowledgeMasteryMapper.selectWeakestKnowledgeByCourseId(1L)).thenReturn(null);

        service.syncOne(1L);

        ArgumentCaptor<TeacherCourseAnalysis> captor =
                ArgumentCaptor.forClass(TeacherCourseAnalysis.class);
        verify(teacherCourseAnalysisMapper).upsert(captor.capture());

        TeacherCourseAnalysis analysis = captor.getValue();
        assertThat(analysis.getTotalStudents()).isZero();
        assertThat(analysis.getTaskCount()).isZero();
        assertThat(analysis.getAvgScoreRate()).isZero();
        assertThat(analysis.getAvgProgress()).isZero();
        assertThat(analysis.getSubmissionRate()).isZero();
        assertThat(analysis.getAlertStudentCount()).isZero();
        assertThat(analysis.getWeakestKnowledge()).isNull();
    }

    @Test
    void syncAll_continuesAfterSingleFailure() {
        when(courseMapper.selectActiveCourseIds()).thenReturn(List.of(1L, 2L, 3L));
        when(courseStudentMapper.countStudentsByCourseId(anyLong())).thenReturn(10L);
        when(taskMapper.countPublishedTasksByCourseId(anyLong())).thenReturn(5L);
        when(studentSubmissionMapper.selectAvgScoreRateByCourseId(anyLong())).thenReturn(80.0);
        when(studentCourseProgressMapper.selectAvgProgressByCourseId(anyLong())).thenReturn(60.0);
        when(studentSubmissionMapper.countSubmittedByCourseId(anyLong())).thenReturn(40L);
        when(studentCourseProgressMapper.countAlertStudentsByCourseId(anyLong())).thenReturn(2L);
        when(studentKnowledgeMasteryMapper.selectWeakestKnowledgeByCourseId(anyLong())).thenReturn("kp-1");

        doThrow(new RuntimeException("db error"))
                .doReturn(1)
                .doReturn(1)
                .when(teacherCourseAnalysisMapper).upsert(any(TeacherCourseAnalysis.class));

        int count = service.syncAll();

        assertThat(count).isEqualTo(2);
        verify(teacherCourseAnalysisMapper, times(3)).upsert(any(TeacherCourseAnalysis.class));
    }
}