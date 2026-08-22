package com.superyoudi.businessservice.service;

import com.superyoudi.businessservice.mapper.StudentAnswerMapper;
import com.superyoudi.businessservice.mapper.StudentSubmissionMapper;
import com.superyoudi.businessservice.mapper.TaskAnalysisMapper;
import com.superyoudi.businessservice.mapper.TaskMapper;
import com.superyoudi.businessservice.pojo.entity.TaskAnalysis;
import com.superyoudi.businessservice.pojo.entity.TaskScoreStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TaskAnalysisServiceTest {

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private StudentSubmissionMapper studentSubmissionMapper;

    @Mock
    private StudentAnswerMapper studentAnswerMapper;

    @Mock
    private TaskAnalysisMapper taskAnalysisMapper;

    @InjectMocks
    private TaskAnalysisService taskAnalysisService;

    private TaskScoreStats createStats(Double avg, Integer max, Integer min) {
        TaskScoreStats stats = new TaskScoreStats();
        stats.setAvgScore(avg);
        stats.setMaxScore(max);
        stats.setMinScore(min);
        return stats;
    }

    @Nested
    @DisplayName("getTaskAnalysis 方法测试")
    class GetTaskAnalysisTest {

        @Test
        @DisplayName("根据任务ID获取分析数据 - 成功")
        void shouldReturnAnalysisWhenExists() {
            // Given
            Long taskId = 1L;
            TaskAnalysis expected = new TaskAnalysis();
            expected.setId(1L);
            expected.setTaskId(taskId);
            expected.setTotalSubmitted(10);
            expected.setAvgScore(85.5);
            expected.setMaxScore(100);
            expected.setMinScore(60);
            expected.setAvgTimeSpent(300);

            when(taskAnalysisMapper.findByTaskId(taskId)).thenReturn(expected);

            // When
            TaskAnalysis actual = taskAnalysisService.getTaskAnalysis(taskId);

            // Then
            assertThat(actual).isNotNull();
            assertThat(actual.getTaskId()).isEqualTo(taskId);
            assertThat(actual.getTotalSubmitted()).isEqualTo(10);
            assertThat(actual.getAvgScore()).isEqualTo(85.5);
            assertThat(actual.getMaxScore()).isEqualTo(100);
            assertThat(actual.getMinScore()).isEqualTo(60);
            assertThat(actual.getAvgTimeSpent()).isEqualTo(300);

            verify(taskAnalysisMapper, times(1)).findByTaskId(taskId);
        }

        @Test
        @DisplayName("根据任务ID获取分析数据 - 返回null")
        void shouldReturnNullWhenNotExists() {
            // Given
            Long taskId = 999L;
            when(taskAnalysisMapper.findByTaskId(taskId)).thenReturn(null);

            // When
            TaskAnalysis actual = taskAnalysisService.getTaskAnalysis(taskId);

            // Then
            assertThat(actual).isNull();
            verify(taskAnalysisMapper, times(1)).findByTaskId(taskId);
        }

        @Test
        @DisplayName("根据任务ID获取分析数据 - 数据库异常")
        void shouldThrowExceptionWhenDatabaseError() {
            // Given
            Long taskId = 1L;
            when(taskAnalysisMapper.findByTaskId(taskId))
                    .thenThrow(new RuntimeException("Database connection failed"));

            // When & Then
            assertThatThrownBy(() -> taskAnalysisService.getTaskAnalysis(taskId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Database connection failed");

            verify(taskAnalysisMapper, times(1)).findByTaskId(taskId);
        }
    }

    @Nested
    @DisplayName("syncOne 方法测试")
    class SyncOneTest {

        private Long taskId;

        @BeforeEach
        void setUp() {
            taskId = 1L;
        }

        @Test
        @DisplayName("同步单个任务分析 - 正常数据")
        void shouldSyncAnalysisWithNormalData() {
            // Given
            TaskScoreStats stats = createStats(85.5, 100, 60);
            when(studentSubmissionMapper.selectScoreStatsByTaskId(taskId)).thenReturn(stats);
            when(studentSubmissionMapper.countSubmittedByTaskId(taskId)).thenReturn(10);
            when(studentAnswerMapper.selectAvgTimeSpentByTaskId(taskId)).thenReturn(300);

            // When
            taskAnalysisService.syncOne(taskId);

            // Then
            verify(studentSubmissionMapper, times(1)).selectScoreStatsByTaskId(taskId);
            verify(studentSubmissionMapper, times(1)).countSubmittedByTaskId(taskId);
            verify(studentAnswerMapper, times(1)).selectAvgTimeSpentByTaskId(taskId);

            verify(taskAnalysisMapper, times(1)).upsert(argThat(analysis -> {
                assertThat(analysis.getTaskId()).isEqualTo(taskId);
                assertThat(analysis.getTotalSubmitted()).isEqualTo(10);
                assertThat(analysis.getAvgScore()).isEqualTo(85.5);
                assertThat(analysis.getMaxScore()).isEqualTo(100);
                assertThat(analysis.getMinScore()).isEqualTo(60);
                assertThat(analysis.getAvgTimeSpent()).isEqualTo(300);
                return true;
            }));
        }

        @Test
        @DisplayName("同步单个任务分析 - 统计数据为null")
        void shouldHandleNullStats() {
            // Given
            when(studentSubmissionMapper.selectScoreStatsByTaskId(taskId)).thenReturn(null);
            when(studentSubmissionMapper.countSubmittedByTaskId(taskId)).thenReturn(0);
            when(studentAnswerMapper.selectAvgTimeSpentByTaskId(taskId)).thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> taskAnalysisService.syncOne(taskId))
                    .isInstanceOf(NullPointerException.class);

            verify(taskAnalysisMapper, never()).upsert(any(TaskAnalysis.class));
        }

        @Test
        @DisplayName("同步单个任务分析 - 部分字段为null")
        void shouldHandlePartialNullValues() {
            // Given
            TaskScoreStats stats = createStats(null, null, null);
            when(studentSubmissionMapper.selectScoreStatsByTaskId(taskId)).thenReturn(stats);
            when(studentSubmissionMapper.countSubmittedByTaskId(taskId)).thenReturn(null);
            when(studentAnswerMapper.selectAvgTimeSpentByTaskId(taskId)).thenReturn(null);

            // When
            taskAnalysisService.syncOne(taskId);

            // Then
            verify(taskAnalysisMapper, times(1)).upsert(argThat(analysis -> {
                assertThat(analysis.getTaskId()).isEqualTo(taskId);
                assertThat(analysis.getTotalSubmitted()).isZero();
                assertThat(analysis.getAvgScore()).isZero();
                assertThat(analysis.getMaxScore()).isZero();
                assertThat(analysis.getMinScore()).isZero();
                assertThat(analysis.getAvgTimeSpent()).isZero();
                return true;
            }));
        }

        @Test
        @DisplayName("同步单个任务分析 - 零提交")
        void shouldHandleZeroSubmissions() {
            // Given
            TaskScoreStats stats = createStats(0.0, 0, 0);
            when(studentSubmissionMapper.selectScoreStatsByTaskId(taskId)).thenReturn(stats);
            when(studentSubmissionMapper.countSubmittedByTaskId(taskId)).thenReturn(0);
            when(studentAnswerMapper.selectAvgTimeSpentByTaskId(taskId)).thenReturn(0);

            // When
            taskAnalysisService.syncOne(taskId);

            // Then
            verify(taskAnalysisMapper, times(1)).upsert(argThat(analysis -> {
                assertThat(analysis.getTaskId()).isEqualTo(taskId);
                assertThat(analysis.getTotalSubmitted()).isZero();
                assertThat(analysis.getAvgScore()).isZero();
                assertThat(analysis.getMaxScore()).isZero();
                assertThat(analysis.getMinScore()).isZero();
                assertThat(analysis.getAvgTimeSpent()).isZero();
                return true;
            }));
        }

        @Test
        @DisplayName("同步单个任务分析 - 数据库异常")
        void shouldThrowExceptionWhenDatabaseError() {
            // Given
            when(studentSubmissionMapper.selectScoreStatsByTaskId(taskId))
                    .thenThrow(new RuntimeException("Database error"));

            // When & Then
            assertThatThrownBy(() -> taskAnalysisService.syncOne(taskId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Database error");

            verify(taskAnalysisMapper, never()).upsert(any(TaskAnalysis.class));
        }
    }

    @Nested
    @DisplayName("syncAll 方法测试")
    class SyncAllTest {

        @Test
        @DisplayName("同步所有任务分析 - 成功")
        void shouldSyncAllTasks() {
            // Given
            List<Long> taskIds = Arrays.asList(1L, 2L, 3L);
            when(taskMapper.selectActiveTaskIds()).thenReturn(taskIds);

            // Mock syncOne for each task
            for (Long taskId : taskIds) {
                TaskScoreStats stats = createStats(80.0 + taskId, 100, 50);
                when(studentSubmissionMapper.selectScoreStatsByTaskId(taskId)).thenReturn(stats);
                when(studentSubmissionMapper.countSubmittedByTaskId(taskId)).thenReturn(5);
                when(studentAnswerMapper.selectAvgTimeSpentByTaskId(taskId)).thenReturn(200);
            }

            // When
            int count = taskAnalysisService.syncAll();

            // Then
            assertThat(count).isEqualTo(3);
            verify(taskMapper, times(1)).selectActiveTaskIds();
            verify(taskAnalysisMapper, times(3)).upsert(any(TaskAnalysis.class));
        }

        @Test
        @DisplayName("同步所有任务分析 - 无活动任务")
        void shouldReturnZeroWhenNoActiveTasks() {
            // Given
            when(taskMapper.selectActiveTaskIds()).thenReturn(Collections.emptyList());

            // When
            int count = taskAnalysisService.syncAll();

            // Then
            assertThat(count).isZero();
            verify(taskMapper, times(1)).selectActiveTaskIds();
            verify(taskAnalysisMapper, never()).upsert(any(TaskAnalysis.class));
        }

        @Test
        @DisplayName("同步所有任务分析 - 部分任务同步失败")
        void shouldContinueWhenSomeTasksFail() {
            // Given
            List<Long> taskIds = Arrays.asList(1L, 2L, 3L);
            when(taskMapper.selectActiveTaskIds()).thenReturn(taskIds);

            // Task 1 正常
            TaskScoreStats stats1 = createStats(85.0, 100, 60);
            when(studentSubmissionMapper.selectScoreStatsByTaskId(1L)).thenReturn(stats1);
            when(studentSubmissionMapper.countSubmittedByTaskId(1L)).thenReturn(10);
            when(studentAnswerMapper.selectAvgTimeSpentByTaskId(1L)).thenReturn(300);

            // Task 2 抛异常
            when(studentSubmissionMapper.selectScoreStatsByTaskId(2L))
                    .thenThrow(new RuntimeException("Sync failed for task 2"));

            // Task 3 正常
            TaskScoreStats stats3 = createStats(75.0, 95, 55);
            when(studentSubmissionMapper.selectScoreStatsByTaskId(3L)).thenReturn(stats3);
            when(studentSubmissionMapper.countSubmittedByTaskId(3L)).thenReturn(8);
            when(studentAnswerMapper.selectAvgTimeSpentByTaskId(3L)).thenReturn(250);

            // When
            int count = taskAnalysisService.syncAll();

            // Then
            assertThat(count).isEqualTo(2); // 2个成功
            verify(taskAnalysisMapper, times(2)).upsert(any(TaskAnalysis.class));
        }

        @Test
        @DisplayName("同步所有任务分析 - 所有任务都失败")
        void shouldReturnZeroWhenAllTasksFail() {
            // Given
            List<Long> taskIds = Arrays.asList(1L, 2L);
            when(taskMapper.selectActiveTaskIds()).thenReturn(taskIds);

            when(studentSubmissionMapper.selectScoreStatsByTaskId(anyLong()))
                    .thenThrow(new RuntimeException("Sync failed"));

            // When
            int count = taskAnalysisService.syncAll();

            // Then
            assertThat(count).isZero();
            verify(taskAnalysisMapper, never()).upsert(any(TaskAnalysis.class));
        }

        @Test
        @DisplayName("同步所有任务分析 - 空任务ID列表")
        void shouldHandleNullTaskList() {
            // Given
            when(taskMapper.selectActiveTaskIds()).thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> taskAnalysisService.syncAll())
                    .isInstanceOf(NullPointerException.class);
        }
    }
}