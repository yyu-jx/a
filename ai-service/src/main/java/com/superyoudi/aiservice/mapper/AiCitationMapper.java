package com.superyoudi.aiservice.mapper;

import com.superyoudi.aiservice.pojo.dto.AiCitation;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author Sijayyx
 */
@Mapper
public interface AiCitationMapper {

    /**
     * 插入引用记录
     */
    @Insert("INSERT INTO ai_citations (chat_message_id, suggestion_id, title, data_type, data) " +
            "VALUES (#{chatMessageId}, #{suggestionsId}, #{title}, #{dataType}, #{data})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AiCitation citation);


    /**
     * 根据ID查询引用记录
     */
    @Select("SELECT * FROM ai_citations WHERE id = #{id}")
    AiCitation selectById(Long id);

    /**
     * 根据对话消息ID查询引用列表
     */
    @Select("SELECT * FROM ai_citations WHERE chat_message_id = #{chatMessageId} ORDER BY id ASC")
    List<AiCitation> selectByChatMessageId(Long chatMessageId);

    /**
     * 根据AI建议ID查询引用列表
     */
    @Select("SELECT * FROM ai_citations WHERE suggestion_id = #{suggestionsId} ORDER BY id ASC")
    List<AiCitation> selectBySuggestionsId(Long suggestionsId);

    /**
     * 根据数据类型查询引用列表
     */
    @Select("SELECT * FROM ai_citations WHERE data_type = #{dataType} ORDER BY id DESC")
    List<AiCitation> selectByDataType(@Param("dataType") String dataType);


    /**
     * 根据ID删除引用记录
     */
    @Delete("DELETE FROM ai_citations WHERE id = #{id}")
    int deleteById(Long id);

    /**
     * 根据对话消息ID删除所有引用
     */
    @Delete("DELETE FROM ai_citations WHERE chat_message_id = #{chatMessageId}")
    int deleteByChatMessageId(Long chatMessageId);

    /**
     * 根据AI建议ID删除所有引用
     */
    @Delete("DELETE FROM ai_citations WHERE suggestion_id = #{suggestionsId}")
    int deleteBySuggestionsId(Long suggestionsId);


}