package com.superyoudi.businessservice.mapper;

import com.superyoudi.businessservice.pojo.entity.TaskAnalysis;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author Sijayyx
 */
@Mapper
public interface TaskAnalysisMapper {


    @Insert("""
            INSERT INTO task_analysises
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
    int upsert(TaskAnalysis analysis);

    /**
     * 插入分析记录
     */
    @Insert("INSERT INTO task_analysises (task_id, total_submitted, avg_score, max_score, " +
            "min_score, avg_time_spent) " +
            "VALUES (#{taskId}, #{totalSubmitted}, #{avgScore}, #{maxScore}, " +
            "#{minScore}, #{avgTimeSpent})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    Long insert(TaskAnalysis analysis);

    /**
     * 根据ID删除分析记录
     */
    @Delete("DELETE FROM task_analysises WHERE id = #{id}")
    Long deleteById(Long id);

    /**
     * 根据任务ID删除分析记录
     */
    @Delete("DELETE FROM task_analysises WHERE task_id = #{taskId}")
    Long deleteByTaskId(Long taskId);

    /**
     * 更新分析记录
     */
    @Update("UPDATE task_analysises SET " +
            "task_id = #{taskId}, " +
            "total_submitted = #{totalSubmitted}, " +
            "avg_score = #{avgScore}, " +
            "max_score = #{maxScore}, " +
            "min_score = #{minScore}, " +
            "avg_time_spent = #{avgTimeSpent} " +
            "WHERE id = #{id}")
    Long update(TaskAnalysis analysis);

    /**
     * 根据任务ID更新分析记录（用于重新计算）
     */
    @Update("UPDATE task_analysises SET " +
            "total_submitted = #{totalSubmitted}, " +
            "avg_score = #{avgScore}, " +
            "max_score = #{maxScore}, " +
            "min_score = #{minScore}, " +
            "avg_time_spent = #{avgTimeSpent}, " +
            "updated_at = #{updatedAt} " +
            "WHERE task_id = #{taskId}")
    Long updateByTaskId(TaskAnalysis analysis);

    /**
     * 根据ID查询分析记录
     */
    @Select("SELECT * FROM task_analysises WHERE id = #{id}")
    TaskAnalysis findById(Long id);

    /**
     * 根据任务ID查询分析记录
     */
    @Select("SELECT * FROM task_analysises WHERE task_id = #{taskId}")
    TaskAnalysis findByTaskId(Long taskId);

    /**
     * 查询所有分析记录
     */
    @Select("SELECT * FROM task_analysises ORDER BY updated_at DESC")
    List<TaskAnalysis> findAll();

    /**
     * 查询任务的提交分析统计（从学生答案表实时计算）
     */
    @Select("SELECT " +
            "COUNT(DISTINCT ss.id) as total_submitted, " +
            "COALESCE(AVG(sa.score), 0) as avg_score, " +
            "COALESCE(MAX(sa.score), 0) as max_score, " +
            "COALESCE(MIN(sa.score), 0) as min_score, " +
            "COALESCE(AVG(sa.time_spent), 0) as avg_time_spent " +
            "FROM student_submissions ss " +
            "LEFT JOIN student_answers sa ON ss.id = sa.submission_id " +
            "WHERE ss.task_id = #{taskId} AND ss.status = 'passed'")
    TaskAnalysis calculateStatsByTaskId(Long taskId);

    /**
     * 检查任务是否有分析记录
     */
    @Select("SELECT COUNT(*) > 0 FROM task_analysises WHERE task_id = #{taskId}")
    boolean existsByTaskId(Long taskId);

    /**
     * 统计分析记录总数
     */
    @Select("SELECT COUNT(*) FROM task_analysises")
    Long countAll();

    /**
     * 统计已分析的任务数量
     */
    @Select("SELECT COUNT(DISTINCT task_id) FROM task_analysises")
    Long countAnalyzedTasks();

}