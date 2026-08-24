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
     * 根据会话ID查询最新的 N 条消息（按序号升序）
     *
     * @param chatId 会话ID
     * @param limit  查询条数
     * @return 最新的 N 条消息列表（按序号升序排列）
     */
    @Select("SELECT * FROM chat_messages WHERE chat_id = #{chatId} " +
            "ORDER BY msg_seq DESC LIMIT #{limit}")
    List<ChatMessage> selectByChatIdWithLimit(@Param("chatId") Long chatId,
                                              @Param("limit") Integer limit);

    /**
     * 根据会话ID和消息序号查询单条消息
     */
    @Select("SELECT * FROM chat_messages WHERE chat_id = #{chatId} AND msg_seq = #{msgSeq}")
    ChatMessage selectByChatIdAndSeq(@Param("chatId") Long chatId, @Param("msgSeq") Integer msgSeq);

    /**
     * 查询指定chat_id下msg_seq最大，且role为AI的行
     */
    @Select("SELECT * FROM chat_messages WHERE chat_id = #{chatId} AND role = 'ai' ORDER BY msg_seq DESC LIMIT 1")
    ChatMessage selectLatestAiMessageByChatId(Long chatId);

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
     * 插入消息，并自增msg_seq。
     * <p>
     * 自增的msg_seq将回填至chatMessage中。
     */
    @SelectKey(
            // 预先计算msg_seq
            statement = "SELECT COALESCE(MAX(msg_seq), 0) + 1 FROM chat_messages WHERE chat_id = #{chatId}",
            keyProperty = "msgSeq",
            before = true,
            resultType = Integer.class
    )
    @Insert("""
            INSERT INTO chat_messages (chat_id, msg_seq, msg_type, role, message, sent_at)
            VALUES (#{chatId}, #{msgSeq}, #{msgType}, #{role}, #{message}, #{sentAt})
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