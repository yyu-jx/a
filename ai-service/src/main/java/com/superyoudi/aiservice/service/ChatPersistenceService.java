package com.superyoudi.aiservice.service;

import com.superyoudi.aiservice.mapper.*;
import com.superyoudi.aiservice.pojo.dto.AiCitation;
import com.superyoudi.aiservice.pojo.entity.ChatCourseRel;
import com.superyoudi.aiservice.pojo.entity.ChatCoursewareRecRel;
import com.superyoudi.aiservice.pojo.entity.ChatMessage;
import com.superyoudi.aiservice.pojo.entity.ChatSession;
import com.superyoudi.common.dto.ChatHistoryDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 会话持久化管理服务
 *
 * @author Sijayyx
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatPersistenceService {

    private static final int CHAT_MESSAGE_LIMIT = 20; // 默认会话查询消息数量限制

    private final ChatSessionMapper chatSessionMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final ChatCourseRelMapper chatCourseRelMapper;
    private final ChatCoursewareRecRelMapper chatCoursewareRecRelMapper;
    private final AiCitationMapper aiCitationMapper;

    /**
     * 构造会话历史记录
     */
    public ChatHistoryDTO constructChatHistory(Long chatSessionId, Long userId, List<ChatMessage> chatMessages) {
        List<ChatHistoryDTO.ChatMessageData> messages = chatMessages.stream().map(message ->
                        new ChatHistoryDTO.ChatMessageData(message.getMsgSeq(), message.getMsgType(),
                                message.getMessage(), message.getRole(), message.getSentAt()))
                .toList();

        return new ChatHistoryDTO(chatSessionId, userId, messages);
    }

    public ChatMessage getLatestAiMessageByChatId(Long chatSessionId) {
        return chatMessageMapper.selectLatestAiMessageByChatId(chatSessionId);
    }


    public void saveCitation(AiCitation citation) {
        aiCitationMapper.insert(citation);
    }

    public List<AiCitation> getCitationsByMessageId(Long chatMessageId) {
        return aiCitationMapper.selectByChatMessageId(chatMessageId);
    }

    public List<AiCitation> getCitationsBySuggestionId(Long suggestionId) {
        return aiCitationMapper.selectByChatMessageId(suggestionId);
    }

    public void deleteByMessageId(Long chatMessageId) {
        aiCitationMapper.deleteByChatMessageId(chatMessageId);
    }

    /**
     * 获取会话消息列表
     */
    @Transactional
    public List<ChatMessage> getChatMessages(Long chatSessionId) {
        return chatMessageMapper.selectByChatId(chatSessionId);
    }

    /**
     * 获取会话消息列表。如果不存在会话元数据，就创建，并返回null。
     */
    @Transactional
    public List<ChatMessage> getOrCreateCourseRelChatMessages(Long studentId, Long courseId) {
        ChatCourseRel chatCourseRel = chatCourseRelMapper.selectByCourseAndStudentId(courseId, studentId);
        if (chatCourseRel == null) {
            log.info("未找到课程与聊天关联，创建新的关联关系。");
            createChatSessionAndCourseRel(studentId, courseId);
            return null;
        }

        return chatMessageMapper.selectByChatIdWithLimit(chatCourseRel.getChatId(), CHAT_MESSAGE_LIMIT);
    }

    @Transactional
    public List<ChatMessage> getOrCreateCoursewareRecChatMessages(Long studentId, Long coursewareRecId) {
        ChatCoursewareRecRel chatCoursewareRecRel = chatCoursewareRecRelMapper.selectByCoursewareRecordId(coursewareRecId);
        if (chatCoursewareRecRel == null) {
            log.info("未找到课件生成与聊天关联，创建新的关联关系。");
            createChatCoursewareRecRel(studentId, coursewareRecId);
            return null;
        }

        return chatMessageMapper.selectByChatIdWithLimit(chatCoursewareRecRel.getChatId(), CHAT_MESSAGE_LIMIT);
    }

    @Transactional
    public List<ChatMessage> getOrCreateTeacherIdChatMessages(Long teacherId) {
        ChatSession chatSession = getOrCreateChatSessionByUserId(teacherId);
        if (chatSession == null) {
            log.info("未找到教师与聊天关联，创建新的关联关系。");

            createChatSession(teacherId);
            return null;
        }

        return chatMessageMapper.selectByChatIdWithLimit(chatSession.getId(), CHAT_MESSAGE_LIMIT);

    }

    /**
     * 向列表追加消息
     *
     * @return 消息ID
     */
    @Transactional
    public Long appendChatMessage(Long chatId, String message, String role) {
        ChatMessage chatMessage = new ChatMessage(chatId, message, role);
        chatMessageMapper.insertWithAutoSeq(chatMessage);

        return chatMessage.getId();
    }

    /**
     * 向列表追加消息
     *
     * @return 消息ID
     */
    @Transactional
    public Long appendChatMessage(ChatMessage chatMessage) {
        chatMessageMapper.insertWithAutoSeq(chatMessage);
        return chatMessage.getId();
    }

    /**
     * 获取课程对应的会话
     */
    @Transactional
    public ChatSession getOrCreateChatSessionByCourseRel(Long studentId, Long courseId) {
        ChatCourseRel chatCourseRel = chatCourseRelMapper.selectByCourseAndStudentId(courseId, studentId);
        if (chatCourseRel == null) {
            log.error("未找到课程与聊天关联。");
            createChatSessionAndCourseRel(studentId, courseId);
        }

        chatCourseRel = chatCourseRelMapper.selectByCourseAndStudentId(courseId, studentId);

        ChatSession chatSession = chatSessionMapper.selectById(chatCourseRel.getChatId());
        if (chatSession == null) {
            chatSession = createChatSession(chatCourseRel.getChatId());
            chatCourseRel.setChatId(chatSession.getId());
            chatCourseRelMapper.update(chatCourseRel);
        }

        return chatSession;
    }

    /**
     * 根据用户ID查询会话。用于教师ID
     */
    @Transactional
    public ChatSession getOrCreateChatSessionByUserId(Long userId) {
        ChatSession chatSession = chatSessionMapper.selectSingleByUserId(userId);
        if (chatSession == null) {
            log.error("未找到会话，创建新的会话。");
            chatSession = createChatSession(userId);
        }
        return chatSession;
    }

    /**
     * 获取课件生成记录对应的会话
     */
    @Transactional
    public ChatSession getChatSessionByCoursewareRecId(@NonNull Long coursewareRecId) {
        return chatSessionMapper.selectByCoursewareRecordId(coursewareRecId);
    }

    /**
     * 创建会话与课程关联关系
     */
    @Transactional
    public Long createChatSessionAndCourseRel(Long userId, Long courseId) {
        ChatSession chatSession = createChatSession(userId);
        ChatCourseRel chatCourseRel = new ChatCourseRel(null, chatSession.getId(), courseId, userId);
        chatCourseRelMapper.insert(chatCourseRel);
        return chatCourseRel.getId();
    }

    private ChatSession createChatSession(Long userId) {
        ChatSession chatSession = new ChatSession(null, userId, null);
        chatSessionMapper.insert(chatSession);
        return chatSession;
    }


    /**
     * 创建会话与课程关联关系
     */
    @Transactional
    public Long createChatCoursewareRecRel(Long userId, Long coursewareRecId) {
        ChatSession chatSession = createChatSession(userId);
        ChatCoursewareRecRel chatCoursewareRecRel = new ChatCoursewareRecRel(null, chatSession.getId(), coursewareRecId, userId);
        chatCoursewareRecRelMapper.insert(chatCoursewareRecRel);
        return chatCoursewareRecRel.getId();
    }

}
