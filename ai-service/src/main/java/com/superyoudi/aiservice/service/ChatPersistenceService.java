package com.superyoudi.aiservice.service;

import com.superyoudi.aiservice.mapper.ChatCourseRelMapper;
import com.superyoudi.aiservice.mapper.ChatCoursewareRecRelMapper;
import com.superyoudi.aiservice.mapper.ChatMessageMapper;
import com.superyoudi.aiservice.mapper.ChatSessionMapper;
import com.superyoudi.aiservice.pojo.entity.ChatCourseRel;
import com.superyoudi.aiservice.pojo.entity.ChatCoursewareRecRel;
import com.superyoudi.aiservice.pojo.entity.ChatMessage;
import com.superyoudi.aiservice.pojo.entity.ChatSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 会话持久化管理服务
 *
 * @author Sijayyx
 */
@Service
@RequiredArgsConstructor
public class ChatPersistenceService {

    private final ChatSessionMapper chatSessionMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final ChatCourseRelMapper chatCourseRelMapper;
    private final ChatCoursewareRecRelMapper chatCoursewareRecRelMapper;

    /**
     * 获取会话消息列表
     */
    public List<ChatMessage> getChatMessages(Long chatSessionId) {
        return chatMessageMapper.selectByChatId(chatSessionId);
    }

    /**
     * 向列表追加消息
     */
    public Long appendChatMessage(ChatMessage chatMessage) {
        return chatMessageMapper.insert(chatMessage);
    }

    /**
     * 插入消息元数据
     */
    public Long insertChatSession(ChatSession chatSession) {
        return chatSessionMapper.insert(chatSession);
    }

    /**
     * 获取会话元数据
     */
    public ChatSession getChatSession(Long id) {
        return chatSessionMapper.selectById(id);
    }

    /**
     * 根据用户ID查询会话。用于教师ID
     */
    public ChatSession getChatSessionByUserId(Long userId) {
        return chatSessionMapper.selectSingleByUserId(userId);
    }

    /**
     * 获取课件生成记录对应的会话
     */
    public ChatSession getChatSessionByCoursewareRecId(Long coursewareRecId) {
        return chatSessionMapper.selectByCoursewareRecordId(coursewareRecId);
    }

    /**
     * 获取课程对应的会话
     */
    public ChatSession getChatSessionByCourseId(Long courseId) {
        return chatSessionMapper.selectByCourseId(courseId);
    }

    /**
     * 创建会话与课程关联关系
     */
    public Long createChatCourseRel(Long chatId, Long courseId, Long userId) {
        ChatCourseRel chatCourseRel = new ChatCourseRel(null, chatId, courseId, userId);
        return chatCourseRelMapper.insert(chatCourseRel);
    }

    /**
     * 创建会话与课件生成记录关联关系
     */
    public Long createChatCoursewareRecRel(Long chatId, Long coursewareRecId, Long userId) {
        ChatCoursewareRecRel chatCoursewareRecRel = new ChatCoursewareRecRel(null, chatId, coursewareRecId, userId);
        return chatCoursewareRecRelMapper.insert(chatCoursewareRecRel);
    }
}
