package com.superyoudi.aiservice.mapper;

import com.superyoudi.aiservice.pojo.entity.ChatCourseRel;
import org.apache.ibatis.annotations.*;

/**
 * @author Sijayyx
 */
@Mapper
public interface ChatCourseRelMapper {
    /**
     * 根据会话ID查询
     */
    @Select("SELECT * FROM chat_course_rel WHERE chat_id = #{chatId}")
    ChatCourseRel selectByChatId(Long chatId);

    /**
     * 根据课程ID查询
     */
    @Select("SELECT * FROM chat_course_rel WHERE course_id = #{courseId}")
    ChatCourseRel selectByCourseId(Long courseId);

    @Select("SELECT * FROM chat_course_rel WHERE course_id = #{courseId} AND user_id = #{studentId}")
    ChatCourseRel selectByCourseAndStudentId(@Param("courseId") Long courseId, @Param("studentId") Long studentId);

    /**
     * 插入会话课程关联
     */
    @Insert("INSERT INTO chat_course_rel (chat_id, course_id, user_id) VALUES (#{chatId}, #{courseId}, #{userId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    Long insert(ChatCourseRel chatCourseRel);

    /**
     * 更新会话课程关联
     */
    @Update("UPDATE chat_course_rel SET course_id = #{courseId}, user_id = #{userId} WHERE chat_id = #{chatId}")
    Long updateByChatId(ChatCourseRel chatCourseRel);

    /**
     * 根据会话ID删除
     */
    @Delete("DELETE FROM chat_course_rel WHERE chat_id = #{chatId}")
    Long deleteByChatId(Long chatId);

    /**
     * 根据课程ID删除
     */
    @Delete("DELETE FROM chat_course_rel WHERE course_id = #{courseId}")
    Long deleteByCourseId(Long courseId);

}
