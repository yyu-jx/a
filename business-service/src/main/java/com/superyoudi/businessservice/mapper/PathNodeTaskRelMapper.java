package com.superyoudi.businessservice.mapper;

import com.superyoudi.businessservice.pojo.entity.PathNodeTaskRel;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author Sijayyx
 */
@Mapper
public interface PathNodeTaskRelMapper {

    /**
     * 插入关联记录
     */
    @Insert("INSERT INTO path_node_task_rel (path_node_id, student_id, task_id) " +
            "VALUES (#{pathNodeId}, #{studentId}, #{taskId})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    Long insert(PathNodeTaskRel rel);


    /**
     * 根据ID删除关联记录
     */
    @Delete("DELETE FROM path_node_task_rel WHERE id = #{id}")
    Long deleteById(Long id);

    /**
     * 根据路径节点ID删除所有关联
     */
    @Delete("DELETE FROM path_node_task_rel WHERE path_node_id = #{pathNodeId}")
    Long deleteByPathNodeId(Long pathNodeId);

    /**
     * 根据任务ID删除所有关联
     */
    @Delete("DELETE FROM path_node_task_rel WHERE task_id = #{taskId}")
    Long deleteByTaskId(Long taskId);

    /**
     * 根据ID查询关联记录
     */
    @Select("SELECT * FROM path_node_task_rel WHERE id = #{id}")
    PathNodeTaskRel findById(Long id);

    /**
     * 根据路径节点ID查询所有关联的任务ID
     */
    @Select("SELECT task_id FROM path_node_task_rel WHERE path_node_id = #{pathNodeId} ORDER BY create_at ASC")
    List<Long> findTaskIdsByPathNodeId(Long pathNodeId);

    /**
     * 根据路径节点ID查询关联记录
     */
    @Select("SELECT * FROM path_node_task_rel WHERE path_node_id = #{pathNodeId} ORDER BY create_at  ")
    PathNodeTaskRel findByPathNodeId(Long pathNodeId);

    /**
     * 根据任务ID查询所有关联的路径节点ID
     */
    @Select("SELECT path_node_id FROM path_node_task_rel WHERE task_id = #{taskId}")
    List<Long> findPathNodeIdsByTaskId(Long taskId);

    /**
     * 检查关联是否存在
     */
    @Select("SELECT COUNT(*) > 0 FROM path_node_task_rel " +
            "WHERE path_node_id = #{pathNodeId} AND task_id = #{taskId}")
    boolean existsByPathNodeAndTask(@Param("pathNodeId") Long pathNodeId,
                                    @Param("taskId") Long taskId);

    /**
     * 根据路径节点ID和任务ID删除关联
     */
    @Delete("DELETE FROM path_node_task_rel " +
            "WHERE path_node_id = #{pathNodeId} AND task_id = #{taskId}")
    Long deleteByPathNodeAndTask(@Param("pathNodeId") Long pathNodeId,
                                 @Param("taskId") Long taskId);

    /**
     * 统计路径节点关联的任务数量
     */
    @Select("SELECT COUNT(*) FROM path_node_task_rel WHERE path_node_id = #{pathNodeId}")
    Long countByPathNodeId(Long pathNodeId);

    /**
     * 统计任务被关联的路径节点数量
     */
    @Select("SELECT COUNT(*) FROM path_node_task_rel WHERE task_id = #{taskId}")
    Long countByTaskId(Long taskId);
}