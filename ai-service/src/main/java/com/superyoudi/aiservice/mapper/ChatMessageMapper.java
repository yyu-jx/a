package com.superyoudi.aiservice.mapper;

import com.superyoudi.aiservice.pojo.entity.ChatMessage;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author Sijayyx
 */
@Mapper
public interface ChatMessageMapper {

    @Select("SELECT * FROM chat_messages WHERE id = #{id}")
    ChatMessage selectById(Long id);

    /**
     * 根据会话ID查询所有消息（按序号升序）
     */
    @Select("SELECT * FROM chat_messages WHERE chat_id = #{chatId} ORDER BY msg_seq ASC")
    List<ChatMessage> selectByChatId(Long chatId);

    /**
     * 根据会话ID和消息序号查询单条消息
     */
    @Select("SELECT * FROM chat_messages WHERE chat_id = #{chatId} AND msg_seq = #{msgSeq}")
    ChatMessage selectByChatIdAndSeq(@Param("chatId") Long chatId, @Param("msgSeq") Integer msgSeq);

    /**
     * 查询会话中指定角色类型的消息
     */
    @Select("SELECT * FROM chat_messages WHERE chat_id = #{chatId} AND role = #{role} ORDER BY msg_seq ASC")
    List<ChatMessage> selectByChatIdAndRole(@Param("chatId") Long chatId, @Param("role") String role);

    /**
     * 查询会话中指定消息类型的消息
     */
    @Select("SELECT * FROM chat_messages WHERE chat_id = #{chatId} AND msg_type = #{msgType} ORDER BY msg_seq ASC")
    List<ChatMessage> selectByChatIdAndMsgType(@Param("chatId") Long chatId, @Param("msgType") String msgType);

    /**
     * 插入消息
     */
    @Insert("INSERT INTO chat_messages (chat_id, msg_seq, msg_type, role, message, sent_at) " +
            "VALUES (#{chatId}, #{msgSeq}, #{msgType}, #{role}, #{message}, #{sentAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    Long insert(ChatMessage chatMessage);

    /**
     * 插入消息，并自增msg_seq
     */
    @Insert("""
            INSERT INTO chat_messages (chat_id, msg_seq, msg_type, role, message, sent_at)
            SELECT #{chatId},
            COALESCE(MAX(msg_seq), 0) + 1,
            #{msgType}, #{role}, #{message}, #{sentAt}
            FROM chat_messages
            WHERE chat_id = #{chatId}
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    Long insertWithAutoSeq(ChatMessage chatMessage);

    /**
     * 更新消息内容
     */
    @Update("UPDATE chat_messages SET message = #{message}, msg_type = #{msgType} WHERE id = #{id}")
    Long update(ChatMessage chatMessage);

    /**
     * 根据ID删除消息
     */
    @Delete("DELETE FROM chat_messages WHERE id = #{id}")
    Long deleteById(Long id);

    /**
     * 根据会话ID删除所有消息
     */
    @Delete("DELETE FROM chat_messages WHERE chat_id = #{chatId}")
    Long deleteByChatId(Long chatId);

    /**
     * 删除会话中某个序号之后的消息
     */
    @Delete("DELETE FROM chat_messages WHERE chat_id = #{chatId} AND msg_seq > #{msgSeq}")
    Long deleteAfterSeq(@Param("chatId") Long chatId, @Param("msgSeq") Integer msgSeq);

    /**
     * 统计会话的消息数量
     */
    @Select("SELECT COUNT(*) FROM chat_messages WHERE chat_id = #{chatId}")
    Long countByChatId(Long chatId);
}