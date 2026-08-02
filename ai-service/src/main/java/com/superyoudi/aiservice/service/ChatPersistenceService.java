package com.superyoudi.aiservice.service;

import com.superyoudi.aiservice.mapper.ChatMessageMapper;
import com.superyoudi.aiservice.mapper.ChatSessionMapper;
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
     * 获取课程对应的会话
     */
    public ChatSession getChatSessionByCourse(Long courseId) {
        return chatSessionMapper.selectByCourseId(courseId);
    }
}
