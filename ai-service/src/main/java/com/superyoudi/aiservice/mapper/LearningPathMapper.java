package com.superyoudi.aiservice.mapper;

import com.superyoudi.aiservice.pojo.entity.LearningPath;
import com.superyoudi.aiservice.pojo.entity.LearningPathNode;
import com.superyoudi.common.dto.TodayTaskRawDTO;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author Sijayyx
 */
@Mapper
public interface LearningPathMapper {

    @Select("SELECT * FROM learning_paths WHERE id = #{id}")
    LearningPath selectById(Long id);

    /**
     * 根据学生ID和课程ID查询
     */
    @Select("SELECT * FROM learning_paths WHERE student_id = #{studentId} AND course_id = #{courseId}")
    LearningPath selectByStudentAndCourse(@Param("studentId") Long studentId, @Param("courseId") Long courseId);

    /**
     * 根据学生ID查询所有学习路径
     */
    @Select("SELECT * FROM learning_paths WHERE student_id = #{studentId}")
    List<LearningPath> selectByStudentId(Long studentId);

    /**
     * 根据课程ID查询所有学习路径
     */
    @Select("SELECT * FROM learning_paths WHERE course_id = #{courseId}")
    List<LearningPath> selectByCourseId(Long courseId);

    /**
     * 根据当前节点ID查询学习路径
     */
    @Select("SELECT * FROM learning_paths WHERE current_node_id = #{nodeId}")
    List<LearningPath> selectByCurrentNodeId(Long nodeId);

    /**
     * 根据当前阶段查询学习路径
     */
    @Select("SELECT * FROM learning_paths WHERE current_stage = #{currentStage}")
    List<LearningPath> selectByCurrentStage(String currentStage);

    /**
     * 查询所有学习路径
     */
    @Select("SELECT * FROM learning_paths")
    List<LearningPath> selectAll();

    /**
     * 插入学习路径
     */
    @Insert("INSERT INTO learning_paths (student_id, course_id, current_node_id, current_stage, additional_info, created_at, updated_at) " +
            "VALUES (#{studentId}, #{courseId}, #{currentNodeId}, #{currentStage}, #{additionalInfo}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    Long insert(LearningPath learningPath);

    /**
     * 更新学习路径
     */
    @Update("UPDATE learning_paths SET " +
            "current_node_id = #{currentNodeId}, " +
            "current_stage = #{currentStage}, " +
            "additional_info = #{additionalInfo}, " +
            "updated_at = #{updatedAt} " +
            "WHERE id = #{id}")
    Long update(LearningPath learningPath);

    /**
     * 更新当前学习节点
     */
    @Update("UPDATE learning_paths SET current_node_id = #{currentNodeId}, updated_at = NOW() WHERE id = #{id}")
    Long updateCurrentNode(@Param("id") Long id, @Param("currentNodeId") Long currentNodeId);

    /**
     * 更新当前教学阶段
     */
    @Update("UPDATE learning_paths SET current_stage = #{currentStage}, updated_at = NOW() WHERE id = #{id}")
    Long updateCurrentStage(@Param("id") Long id, @Param("currentStage") String currentStage);

    /**
     * 同时更新当前节点和阶段
     */
    @Update("UPDATE learning_paths SET " +
            "current_node_id = #{currentNodeId}, " +
            "current_stage = #{currentStage}, " +
            "updated_at = NOW() " +
            "WHERE id = #{id}")
    Long updateCurrentNodeAndStage(@Param("id") Long id,
                                   @Param("currentNodeId") Long currentNodeId,
                                   @Param("currentStage") String currentStage);

    /**
     * 标记学习路径为已完成
     */
    @Update("UPDATE learning_paths SET current_stage = 'finished', updated_at = NOW() WHERE id = #{id}")
    Long markAsFinished(Long id);

    /**
     * 根据学生ID和课程ID删除学习路径
     */
    @Delete("DELETE FROM learning_paths WHERE student_id = #{studentId} AND course_id = #{courseId}")
    Long deleteByStudentAndCourse(@Param("studentId") Long studentId, @Param("courseId") Long courseId);

    /**
     * 根据ID删除学习路径
     */
    @Delete("DELETE FROM learning_paths WHERE id = #{id}")
    Long deleteById(Long id);

    /**
     * 根据学生ID删除所有学习路径
     */
    @Delete("DELETE FROM learning_paths WHERE student_id = #{studentId}")
    Long deleteByStudentId(Long studentId);

    /**
     * 根据课程ID删除所有学习路径
     */
    @Delete("DELETE FROM learning_paths WHERE course_id = #{courseId}")
    Long deleteByCourseId(Long courseId);

    List<TodayTaskRawDTO> selectTaskRawByStudentId(Long studentId);


    /**
     * 根据学生id 查询【最近更新】的学习路径
     */
    @Select("SELECT id, student_id, course_id, updated_at " +
            "FROM learning_paths " +
            "WHERE student_id = #{studentId} " +
            "ORDER BY updated_at DESC LIMIT 1")
    LearningPath selectLatestPathByStudentId(Long studentId);

    /**
     * 根据学习路径ID查询第一个学习路径节点
     */
    @Select("SELECT * FROM learning_path_nodes WHERE path_id = #{pathId} ORDER BY id ASC LIMIT 1")
    LearningPathNode findFirstNodeByPathId(Long pathId);
}