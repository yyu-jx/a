package com.superyoudi.coursewareservice.mapper;

import com.superyoudi.coursewareservice.pojo.entity.CoursewareRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CoursewareRecordMapper {

    /**
     * 插入记录
     * 返回受影响的行数
     */
    @Insert("INSERT INTO courseware_records (user_id, chat_id, ppt_file_id, word_file_id, ppt_html, word_html) " +
            "VALUES (#{userId}, #{chatId}, #{pptFileId}, #{wordFileId}, #{pptHtml}, #{wordHtml})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    Long insert(CoursewareRecord record);

    /**
     * 根据ID删除记录
     * 返回受影响的行数
     */
    @Delete("DELETE FROM courseware_records WHERE id = #{id}")
    Long deleteById(Long id);

    /**
     * 根据ID更新记录
     * 返回受影响的行数
     */
    @Update("UPDATE courseware_records SET " +
            "user_id = #{userId}, " +
            "chat_id = #{chatId}, " +
            "ppt_file_id = #{pptFileId}, " +
            "word_file_id = #{wordFileId}, " +
            "ppt_html = #{pptHtml}, " +
            "word_html = #{wordHtml} " +
            "WHERE id = #{id}")
    Long update(CoursewareRecord record);

    /**
     * 根据ID查询记录
     */
    @Select("SELECT id, user_id, chat_id, ppt_file_id, word_file_id, ppt_html, word_html, created_at, updated_at " +
            "FROM courseware_records WHERE id = #{id}")
    @Results(id = "recordResultMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "chatId", column = "chat_id"),
            @Result(property = "pptFileId", column = "ppt_file_id"),
            @Result(property = "wordFileId", column = "word_file_id"),
            @Result(property = "pptHtml", column = "ppt_html"),
            @Result(property = "wordHtml", column = "word_html"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    CoursewareRecord findById(Long id);

    /**
     * 查询所有记录
     */
    @Select("SELECT id, user_id, chat_id, ppt_file_id, word_file_id, ppt_html, word_html, created_at, updated_at " +
            "FROM courseware_records")
    @ResultMap("recordResultMap")
    List<CoursewareRecord> findAll();

    /**
     * 根据用户ID查询记录列表
     */
    @Select("SELECT id, user_id, chat_id, ppt_file_id, word_file_id, ppt_html, word_html, created_at, updated_at " +
            "FROM courseware_records WHERE user_id = #{userId}")
    @ResultMap("recordResultMap")
    List<CoursewareRecord> findByUserId(Long userId);

    /**
     * 根据对话ID查询记录
     */
    @Select("SELECT id, user_id, chat_id, ppt_file_id, word_file_id, ppt_html, word_html, created_at, updated_at " +
            "FROM courseware_records WHERE chat_id = #{chatId}")
    @ResultMap("recordResultMap")
    CoursewareRecord findByChatId(Long chatId);
}