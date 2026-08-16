package com.superyoudi.businessservice.mapper;

import com.superyoudi.businessservice.pojo.entity.TaskQuestionRel;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TaskQuestionRelMapper {
    /**
     * 插入任务题目关联
     */
    @Insert("INSERT INTO task_question_rel (task_id, question_id, score, question_sort) " +
            "VALUES (#{taskId}, #{questionId}, #{score}, #{questionSort})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    Long insert(TaskQuestionRel taskQuestionRel);

    /// see TaskQuestionRelMapper.xml -> batchInsert
    int batchInsert(@Param("list") List<TaskQuestionRel> list);

    /**
     * 根据ID删除任务题目关联
     */
    @Delete("DELETE FROM task_question_rel WHERE id = #{id}")
    Long deleteById(Long id);

    /**
     * 根据ID更新任务题目关联
     */
    @Update("UPDATE task_question_rel SET " +
            "task_id = #{taskId}, " +
            "question_id = #{questionId}, " +
            "score = #{score}, " +
            "question_sort = #{questionSort} " +
            "WHERE id = #{id}")
    Long update(TaskQuestionRel taskQuestionRel);

    /**
     * 根据ID查询任务题目关联
     */
    @Select("SELECT * FROM task_question_rel WHERE id = #{id}")
    TaskQuestionRel findById(Long id);

    /**
     * 查询所有任务题目关联
     */
    @Select("SELECT * FROM task_question_rel")
    List<TaskQuestionRel> findAll();

    /**
     * 根据任务ID查询题目关联列表
     */
    @Select("SELECT * FROM task_question_rel WHERE task_id = #{taskId} ORDER BY question_sort ASC")
    List<TaskQuestionRel> findByTaskId(Long taskId);

    /**
     * 根据题目ID查询任务关联列表
     */
    @Select("SELECT * FROM task_question_rel WHERE question_id = #{questionId}")
    List<TaskQuestionRel> findByQuestionId(Long questionId);

    /**
     * 根据任务ID和题目ID查询
     */
    @Select("SELECT * FROM task_question_rel WHERE task_id = #{taskId} AND question_id = #{questionId}")
    TaskQuestionRel findByTaskIdAndQuestionId(@Param("taskId") Long taskId,
                                              @Param("questionId") Long questionId);

    /**
     * 根据任务ID删除所有关联
     */
    @Delete("DELETE FROM task_question_rel WHERE task_id = #{taskId}")
    Long deleteAllByTaskId(Long taskId);

    /**
     * 根据题目ID删除所有关联
     */
    @Delete("DELETE FROM task_question_rel WHERE question_id = #{questionId}")
    Long deleteByQuestionId(Long questionId);

    /**
     * 统计任务下的题目数量
     */
    @Select("SELECT COUNT(*) FROM task_question_rel WHERE task_id = #{taskId}")
    Long countByTaskId(Long taskId);

    /**
     * 统计题目被多少任务引用
     */
    @Select("SELECT COUNT(*) FROM task_question_rel WHERE question_id = #{questionId}")
    Long countByQuestionId(Long questionId);
}
