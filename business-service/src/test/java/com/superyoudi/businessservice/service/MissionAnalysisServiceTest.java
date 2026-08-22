package com.superyoudi.businessservice.service;


import com.superyoudi.businessservice.mapper.MissionAnalysisMapper;
import com.superyoudi.businessservice.mapper.StudentAnswerMapper;
import com.superyoudi.businessservice.mapper.StudentSubmissionMapper;
import com.superyoudi.businessservice.mapper.TaskMapper;
import com.superyoudi.businessservice.pojo.entity.MissionAnalysis;
import com.superyoudi.businessservice.pojo.entity.MissionScoreStats;
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
class MissionAnalysisServiceTest {

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private StudentSubmissionMapper studentSubmissionMapper;

    @Mock
    private StudentAnswerMapper studentAnswerMapper;

    @Mock
    private MissionAnalysisMapper missionAnalysisMapper;

    @InjectMocks
    private MissionAnalysisService service;

    @Test
    void syncOne_usesScoreStats_andUpserts() {
        MissionScoreStats stats = new MissionScoreStats();
        stats.setAvgScore(80.0);
        stats.setMaxScore(100.0);
        stats.setMinScore(60.0);

        when(studentSubmissionMapper.countSubmittedByTaskId(1L)).thenReturn(5L);
        when(studentSubmissionMapper.selectScoreStatsByTaskId(1L)).thenReturn(stats);
        when(studentAnswerMapper.selectAvgTimeSpentByTaskId(1L)).thenReturn(12.5);

        service.syncOne(1L);

        ArgumentCaptor<MissionAnalysis> captor =
                ArgumentCaptor.forClass(MissionAnalysis.class);
        verify(missionAnalysisMapper).upsert(captor.capture());

        MissionAnalysis analysis = captor.getValue();
        assertThat(analysis.getTaskId()).isEqualTo(1L);
        assertThat(analysis.getTotalSubmitted()).isEqualTo(5L);
        assertThat(analysis.getAvgScore()).isEqualTo(80.0);
        assertThat(analysis.getMaxScore()).isEqualTo(100.0);
        assertThat(analysis.getMinScore()).isEqualTo(60.0);
        assertThat(analysis.getAvgTimeSpent()).isEqualTo(12.5);
    }

    @Test
    void syncOne_handlesNullValues() {
        MissionScoreStats stats = new MissionScoreStats();

        when(studentSubmissionMapper.countSubmittedByTaskId(1L)).thenReturn(null);
        when(studentSubmissionMapper.selectScoreStatsByTaskId(1L)).thenReturn(stats);
        when(studentAnswerMapper.selectAvgTimeSpentByTaskId(1L)).thenReturn(null);

        service.syncOne(1L);

        ArgumentCaptor<MissionAnalysis> captor =
                ArgumentCaptor.forClass(MissionAnalysis.class);
        verify(missionAnalysisMapper).upsert(captor.capture());

        MissionAnalysis analysis = captor.getValue();
        assertThat(analysis.getTotalSubmitted()).isZero();
        assertThat(analysis.getAvgScore()).isZero();
        assertThat(analysis.getMaxScore()).isZero();
        assertThat(analysis.getMinScore()).isZero();
        assertThat(analysis.getAvgTimeSpent()).isZero();
    }

    @Test
    void syncAll_continuesAfterSingleFailure() {
        MissionScoreStats stats = new MissionScoreStats();
        stats.setAvgScore(80.0);
        stats.setMaxScore(100.0);
        stats.setMinScore(60.0);

        when(taskMapper.selectActiveTaskIds()).thenReturn(List.of(1L, 2L));
        when(studentSubmissionMapper.countSubmittedByTaskId(anyLong())).thenReturn(5L);
        when(studentSubmissionMapper.selectScoreStatsByTaskId(anyLong())).thenReturn(stats);
        when(studentAnswerMapper.selectAvgTimeSpentByTaskId(anyLong())).thenReturn(12.5);

        doThrow(new RuntimeException("db error"))
                .doReturn(1)
                .when(missionAnalysisMapper).upsert(any(MissionAnalysis.class));

        int count = service.syncAll();

        assertThat(count).isEqualTo(1);
        verify(missionAnalysisMapper, times(2)).upsert(any(MissionAnalysis.class));
    }
}