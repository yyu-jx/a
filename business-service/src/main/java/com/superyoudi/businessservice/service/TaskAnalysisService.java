package com.superyoudi.businessservice.service;


import com.superyoudi.businessservice.mapper.StudentAnswerMapper;
import com.superyoudi.businessservice.mapper.StudentSubmissionMapper;
import com.superyoudi.businessservice.mapper.TaskAnalysisMapper;
import com.superyoudi.businessservice.mapper.TaskMapper;
import com.superyoudi.businessservice.pojo.entity.TaskAnalysis;
import com.superyoudi.businessservice.pojo.entity.TaskScoreStats;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskAnalysisService {
    private final TaskMapper taskMapper;
    private final StudentSubmissionMapper studentSubmissionMapper;
    private final StudentAnswerMapper studentAnswerMapper;
    private final TaskAnalysisMapper taskAnalysisMapper;

    public TaskAnalysis getTaskAnalysis(Long taskId) {
        return taskAnalysisMapper.findByTaskId(taskId);
    }


    @Transactional
    public int syncAll() {
        List<Long> taskIds = taskMapper.selectActiveTaskIds();
        int count = 0;
        for (Long taskId : taskIds) {
            try {
                syncOne(taskId);
                count++;
            } catch (Exception ex) {
                log.error("sync task_analysises failed, taskId={}", taskId, ex);
            }
        }
        return count;
    }

    @Transactional
    public void syncOne(Long taskId) {
        TaskScoreStats stats = studentSubmissionMapper.selectScoreStatsByTaskId(taskId);
        Integer totalSubmitted = valueOrZero(studentSubmissionMapper.countSubmittedByTaskId(taskId));
        Integer avgTimeSpent = valueOrZero(studentAnswerMapper.selectAvgTimeSpentByTaskId(taskId));

        TaskAnalysis analysis = new TaskAnalysis();
        analysis.setTaskId(taskId);
        analysis.setTotalSubmitted(totalSubmitted);
        analysis.setAvgScore(valueOrZero(stats.getAvgScore()));
        analysis.setMaxScore(valueOrZero(stats.getMaxScore()));
        analysis.setMinScore(valueOrZero(stats.getMinScore()));
        analysis.setAvgTimeSpent(avgTimeSpent);

        taskAnalysisMapper.upsert(analysis);
    }

    private Long valueOrZero(Long value) {
        return value == null ? 0L : value;
    }

    private Double valueOrZero(Double value) {
        return value == null ? 0.0 : value;
    }

    private Integer valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }

}