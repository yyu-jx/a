package com.superyoudi.aiservice.mapper;

import com.superyoudi.aiservice.pojo.entity.LearningPathNode;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author Sijayyx
 */
@Mapper
public interface LearningPathNodeMapper {

    @Select("""
            SELECT COUNT(*)
            FROM learning_path_nodes n
            JOIN learning_paths p ON p.id = n.path_id
            WHERE p.student_id = #{studentId} AND p.course_id = #{courseId}
            """)
    Long countNodes(@Param("studentId") Long studentId, @Param("courseId") Long courseId);

    @Select("""
            SELECT COUNT(*)
            FROM learning_path_nodes n
            JOIN learning_paths p ON p.id = n.path_id
            WHERE p.student_id = #{studentId} AND p.course_id = #{courseId}
              AND n.status = 'completed'
            """)
    Long countCompletedNodes(@Param("studentId") Long studentId, @Param("courseId") Long courseId);

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
     * 根据知识点ID查询所有关联节点（单个知识点）
     */
    @Select("SELECT * FROM learning_path_nodes WHERE JSON_CONTAINS(knowledge_ids, JSON_QUOTE(#{knowledgeId}))")
    List<LearningPathNode> selectByKnowledgeId(String knowledgeId);

    /**
     * 根据路径ID和知识点ID查询唯一节点（单个知识点）
     */
    @Select("SELECT * FROM learning_path_nodes WHERE path_id = #{pathId} AND JSON_CONTAINS(knowledge_ids, JSON_QUOTE(#{knowledgeId}))")
    LearningPathNode selectByPathIdAndKnowledgeId(@Param("pathId") Long pathId, @Param("knowledgeId") String knowledgeId);

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
            "WHERE lp.student_id = #{studentId} AND JSON_CONTAINS(lpn.knowledge_ids, JSON_QUOTE(#{knowledgeId}))")
    LearningPathNode selectByStudentAndKnowledge(@Param("studentId") Long studentId, @Param("knowledgeId") String knowledgeId);

    /**
     * 插入节点
     */
    @Insert("INSERT INTO learning_path_nodes (path_id, node_name, knowledge_ids, status, related_task_id, created_at, updated_at) " +
            "VALUES (#{pathId}, #{nodeName}, #{knowledgeIds}, #{status}, #{relatedTaskId}, #{createdAt}, #{updatedAt})")
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
            "node_name = #{nodeName}, " +
            "knowledge_ids = #{knowledgeIds}, " +
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
    Long deleteAllByPathId(Long pathId);

    /**
     * 删除包含某个知识点的所有节点
     */
    @Delete("DELETE FROM learning_path_nodes WHERE JSON_CONTAINS(knowledge_ids, JSON_QUOTE(#{knowledgeId}))")
    Long deleteByKnowledgeId(String knowledgeId);

    /**
     * 获取进行中的知识点id（第一个进行中的节点）
     */
    @Select("SELECT knowledge_ids FROM learning_path_nodes " +
            "WHERE path_id = #{pathId} AND status = 'proceeding' LIMIT 1")
    String selectProceedingKnowledgeIds(@Param("pathId") Long pathId);

    /**
     * 获取进行中的知识点id（第一个进行中的节点）
     * 返回单个知识点ID（取JSON数组第一个）
     */
    @Select("SELECT JSON_EXTRACT(knowledge_ids, '$[0]') FROM learning_path_nodes " +
            "WHERE path_id = #{pathId} AND status = 'proceeding' LIMIT 1")
    String selectProceedingKnowledgeId(@Param("pathId") Long pathId);

    /**
     * 获取更新时间最新的知识点id
     */
    @Select("SELECT JSON_EXTRACT(knowledge_ids, '$[0]') FROM learning_path_nodes " +
            "WHERE path_id = #{pathId} ORDER BY updated_at DESC LIMIT 1")
    String selectLatestUpdateKnowledgeId(@Param("pathId") Long pathId);

    /**
     * 统计未完成节点数量 status != completed
     */
    @Select("SELECT COUNT(1) FROM learning_path_nodes " +
            "WHERE path_id = #{pathId} AND status != 'completed'")
    Integer countUntestedNode(@Param("pathId") Long pathId);
}