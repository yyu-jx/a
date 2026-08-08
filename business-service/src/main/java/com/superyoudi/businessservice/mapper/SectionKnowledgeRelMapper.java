package com.superyoudi.businessservice.mapper;

/**
 * @author Sijayyx
 */

import com.superyoudi.businessservice.pojo.entity.SectionKnowledgeRel;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SectionKnowledgeRelMapper {

    @Select("SELECT * FROM section_knowledge_rel WHERE id = #{id}")
    SectionKnowledgeRel selectById(Long id);

    /**
     * 根据小节ID查询所有关联的知识点
     */
    @Select("SELECT * FROM section_knowledge_rel WHERE section_id = #{sectionId}")
    List<SectionKnowledgeRel> selectBySectionId(Long sectionId);

    /**
     * 根据课程ID查询所有关联的知识点
     */
    @Select("SELECT * FROM section_knowledge_rel WHERE course_id = #{courseId} LIMIT #{limit}")
    List<SectionKnowledgeRel> selectByCourseId(@Param("courseId") Long courseId,
                                                          @Param("limit") Integer limit);

    /**
     * 根据课程ID查询关联的知识点
     * <p>
     * 查询startId后的元素
     *
     * @param courseId 课程ID
     * @param startId  起始ID
     * @param limit    查询条数
     * @return 知识点列表
     */
    @Select("""
            SELECT * FROM section_knowledge_rel
            WHERE course_id = #{courseId} AND id > #{startId}
            ORDER BY id
            LIMIT #{limit}""")
    List<SectionKnowledgeRel> selectBehindStartByCourseId(@Param("courseId") Long courseId,
                                                          @Param("startId") Long startId,
                                                          @Param("limit") Integer limit);

    /**
     * 根据小节ID和知识点ID查询唯一关联
     */
    @Select("SELECT * FROM section_knowledge_rel WHERE section_id = #{sectionId} AND neo4j_kp_id = #{neo4jKpId}")
    SectionKnowledgeRel selectBySectionIdAndKpId(@Param("sectionId") Long sectionId, @Param("neo4jKpId") String neo4jKpId);

    /**
     * 统计小节关联的知识点数量
     */
    @Select("SELECT COUNT(*) FROM section_knowledge_rel WHERE section_id = #{sectionId}")
    Integer countBySectionId(Long sectionId);

    /**
     * 插入关联关系
     */
    @Insert("INSERT INTO section_knowledge_rel (course_id, section_id, neo4j_kp_id, created_at) " +
            "VALUES (#{courseId}, #{sectionId}, #{neo4jKpId}, #{createdTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    Long insert(SectionKnowledgeRel sectionKnowledgeRel);


    /**
     * 根据ID删除关联关系
     */
    @Delete("DELETE FROM section_knowledge_rel WHERE id = #{id}")
    Long deleteById(Long id);

    /**
     * 根据小节ID删除所有关联知识点
     */
    @Delete("DELETE FROM section_knowledge_rel WHERE section_id = #{sectionId}")
    Long deleteBySectionId(Long sectionId);

    /**
     * 根据小节ID和知识点ID删除关联
     */
    @Delete("DELETE FROM section_knowledge_rel WHERE section_id = #{sectionId} AND neo4j_kp_id = #{neo4jKpId}")
    Long deleteBySectionIdAndKpId(@Param("sectionId") Long sectionId, @Param("neo4jKpId") String neo4jKpId);

    /**
     * 根据Neo4j知识点ID删除所有关联
     */
    @Delete("DELETE FROM section_knowledge_rel WHERE neo4j_kp_id = #{neo4jKpId}")
    Long deleteByNeo4jKpId(String neo4jKpId);
}