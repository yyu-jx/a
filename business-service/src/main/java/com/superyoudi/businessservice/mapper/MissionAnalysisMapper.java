package com.superyoudi.businessservice.mapper;


import com.superyoudi.businessservice.pojo.entity.MissionAnalysis;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MissionAnalysisMapper {

    @Insert("""
            INSERT INTO mission_analysises
              (task_id, total_submitted, avg_score, max_score, min_score,
               avg_time_spent, updated_at)
            VALUES (#{taskId}, #{totalSubmitted}, #{avgScore}, #{maxScore}, #{minScore},
                    #{avgTimeSpent}, NOW())
            ON DUPLICATE KEY UPDATE
              total_submitted = #{totalSubmitted},
              avg_score = #{avgScore},
              max_score = #{maxScore},
              min_score = #{minScore},
              avg_time_spent = #{avgTimeSpent},
              updated_at = NOW()
            """)
    int upsert(MissionAnalysis analysis);
}