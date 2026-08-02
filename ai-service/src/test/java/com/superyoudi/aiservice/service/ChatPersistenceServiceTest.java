package com.superyoudi.aiservice.service;

import com.superyoudi.aiservice.mapper.ChatMessageMapper;
import com.superyoudi.aiservice.mapper.ChatSessionMapper;
import com.superyoudi.aiservice.pojo.entity.ChatMessage;
import com.superyoudi.aiservice.pojo.entity.ChatSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatPersistenceService 单元测试")
class ChatPersistenceServiceTest {

    @Mock
    private ChatSessionMapper chatSessionMapper;

    @Mock
    private ChatMessageMapper chatMessageMapper;

    @InjectMocks
    private ChatPersistenceService chatPersistenceService;

    private ChatSession chatSession;
    private ChatMessage chatMessage;

    @BeforeEach
    void setUp() {
        chatSession = new ChatSession();
        chatSession.setId(1L);
        chatSession.setUserId(100L);
        chatSession.setCreatedAt(LocalDateTime.now());

        chatMessage = new ChatMessage();
        chatMessage.setId(1L);
        chatMessage.setChatId(1L);
        chatMessage.setMsgSeq(1);
        chatMessage.setMsgType("text");
        chatMessage.setRole("user");
        chatMessage.setMessage("{\"content\":\"Hello\"}");
        chatMessage.setSentAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("获取会话消息列表 - 成功返回消息列表")
    void shouldReturnChatMessages_WhenChatSessionExists() {
        // Given
        List<ChatMessage> expectedMessages = Arrays.asList(chatMessage);
        when(chatMessageMapper.selectByChatId(1L)).thenReturn(expectedMessages);

        // When
        List<ChatMessage> result = chatPersistenceService.getChatMessages(1L);

        // Then
        assertThat(result).isNotNull()
                .hasSize(1)
                .isEqualTo(expectedMessages);
        assertThat(result.get(0).getChatId()).isEqualTo(1L);
        assertThat(result.get(0).getMsgSeq()).isEqualTo(1);

        verify(chatMessageMapper, times(1)).selectByChatId(1L);
    }

    @Test
    @DisplayName("获取会话消息列表 - 空会话返回空列表")
    void shouldReturnEmptyList_WhenChatSessionHasNoMessages() {
        // Given
        when(chatMessageMapper.selectByChatId(1L)).thenReturn(Arrays.asList());

        // When
        List<ChatMessage> result = chatPersistenceService.getChatMessages(1L);

        // Then
        assertThat(result).isNotNull().isEmpty();
        verify(chatMessageMapper, times(1)).selectByChatId(1L);
    }

    @Test
    @DisplayName("追加消息 - 成功返回影响行数")
    void shouldAppendChatMessage_AndReturnAffectedRows() {
        // Given
        when(chatMessageMapper.insert(any(ChatMessage.class))).thenReturn(1L);

        // When
        Long result = chatPersistenceService.appendChatMessage(chatMessage);

        // Then
        assertThat(result).isEqualTo(1L);
        verify(chatMessageMapper, times(1)).insert(chatMessage);
    }

    @Test
    @DisplayName("追加消息 - 插入多条消息验证序号递增")
    void shouldAppendMultipleMessages_WithIncrementalSeq() {
        // Given
        when(chatMessageMapper.insert(any(ChatMessage.class))).thenReturn(1L);

        ChatMessage message1 = new ChatMessage();
        message1.setChatId(1L);
        message1.setMsgSeq(1);
        message1.setMsgType("text");
        message1.setRole("user");
        message1.setMessage("First message");

        ChatMessage message2 = new ChatMessage();
        message2.setChatId(1L);
        message2.setMsgSeq(2);
        message2.setMsgType("text");
        message2.setRole("ai");
        message2.setMessage("Second message");

        // When
        Long result1 = chatPersistenceService.appendChatMessage(message1);
        Long result2 = chatPersistenceService.appendChatMessage(message2);

        // Then
        assertThat(result1).isEqualTo(1L);
        assertThat(result2).isEqualTo(1L);
        verify(chatMessageMapper, times(1)).insert(message1);
        verify(chatMessageMapper, times(1)).insert(message2);
    }

    @Test
    @DisplayName("插入会话元数据 - 成功返回影响行数")
    void shouldInsertChatSession_AndReturnAffectedRows() {
        // Given
        when(chatSessionMapper.insert(any(ChatSession.class))).thenReturn(1L);

        // When
        Long result = chatPersistenceService.insertChatSession(chatSession);

        // Then
        assertThat(result).isEqualTo(1L);
        verify(chatSessionMapper, times(1)).insert(chatSession);
    }

    @Test
    @DisplayName("插入会话元数据 - 验证会话属性正确传递")
    void shouldInsertChatSession_WithCorrectProperties() {
        // Given
        when(chatSessionMapper.insert(any(ChatSession.class))).thenReturn(1L);

        ChatSession session = new ChatSession();
        session.setUserId(200L);
        session.setCreatedAt(LocalDateTime.now());

        // When
        Long result = chatPersistenceService.insertChatSession(session);

        // Then
        assertThat(result).isEqualTo(1L);
        verify(chatSessionMapper).insert(argThat(cs ->
                cs.getUserId().equals(200L) && cs.getCreatedAt() != null
        ));
    }

    @Test
    @DisplayName("获取会话 - 成功返回会话对象")
    void shouldGetChatSession_WhenIdExists() {
        // Given
        when(chatSessionMapper.selectById(1L)).thenReturn(chatSession);

        // When
        ChatSession result = chatPersistenceService.getChatSession(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUserId()).isEqualTo(100L);
        assertThat(result.getCreatedAt()).isNotNull();

        verify(chatSessionMapper, times(1)).selectById(1L);
    }

    @Test
    @DisplayName("获取会话 - 不存在返回null")
    void shouldReturnNull_WhenChatSessionNotExists() {
        // Given
        when(chatSessionMapper.selectById(999L)).thenReturn(null);

        // When
        ChatSession result = chatPersistenceService.getChatSession(999L);

        // Then
        assertThat(result).isNull();
        verify(chatSessionMapper, times(1)).selectById(999L);
    }

    @Test
    @DisplayName("获取会话 - 多次调用验证缓存行为")
    void shouldCallMapperMultipleTimes_WhenGettingSessionRepeatedly() {
        // Given
        when(chatSessionMapper.selectById(1L)).thenReturn(chatSession);

        // When
        ChatSession result1 = chatPersistenceService.getChatSession(1L);
        ChatSession result2 = chatPersistenceService.getChatSession(1L);

        // Then
        assertThat(result1).isEqualTo(result2);
        // 验证实际调用了两次（没有缓存，每次都查数据库）
        verify(chatSessionMapper, times(2)).selectById(1L);
    }

    @Test
    @DisplayName("集成场景 - 创建会话并追加消息")
    void shouldCreateSessionAndAppendMessages() {
        // Given
        when(chatSessionMapper.insert(any(ChatSession.class))).thenReturn(1L);
        when(chatMessageMapper.insert(any(ChatMessage.class))).thenReturn(1L);

        // When - 创建会话
        ChatSession newSession = new ChatSession();
        newSession.setUserId(100L);
        newSession.setCreatedAt(LocalDateTime.now());
        Long sessionResult = chatPersistenceService.insertChatSession(newSession);

        // When - 追加消息
        ChatMessage newMessage = new ChatMessage();
        newMessage.setChatId(1L);
        newMessage.setMsgSeq(1);
        newMessage.setMsgType("text");
        newMessage.setRole("user");
        newMessage.setMessage("Hello AI");
        newMessage.setSentAt(LocalDateTime.now());
        Long messageResult = chatPersistenceService.appendChatMessage(newMessage);

        // Then
        assertThat(sessionResult).isEqualTo(1L);
        assertThat(messageResult).isEqualTo(1L);

        verify(chatSessionMapper).insert(any(ChatSession.class));
        verify(chatMessageMapper).insert(any(ChatMessage.class));
    }
}