package com.superyoudi.businessservice.mapper;

import com.superyoudi.businessservice.pojo.entity.Task;
import com.superyoudi.businessservice.pojo.entity.TaskList;
import com.superyoudi.common.dto.ErrorQuestionVO;
import org.apache.ibatis.annotations.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Mapper
public interface TaskMapper {

    @Select("SELECT id FROM tasks WHERE status IN ('published','closed')")
    List<Long> selectActiveTaskIds();

    @Select("SELECT COUNT(*) FROM tasks WHERE course_id = #{courseId} AND status IN ('published','closed')")
    Long countPublishedTasksByCourseId(@Param("courseId") Long courseId);
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
    @Select("""
            SELECT * \
            FROM tasks WHERE id = #{id}""")
    Task selectById(@Param("id") Long id);

    /**
     * 根据课程ID查询所有任务（按开始时间降序）
     */
    @Select("SELECT * " +
            "FROM tasks WHERE course_id = #{courseId} ORDER BY start_time DESC")
    List<Task> selectByCourseId(@Param("courseId") Long courseId);

    /**
     * 根据课程ID分页查询任务（游标分页，ID大于lastId）
     */
    @Select("SELECT * " +
            "FROM tasks WHERE course_id = #{courseId} AND id > #{lastId} " +
            "ORDER BY id ASC LIMIT #{limit}")
    List<Task> selectByCourseIdWithCursor(@Param("courseId") Long courseId,
                                          @Param("lastId") Long lastId,
                                          @Param("limit") Integer limit);

    /**
     * 根据布置者ID查询任务
     */
    @Select("SELECT * " +
            "FROM tasks WHERE assigner_id = #{assignerId} ORDER BY created_at DESC")
    List<Task> selectByAssignerId(@Param("assignerId") Long assignerId);

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

    @Select("select count(*) from task_student_rel where student_id = #{studentId}")
    Integer countStudentAllTask(Long studentId);

    List<ErrorQuestionVO> selectUserErrorQuestion(Long studentId, Long courseId);

    List<TaskList> selectTeacherTaskPage(Pageable pageable, Long userId, Long courseId, String status);

    List<TaskList> selectStudentTaskPage(Pageable pageable, Long userId, Long courseId, String status);

    Long countTeacherTask(@Param("userId") Long userId, @Param("courseId") Long courseId, @Param("status") String status);

    Long countStudentTask(@Param("userId") Long userId, @Param("courseId") Long courseId, @Param("status") String status);
}
