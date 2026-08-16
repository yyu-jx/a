package com.superyoudi.businessservice.mapper;

import com.superyoudi.businessservice.pojo.entity.Task;
import com.superyoudi.common.dto.ErrorQuestionVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TaskMapper {
    /**
     * 插入任务
     */
    @Insert("INSERT INTO tasks (course_id, assigner_id, title, description, type, " +
            "total_score, start_time, deadline, status, after_submit_strategy) " +
            "VALUES (#{courseId}, #{assignerId}, #{title}, #{description}, #{type}, " +
            "#{totalScore}, #{startTime}, #{deadline}, #{status}, #{afterSubmitStrategy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    Long insert(Task task);


    /**
     * 根据ID删除
     */
    @Delete("DELETE FROM tasks WHERE id = #{id}")
    Long deleteById(@Param("id") Long id);

    /**
     * 根据课程ID删除所有任务
     */
    @Delete("DELETE FROM tasks WHERE course_id = #{courseId}")
    Long deleteByCourseId(@Param("courseId") Long courseId);

    /**
     * 根据ID查询
     */
    @Select("SELECT id, course_id, assigner_id, title, description, type, total_score, " +
            "start_time, deadline, status, after_submit_strategy, created_at, updated_at " +
            "FROM tasks WHERE id = #{id}")
    Task selectById(@Param("id") Long id);

    /**
     * 根据课程ID查询所有任务（按开始时间降序）
     */
    @Select("SELECT id, course_id, assigner_id, title, description, type, total_score, " +
            "start_time, deadline, status, after_submit_strategy, created_at, updated_at " +
            "FROM tasks WHERE course_id = #{courseId} ORDER BY start_time DESC")
    List<Task> selectByCourseId(@Param("courseId") Long courseId);

    /**
     * 根据课程ID分页查询任务（游标分页，ID大于lastId）
     */
    @Select("SELECT id, course_id, assigner_id, title, description, type, total_score, " +
            "start_time, deadline, status, after_submit_strategy, created_at, updated_at " +
            "FROM tasks WHERE course_id = #{courseId} AND id > #{lastId} " +
            "ORDER BY id ASC LIMIT #{limit}")
    List<Task> selectByCourseIdWithCursor(@Param("courseId") Long courseId,
                                          @Param("lastId") Long lastId,
                                          @Param("limit") Integer limit);

    /**
     * 根据布置者ID查询任务
     */
    @Select("SELECT id, course_id, assigner_id, title, description, type, total_score, " +
            "start_time, deadline, status, after_submit_strategy, created_at, updated_at " +
            "FROM tasks WHERE assigner_id = #{assignerId} ORDER BY created_at DESC")
    List<Task> selectByAssignerId(@Param("assignerId") Long assignerId);

    /**
     * 根据状态查询任务
     */
    @Select("SELECT id, course_id, assigner_id, title, description, type, total_score, " +
            "start_time, deadline, status, after_submit_strategy, created_at, updated_at " +
            "FROM tasks WHERE status = #{status} ORDER BY created_at DESC")
    List<Task> selectByStatus(@Param("status") String status);

    /**
     * 根据课程ID和状态查询任务
     */
    @Select("SELECT id, course_id, assigner_id, title, description, type, total_score, " +
            "start_time, deadline, status, after_submit_strategy, created_at, updated_at " +
            "FROM tasks WHERE course_id = #{courseId} AND status = #{status} " +
            "ORDER BY start_time DESC")
    List<Task> selectByCourseIdAndStatus(@Param("courseId") Long courseId,
                                         @Param("status") String status);

    /**
     * 查询进行中的任务（已发布且当前时间在开始时间和截止时间之间，deadline可为空）
     */
    @Select("SELECT id, course_id, assigner_id, title, description, type, total_score, " +
            "start_time, deadline, status, after_submit_strategy, created_at, updated_at " +
            "FROM tasks WHERE status = 'published' " +
            "AND start_time <= NOW() " +
            "AND (deadline IS NULL OR deadline >= NOW()) " +
            "ORDER BY deadline ASC")
    List<Task> selectActiveTasks();

    /**
     * 查询已截止的任务（已发布且截止时间已过，且deadline不为空）
     */
    @Select("SELECT id, course_id, assigner_id, title, description, type, total_score, " +
            "start_time, deadline, status, after_submit_strategy, created_at, updated_at " +
            "FROM tasks WHERE status = 'published' AND deadline IS NOT NULL AND deadline < NOW() " +
            "ORDER BY deadline ASC")
    List<Task> selectExpiredTasks();

    /**
     * 查询未开始的任务（已发布且开始时间未到）
     */
    @Select("SELECT id, course_id, assigner_id, title, description, type, total_score, " +
            "start_time, deadline, status, after_submit_strategy, created_at, updated_at " +
            "FROM tasks WHERE status = 'published' AND start_time > NOW() " +
            "ORDER BY start_time ASC")
    List<Task> selectUpcomingTasks();

    @Update("""
            UPDATE tasks SET
            course_id = #{courseId},
            assigner_id = #{assignerId},
            title = #{title},
            description = #{description},
            type = #{type},
            total_score = #{totalScore},
            start_time = #{startTime},
            deadline = #{deadline},
            status = #{status},
            after_submit_strategy = #{afterSubmitStrategy},
            updated_at = NOW()
            WHERE id = #{id}""")
    Long update(Task task);

    /**
     * 更新任务状态
     */
    @Update("UPDATE tasks SET status = #{status} WHERE id = #{id}")
    Long updateStatus(@Param("id") Long id, @Param("status") String status);

    /**
     * 更新任务状态并关闭（设置status为closed）
     */
    @Update("UPDATE tasks SET status = 'closed' WHERE id = #{id}")
    Long closeTask(@Param("id") Long id);

    /**
     * 统计课程下的任务总数
     */
    @Select("SELECT COUNT(*) FROM tasks WHERE course_id = #{courseId}")
    Long countByCourseId(@Param("courseId") Long courseId);

    /**
     * 统计课程下特定状态的任务数
     */
    @Select("SELECT COUNT(*) FROM tasks WHERE course_id = #{courseId} AND status = #{status}")
    Long countByCourseIdAndStatus(@Param("courseId") Long courseId,
                                  @Param("status") String status);


    /**
     * 查询某课程下已发布且提交策略为自动评分的任务
     */
    @Select("SELECT id, course_id, assigner_id, title, description, type, total_score, " +
            "start_time, deadline, status, after_submit_strategy, created_at, updated_at " +
            "FROM tasks WHERE course_id = #{courseId} " +
            "AND status = 'published' " +
            "AND after_submit_strategy IN ('auto_all', 'auto_objective') " +
            "ORDER BY deadline ASC")
    List<Task> selectAutoScoringTasks(@Param("courseId") Long courseId);


    /**
     * 根据任务类型查询
     */
    @Select("SELECT id, course_id, assigner_id, title, description, type, total_score, " +
            "start_time, deadline, status, after_submit_strategy, created_at, updated_at " +
            "FROM tasks WHERE type = #{type} AND status = 'published' " +
            "ORDER BY created_at DESC")
    List<Task> selectByType(@Param("type") String type);

    /**
     * 根据课程ID和任务类型查询
     */
    @Select("SELECT id, course_id, assigner_id, title, description, type, total_score, " +
            "start_time, deadline, status, after_submit_strategy, created_at, updated_at " +
            "FROM tasks WHERE course_id = #{courseId} AND type = #{type} " +
            "ORDER BY created_at DESC")
    List<Task> selectByCourseIdAndType(@Param("courseId") Long courseId,
                                       @Param("type") String type);

    /**
     * 查询某课程下已发布且未截止的任务（供学生端展示）
     */
    @Select("SELECT id, course_id, assigner_id, title, description, type, total_score, " +
            "start_time, deadline, status, after_submit_strategy, created_at, updated_at " +
            "FROM tasks WHERE course_id = #{courseId} " +
            "AND status = 'published' " +
            "AND start_time <= NOW() " +
            "AND (deadline IS NULL OR deadline >= NOW()) " +
            "ORDER BY deadline ASC")
    List<Task> selectAvailableTasks(@Param("courseId") Long courseId);

    @Select("select count(*) from task_student_rel where student_id = #{studentId}")
    Integer countStudentAllTask(Long studentId);

    List<ErrorQuestionVO> selectUserErrorQuestion(Long studentId, Long courseId);
}
