package com.superyoudi.aiservice.mapper;

import com.superyoudi.aiservice.pojo.entity.ChatSession;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author Sijayyx
 */
@Mapper
public interface ChatSessionMapper {

    @Select("SELECT * FROM chat_session WHERE id = #{id}")
    ChatSession selectById(Long id);

    // 联查
    @Select("SELECT cs.id, cs.user_id, cs.created_at " +
            "FROM chat_session cs " +
            "INNER JOIN chat_course_rel ccr ON cs.id = ccr.chat_id " +
            "WHERE ccr.course_id = #{courseId}")
    ChatSession selectByCourseId(Long courseId);

    @Select("SELECT * FROM chat_session WHERE user_id = #{userId}")
    List<ChatSession> selectByUserId(Long userId);

    @Select("SELECT * FROM chat_session")
    List<ChatSession> selectAll();

    @Insert("INSERT INTO chat_session (user_id, created_at) VALUES (#{userId}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    Long insert(ChatSession chatSession);

    @Update("UPDATE chat_session SET user_id = #{userId}, created_at = #{createdAt} WHERE id = #{id}")
    Long update(ChatSession chatSession);

    @Delete("DELETE FROM chat_session WHERE id = #{id}")
    Long deleteById(Long id);

    @Delete("DELETE FROM chat_session WHERE user_id = #{userId}")
    Long deleteByUserId(Long userId);
}