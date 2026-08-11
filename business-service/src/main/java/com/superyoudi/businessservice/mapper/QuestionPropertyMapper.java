package com.superyoudi.businessservice.mapper;

import com.superyoudi.businessservice.pojo.entity.QuestionProperty;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author Sijayyx
 */
@Mapper
public interface QuestionPropertyMapper {

    /**
     * 插入题目属性
     */
    @Insert("INSERT INTO question_properties (question_id, analysis, source, difficulty) " +
            "VALUES (#{questionId}, #{analysis}, #{source}, #{difficulty})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    Long insert(QuestionProperty questionProperty);

    /**
     * 根据ID删除题目属性
     */
    @Delete("DELETE FROM question_properties WHERE id = #{id}")
    Long deleteById(Long id);

    /**
     * 根据题目ID删除属性
     */
    @Delete("DELETE FROM question_properties WHERE question_id = #{questionId}")
    Long deleteByQuestionId(Long questionId);

    /**
     * 根据ID更新题目属性
     */
    @Update("UPDATE question_properties SET " +
            "question_id = #{questionId}, " +
            "analysis = #{analysis}, " +
            "source = #{source}, " +
            "difficulty = #{difficulty} " +
            "WHERE id = #{id}")
    Long update(QuestionProperty questionProperty);

    /**
     * 根据ID查询题目属性
     */
    @Select("SELECT * FROM question_properties WHERE id = #{id}")
    QuestionProperty findById(Long id);

    /**
     * 根据题目ID查询属性
     */
    @Select("SELECT * FROM question_properties WHERE question_id = #{questionId}")
    QuestionProperty findByQuestionId(Long questionId);

    /**
     * 查询所有题目属性
     */
    @Select("SELECT * FROM question_properties")
    List<QuestionProperty> findAll();

    /**
     * 根据来源查询
     */
    @Select("SELECT * FROM question_properties WHERE source = #{source}")
    List<QuestionProperty> findBySource(String source);

    /**
     * 根据难度查询
     */
    @Select("SELECT * FROM question_properties WHERE difficulty = #{difficulty}")
    List<QuestionProperty> findByDifficulty(String difficulty);

    /**
     * 根据来源和难度查询
     */
    @Select("SELECT * FROM question_properties WHERE source = #{source} AND difficulty = #{difficulty}")
    List<QuestionProperty> findBySourceAndDifficulty(@Param("source") String source,
                                                     @Param("difficulty") String difficulty);


    /**
     * 统计各来源的数量
     */
    @Select("SELECT source, COUNT(*) as count FROM question_properties GROUP BY source")
    List<java.util.Map<String, Object>> countBySource();

    /**
     * 统计各难度的数量
     */
    @Select("SELECT difficulty, COUNT(*) as count FROM question_properties GROUP BY difficulty")
    List<java.util.Map<String, Object>> countByDifficulty();

    /**
     * 统计指定来源的各难度数量
     */
    @Select("SELECT difficulty, COUNT(*) as count FROM question_properties " +
            "WHERE source = #{source} GROUP BY difficulty")
    List<java.util.Map<String, Object>> countBySourceAndDifficulty(String source);


}
