package com.superyoudi.aiservice.mapper;

import com.superyoudi.aiservice.pojo.entity.LearningPathNode;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author Sijayyx
 */
@Mapper
public interface LearningPathNodeMapper {

    @Select("SELECT * FROM learning_path_nodes WHERE id = #{id}")
    LearningPathNode selectById(Long id);

    /**
     * 根据学习路径ID查询所有节点
     */
    @Select("SELECT * FROM learning_path_nodes WHERE path_id = #{pathId} ORDER BY id ASC")
    List<LearningPathNode> selectByPathId(Long pathId);

    /**
     * 查询学习路径中下一个节点（按id升序）
     * 如果当前节点已是最后一个，返回当前节点
     */
    @Select("SELECT COALESCE(" +
            "(SELECT id FROM learning_path_nodes WHERE path_id = #{pathId} AND id > #{nodeId} ORDER BY id ASC LIMIT 1), " +
            "#{nodeId}" +
            ")")
    Long selectNextNodeId(@Param("pathId") Long pathId, @Param("nodeId") Long nodeId);

    /**
     * 根据知识点ID查询所有关联节点
     */
    @Select("SELECT * FROM learning_path_nodes WHERE knowledge_id = #{knowledgeId}")
    List<LearningPathNode> selectByKnowledgeId(Long knowledgeId);

    /**
     * 根据路径ID和知识点ID查询唯一节点
     */
    @Select("SELECT * FROM learning_path_nodes WHERE path_id = #{pathId} AND knowledge_id = #{knowledgeId}")
    LearningPathNode selectByPathIdAndKnowledgeId(@Param("pathId") Long pathId, @Param("knowledgeId") Long knowledgeId);

    /**
     * 根据状态查询节点
     */
    @Select("SELECT * FROM learning_path_nodes WHERE path_id = #{pathId} AND status = #{status}")
    List<LearningPathNode> selectByPathIdAndStatus(@Param("pathId") Long pathId, @Param("status") String status);

    /**
     * 根据作业ID查询关联节点
     */
    @Select("SELECT * FROM learning_path_nodes WHERE related_task_id = #{taskId}")
    LearningPathNode selectByRelatedTaskId(Long taskId);

    /**
     * 查询学生某个知识点的学习情况（跨学习路径）
     */
    @Select("SELECT lpn.* FROM learning_path_nodes lpn " +
            "INNER JOIN learning_paths lp ON lpn.path_id = lp.id " +
            "WHERE lp.student_id = #{studentId} AND lpn.knowledge_id = #{knowledgeId}")
    LearningPathNode selectByStudentAndKnowledge(@Param("studentId") Long studentId, @Param("knowledgeId") Long knowledgeId);

    /**
     * 插入节点
     */
    @Insert("INSERT INTO learning_path_nodes (path_id, knowledge_id, status, related_task_id, created_at, updated_at) " +
            "VALUES (#{pathId}, #{knowledgeId}, #{status}, #{relatedTaskId}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    Long insert(LearningPathNode learningPathNode);

    /**
     * 更新节点状态
     */
    @Update("UPDATE learning_path_nodes SET status = #{status}, updated_at = NOW() WHERE id = #{id}")
    Long updateStatus(@Param("id") Long id, @Param("status") String status);

    /**
     * 更新节点关联的作业ID
     */
    @Update("UPDATE learning_path_nodes SET related_task_id = #{relatedTaskId}, updated_at = NOW() WHERE id = #{id}")
    Long updateRelatedTask(@Param("id") Long id, @Param("relatedTaskId") Long relatedTaskId);

    /**
     * 标记节点为已完成
     */
    @Update("UPDATE learning_path_nodes SET status = 'completed', updated_at = NOW() WHERE id = #{id}")
    Long markAsCompleted(Long id);

    /**
     * 标记节点为进行中
     */
    @Update("UPDATE learning_path_nodes SET status = 'proceeding', updated_at = NOW() WHERE id = #{id}")
    Long markAsProceeding(Long id);

    /**
     * 更新节点
     */
    @Update("UPDATE learning_path_nodes SET " +
            "knowledge_id = #{knowledgeId}, " +
            "status = #{status}, " +
            "related_task_id = #{relatedTaskId}, " +
            "updated_at = NOW() " +
            "WHERE id = #{id}")
    Long update(LearningPathNode learningPathNode);

    /**
     * 根据ID删除节点
     */
    @Delete("DELETE FROM learning_path_nodes WHERE id = #{id}")
    Long deleteById(Long id);

    /**
     * 根据学习路径ID删除所有节点
     */
    @Delete("DELETE FROM learning_path_nodes WHERE path_id = #{pathId}")
    Long deleteByPathId(Long pathId);

    /**
     * 删除某个知识点在所有学习路径中的节点
     */
    @Delete("DELETE FROM learning_path_nodes WHERE knowledge_id = #{knowledgeId}")
    Long deleteByKnowledgeId(Long knowledgeId);

    /**
     * 获取进行中的知识点id proceeding
     */
    @Select("SELECT knowledge_id FROM learning_path_nodes " +
            "WHERE path_id = #{pathId} AND status = 'proceeding' LIMIT 1")
    String selectProceedingKnowledgeId(@Param("pathId") Long pathId);

    /**
     * 获取更新时间最新的知识点id
     */
    @Select("SELECT knowledge_id FROM learning_path_nodes " +
            "WHERE path_id = #{pathId} ORDER BY updated_at DESC LIMIT 1")
    String selectLatestUpdateKnowledgeId(@Param("pathId") Long pathId);

    /**
     * 统计未完成节点数量 status != completed
     */
    @Select("SELECT COUNT(1) FROM learning_path_nodes " +
            "WHERE path_id = #{pathId} AND status != 'completed'")
    Integer countUntestedNode(@Param("pathId") Long pathId);

}