package com.superyoudi.businessservice.service;


import com.superyoudi.aiservice.mapper.LearningPathNodeMapper;
import com.superyoudi.businessservice.mapper.*;
import com.superyoudi.businessservice.pojo.entity.StudentCourseKey;
import com.superyoudi.common.dto.StudentCourseProgress;
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
class StudentCourseProgressServiceTest {

    @Mock
    private CourseStudentRelMapper courseStudentMapper;

    @Mock
    private LearningPathNodeMapper learningPathNodeMapper;

    @Mock
    private StudentLearningAttitudeMapper studentLearningAttitudeMapper;

    @Mock
    private StudentSubmissionMapper studentSubmissionMapper;

    @Mock
    private StudentKnowledgeMasteryMapper studentKnowledgeMasteryMapper;

    @Mock
    private SectionKnowledgeRelMapper sectionKnowledgeRelMapper;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private StudentCourseProgressMapper studentCourseProgressMapper;

    @InjectMocks
    private StudentCourseProgressService service;

    @Test
    void syncOne_computesProgressRates_andUpserts() {
        when(learningPathNodeMapper.countNodes(1L, 10L)).thenReturn(10L);
        when(learningPathNodeMapper.countCompletedNodes(1L, 10L)).thenReturn(4L);
        when(studentLearningAttitudeMapper.sumInteractionCount(1L)).thenReturn(12L);
        when(studentSubmissionMapper.selectAvgTaskScore(1L, 10L)).thenReturn(85.0);
        when(studentKnowledgeMasteryMapper.countCoveredKnowledge(1L, 10L)).thenReturn(8L);
        when(sectionKnowledgeRelMapper.countCourseKnowledge(10L)).thenReturn(16L);
        when(taskMapper.countPublishedTasksByCourseId(10L)).thenReturn(5L);
        when(studentSubmissionMapper.countCompletedTasks(1L, 10L)).thenReturn(3L);

        service.syncOne(1L, 10L);

        ArgumentCaptor<StudentCourseProgress> captor =
                ArgumentCaptor.forClass(StudentCourseProgress.class);
        verify(studentCourseProgressMapper).upsert(captor.capture());

        StudentCourseProgress progress = captor.getValue();
        assertThat(progress.getStudentId()).isEqualTo(1L);
        assertThat(progress.getCourseId()).isEqualTo(10L);
        assertThat(progress.getTotalProgress()).isEqualTo(40.0);
        assertThat(progress.getInteractionCount()).isEqualTo(12L);
        assertThat(progress.getAvgTaskScore()).isEqualTo(85.0);
        assertThat(progress.getKnowledgeCoverRate()).isEqualTo(50.0);
        assertThat(progress.getTaskCompletionRate()).isEqualTo(60.0);
    }

    @Test
    void syncOne_handlesNullValues_andZeroTotals() {
        when(learningPathNodeMapper.countNodes(1L, 10L)).thenReturn(null);
        when(learningPathNodeMapper.countCompletedNodes(1L, 10L)).thenReturn(null);
        when(studentLearningAttitudeMapper.sumInteractionCount(1L)).thenReturn(null);
        when(studentSubmissionMapper.selectAvgTaskScore(1L, 10L)).thenReturn(null);
        when(studentKnowledgeMasteryMapper.countCoveredKnowledge(1L, 10L)).thenReturn(null);
        when(sectionKnowledgeRelMapper.countCourseKnowledge(10L)).thenReturn(null);
        when(taskMapper.countPublishedTasksByCourseId(10L)).thenReturn(null);
        when(studentSubmissionMapper.countCompletedTasks(1L, 10L)).thenReturn(null);

        service.syncOne(1L, 10L);

        ArgumentCaptor<StudentCourseProgress> captor =
                ArgumentCaptor.forClass(StudentCourseProgress.class);
        verify(studentCourseProgressMapper).upsert(captor.capture());

        StudentCourseProgress progress = captor.getValue();
        assertThat(progress.getTotalProgress()).isZero();
        assertThat(progress.getInteractionCount()).isZero();
        assertThat(progress.getAvgTaskScore()).isZero();
        assertThat(progress.getKnowledgeCoverRate()).isZero();
        assertThat(progress.getTaskCompletionRate()).isZero();
    }

    @Test
    void syncAll_continuesAfterSingleFailure() {
        when(courseStudentMapper.selectActiveStudentCourses()).thenReturn(List.of(
                new StudentCourseKey(1L, 10L),
                new StudentCourseKey(2L, 10L)
        ));
        when(learningPathNodeMapper.countNodes(anyLong(), anyLong())).thenReturn(10L);
        when(learningPathNodeMapper.countCompletedNodes(anyLong(), anyLong())).thenReturn(4L);
        when(studentLearningAttitudeMapper.sumInteractionCount(anyLong())).thenReturn(12L);
        when(studentSubmissionMapper.selectAvgTaskScore(anyLong(), anyLong())).thenReturn(85.0);
        when(studentKnowledgeMasteryMapper.countCoveredKnowledge(anyLong(), anyLong())).thenReturn(8L);
        when(sectionKnowledgeRelMapper.countCourseKnowledge(anyLong())).thenReturn(16L);
        when(taskMapper.countPublishedTasksByCourseId(anyLong())).thenReturn(5L);
        when(studentSubmissionMapper.countCompletedTasks(anyLong(), anyLong())).thenReturn(3L);

        doThrow(new RuntimeException("db error"))
                .doReturn(1)
                .when(studentCourseProgressMapper).upsert(any(StudentCourseProgress.class));

        int count = service.syncAll();

        assertThat(count).isEqualTo(1);
        verify(studentCourseProgressMapper, times(2)).upsert(any(StudentCourseProgress.class));
    }
}