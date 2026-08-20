package com.superyoudi.businessservice.mapper;

import com.superyoudi.businessservice.pojo.entity.QuestionKnowledgeRel;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author Sijayyx
 */
@Mapper
public interface QuestionKnowledgeRelMapper {

    /**
     * 插入关联记录
     */
    @Insert("INSERT INTO question_knowledge_rel (question_id, knowledge_node, sort) " +
            "VALUES (#{questionId}, #{knowledgeNode}, #{sort})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    Long insert(QuestionKnowledgeRel rel);


    /**
     * 根据ID删除关联记录
     */
    @Delete("DELETE FROM question_knowledge_rel WHERE id = #{id}")
    Long deleteById(Long id);

    /**
     * 根据题目ID删除所有关联
     */
    @Delete("DELETE FROM question_knowledge_rel WHERE question_id = #{questionId}")
    Long deleteByQuestionId(Long questionId);

    /**
     * 根据知识点ID删除所有关联
     */
    @Delete("DELETE FROM question_knowledge_rel WHERE knowledge_node = #{knowledgeId}")
    Long deleteByKnowledgeNode(String knowledgeId);

    /**
     * 根据ID更新关联记录
     */
    @Update("UPDATE question_knowledge_rel SET " +
            "question_id = #{questionId}, " +
            "knowledge_node = #{knowledgeNode}, " +
            "sort = #{sort} " +
            "WHERE id = #{id}")
    Long update(QuestionKnowledgeRel rel);

    /**
     * 更新排序
     */
    @Update("UPDATE question_knowledge_rel SET sort = #{sort} WHERE id = #{id}")
    Long updateSort(@Param("id") Long id, @Param("sort") Integer sort);

    /**
     * 根据ID查询关联记录
     */
    @Select("SELECT * FROM question_knowledge_rel WHERE id = #{id}")
    QuestionKnowledgeRel findById(Long id);

    /**
     * 查询所有关联记录
     */
    @Select("SELECT * FROM question_knowledge_rel ORDER BY sort ASC")
    List<QuestionKnowledgeRel> findAll();


}
