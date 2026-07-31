package com.superyoudi.businessservice.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface LearningPathNodesMapper {

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