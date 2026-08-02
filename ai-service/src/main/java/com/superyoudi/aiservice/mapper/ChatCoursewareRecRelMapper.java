package com.superyoudi.aiservice.mapper;

import com.superyoudi.aiservice.pojo.entity.ChatCoursewareRecRel;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author Sijayyx
 */
@Mapper
public interface ChatCoursewareRecRelMapper {

    @Select("SELECT * FROM chat_courseware_rec_rel WHERE id = #{id}")
    ChatCoursewareRecRel selectById(Long id);

    /**
     * 根据会话ID查询（一对一关系）
     */
    @Select("SELECT * FROM chat_courseware_rec_rel WHERE chat_id = #{chatId}")
    ChatCoursewareRecRel selectByChatId(Long chatId);

    /**
     * 根据课件生成记录ID查询（一对一关系）
     */
    @Select("SELECT * FROM chat_courseware_rec_rel WHERE courseware_record_id = #{coursewareRecordId}")
    ChatCoursewareRecRel selectByCoursewareRecordId(Long coursewareRecordId);

    /**
     * 根据教师ID查询列表（一对多关系）
     */
    @Select("SELECT * FROM chat_courseware_rec_rel WHERE user_id = #{userId}")
    List<ChatCoursewareRecRel> selectByUserId(Long userId);

    /**
     * 查询所有记录
     */
    @Select("SELECT * FROM chat_courseware_rec_rel")
    List<ChatCoursewareRecRel> selectAll();

    /**
     * 插入关联记录
     */
    @Insert("INSERT INTO chat_courseware_rec_rel (chat_id, courseware_record_id, user_id) " +
            "VALUES (#{chatId}, #{coursewareRecordId}, #{userId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    Long insert(ChatCoursewareRecRel chatCoursewareRecRel);

    /**
     * 更新关联记录
     */
    @Update("UPDATE chat_courseware_rec_rel " +
            "SET courseware_record_id = #{coursewareRecordId}, user_id = #{userId} " +
            "WHERE chat_id = #{chatId}")
    Long updateByChatId(ChatCoursewareRecRel chatCoursewareRecRel);

    /**
     * 根据ID删除
     */
    @Delete("DELETE FROM chat_courseware_rec_rel WHERE id = #{id}")
    Long deleteById(Long id);

    /**
     * 根据会话ID删除
     */
    @Delete("DELETE FROM chat_courseware_rec_rel WHERE chat_id = #{chatId}")
    Long deleteByChatId(Long chatId);

    /**
     * 根据课件生成记录ID删除
     */
    @Delete("DELETE FROM chat_courseware_rec_rel WHERE courseware_record_id = #{coursewareRecordId}")
    Long deleteByCoursewareRecordId(Long coursewareRecordId);

    /**
     * 根据教师ID删除
     */
    @Delete("DELETE FROM chat_courseware_rec_rel WHERE user_id = #{userId}")
    Long deleteByUserId(Long userId);
}