package com.superyoudi.businessservice.service;


import com.superyoudi.businessservice.mapper.MissionAnalysisMapper;
import com.superyoudi.businessservice.mapper.StudentAnswerMapper;
import com.superyoudi.businessservice.mapper.StudentSubmissionMapper;
import com.superyoudi.businessservice.mapper.TaskMapper;
import com.superyoudi.businessservice.pojo.entity.MissionAnalysis;
import com.superyoudi.businessservice.pojo.entity.MissionScoreStats;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MissionAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(MissionAnalysisService.class);

    private final TaskMapper taskMapper;
    private final StudentSubmissionMapper studentSubmissionMapper;
    private final StudentAnswerMapper studentAnswerMapper;
    private final MissionAnalysisMapper missionAnalysisMapper;



    public int syncAll() {
        List<Long> taskIds = taskMapper.selectActiveTaskIds();
        int count = 0;
        for (Long taskId : taskIds) {
            try {
                syncOne(taskId);
                count++;
            } catch (Exception ex) {
                log.error("sync mission_analysises failed, taskId={}", taskId, ex);
            }
        }
        return count;
    }

    public void syncOne(Long taskId) {
        Long totalSubmitted = valueOrZero(studentSubmissionMapper.countSubmittedByTaskId(taskId));
        MissionScoreStats stats = studentSubmissionMapper.selectScoreStatsByTaskId(taskId);
        Double avgTimeSpent = valueOrZero(studentAnswerMapper.selectAvgTimeSpentByTaskId(taskId));

        MissionAnalysis analysis = new MissionAnalysis();
        analysis.setTaskId(taskId);
        analysis.setTotalSubmitted(totalSubmitted);
        analysis.setAvgScore(valueOrZero(stats.getAvgScore()));
        analysis.setMaxScore(valueOrZero(stats.getMaxScore()));
        analysis.setMinScore(valueOrZero(stats.getMinScore()));
        analysis.setAvgTimeSpent(avgTimeSpent);

        missionAnalysisMapper.upsert(analysis);
    }

    private Long valueOrZero(Long value) {
        return value == null ? 0L : value;
    }

    private Double valueOrZero(Double value) {
        return value == null ? 0.0 : value;
    }
}