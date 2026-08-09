package com.superyoudi.aiservice.service;

import com.superyoudi.aiservice.mapper.ChatCourseRelMapper;
import com.superyoudi.aiservice.mapper.ChatCoursewareRecRelMapper;
import com.superyoudi.aiservice.mapper.ChatMessageMapper;
import com.superyoudi.aiservice.mapper.ChatSessionMapper;
import com.superyoudi.aiservice.pojo.entity.ChatCourseRel;
import com.superyoudi.aiservice.pojo.entity.ChatCoursewareRecRel;
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

    @Mock
    private ChatCourseRelMapper chatCourseRelMapper;

    @Mock
    private ChatCoursewareRecRelMapper chatCoursewareRecRelMapper;

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
        List<ChatMessage> result = chatPersistenceService.getOrCreateChatMessages(1L);

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
        List<ChatMessage> result = chatPersistenceService.getOrCreateChatMessages(1L);

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

    @Test
    @DisplayName("根据用户ID查询会话 - 成功")
    void shouldReturnChatSession_WhenUserHasSession() {
        // Given
        when(chatSessionMapper.selectSingleByUserId(100L)).thenReturn(chatSession);

        // When
        ChatSession result = chatPersistenceService.getChatSessionByUserId(100L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUserId()).isEqualTo(100L);
        verify(chatSessionMapper, times(1)).selectSingleByUserId(100L);
    }

    @Test
    @DisplayName("根据用户ID查询会话 - 无会话返回null")
    void shouldReturnNull_WhenUserHasNoSession() {
        // Given
        when(chatSessionMapper.selectSingleByUserId(999L)).thenReturn(null);

        // When
        ChatSession result = chatPersistenceService.getChatSessionByUserId(999L);

        // Then
        assertThat(result).isNull();
        verify(chatSessionMapper, times(1)).selectSingleByUserId(999L);
    }

    @Test
    @DisplayName("根据用户ID查询会话 - 不同用户查询各自的会话")
    void shouldReturnDifferentSessions_ForDifferentUsers() {
        // Given
        ChatSession session1 = new ChatSession();
        session1.setId(1L);
        session1.setUserId(100L);

        ChatSession session2 = new ChatSession();
        session2.setId(2L);
        session2.setUserId(200L);

        when(chatSessionMapper.selectSingleByUserId(100L)).thenReturn(session1);
        when(chatSessionMapper.selectSingleByUserId(200L)).thenReturn(session2);

        // When
        ChatSession result1 = chatPersistenceService.getChatSessionByUserId(100L);
        ChatSession result2 = chatPersistenceService.getChatSessionByUserId(200L);

        // Then
        assertThat(result1.getUserId()).isEqualTo(100L);
        assertThat(result2.getUserId()).isEqualTo(200L);
        assertThat(result1.getId()).isNotEqualTo(result2.getId());
    }

    @Test
    @DisplayName("根据课件生成记录ID查询会话 - 成功")
    void shouldReturnChatSession_WhenCoursewareRecordExists() {
        // Given
        when(chatSessionMapper.selectByCoursewareRecordId(200L)).thenReturn(chatSession);

        // When
        ChatSession result = chatPersistenceService.getChatSessionByCoursewareRecId(200L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(chatSessionMapper, times(1)).selectByCoursewareRecordId(200L);
    }

    @Test
    @DisplayName("根据课件生成记录ID查询会话 - 无关联会话返回null")
    void shouldReturnNull_WhenNoSessionLinkedToCoursewareRecord() {
        // Given
        when(chatSessionMapper.selectByCoursewareRecordId(999L)).thenReturn(null);

        // When
        ChatSession result = chatPersistenceService.getChatSessionByCoursewareRecId(999L);

        // Then
        assertThat(result).isNull();
        verify(chatSessionMapper, times(1)).selectByCoursewareRecordId(999L);
    }

    @Test
    @DisplayName("根据课程ID查询会话 - 成功")
    void shouldReturnChatSession_WhenCourseExists() {
        // Given
        when(chatSessionMapper.selectByCourseId(300L)).thenReturn(chatSession);

        // When
        ChatSession result = chatPersistenceService.getChatSessionByCourseId(300L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(chatSessionMapper, times(1)).selectByCourseId(300L);
    }

    @Test
    @DisplayName("根据课程ID查询会话 - 无关联会话返回null")
    void shouldReturnNull_WhenCourseHasNoSession() {
        // Given
        when(chatSessionMapper.selectByCourseId(999L)).thenReturn(null);

        // When
        ChatSession result = chatPersistenceService.getChatSessionByCourseId(999L);

        // Then
        assertThat(result).isNull();
        verify(chatSessionMapper, times(1)).selectByCourseId(999L);
    }

    @Test
    @DisplayName("创建会话与课程关联关系 - 成功")
    void shouldCreateChatCourseRel_AndReturnAffectedRows() {
        // Given
        when(chatCourseRelMapper.insert(any(ChatCourseRel.class))).thenReturn(1L);

        // When
        Long result = chatPersistenceService.createChatCourseRel(1L, 300L, 100L);

        // Then
        assertThat(result).isEqualTo(1L);
        verify(chatCourseRelMapper, times(1)).insert(any(ChatCourseRel.class));
    }

    @Test
    @DisplayName("创建会话与课程关联关系 - 验证参数正确传递")
    void shouldPassCorrectProperties_WhenCreatingChatCourseRel() {
        // Given
        when(chatCourseRelMapper.insert(any(ChatCourseRel.class))).thenReturn(1L);

        // When
        chatPersistenceService.createChatCourseRel(1L, 300L, 100L);

        // Then
        verify(chatCourseRelMapper).insert(argThat(rel ->
                rel.getChatId().equals(1L) &&
                        rel.getCourseId().equals(300L) &&
                        rel.getUserId().equals(100L) &&
                        rel.getId() == null
        ));
    }

    @Test
    @DisplayName("创建会话与课程关联关系 - 插入失败返回0")
    void shouldReturnZero_WhenChatCourseRelInsertFails() {
        // Given
        when(chatCourseRelMapper.insert(any(ChatCourseRel.class))).thenReturn(0L);

        // When
        Long result = chatPersistenceService.createChatCourseRel(1L, 300L, 100L);

        // Then
        assertThat(result).isEqualTo(0L);
        verify(chatCourseRelMapper, times(1)).insert(any(ChatCourseRel.class));
    }

    @Test
    @DisplayName("创建会话与课件生成记录关联关系 - 成功")
    void shouldCreateChatCoursewareRecRel_AndReturnAffectedRows() {
        // Given
        when(chatCoursewareRecRelMapper.insert(any(ChatCoursewareRecRel.class))).thenReturn(1L);

        // When
        Long result = chatPersistenceService.createChatCoursewareRecRel(1L, 200L, 100L);

        // Then
        assertThat(result).isEqualTo(1L);
        verify(chatCoursewareRecRelMapper, times(1)).insert(any(ChatCoursewareRecRel.class));
    }

    @Test
    @DisplayName("创建会话与课件生成记录关联关系 - 验证参数正确传递")
    void shouldPassCorrectProperties_WhenCreatingChatCoursewareRecRel() {
        // Given
        when(chatCoursewareRecRelMapper.insert(any(ChatCoursewareRecRel.class))).thenReturn(1L);

        // When
        chatPersistenceService.createChatCoursewareRecRel(1L, 200L, 100L);

        // Then
        verify(chatCoursewareRecRelMapper).insert(argThat(rel ->
                rel.getChatId().equals(1L) &&
                        rel.getCoursewareRecordId().equals(200L) &&
                        rel.getUserId().equals(100L) &&
                        rel.getId() == null
        ));
    }

    @Test
    @DisplayName("创建会话与课件生成记录关联关系 - 插入失败返回0")
    void shouldReturnZero_WhenChatCoursewareRecRelInsertFails() {
        // Given
        when(chatCoursewareRecRelMapper.insert(any(ChatCoursewareRecRel.class))).thenReturn(0L);

        // When
        Long result = chatPersistenceService.createChatCoursewareRecRel(1L, 200L, 100L);

        // Then
        assertThat(result).isEqualTo(0L);
        verify(chatCoursewareRecRelMapper, times(1)).insert(any(ChatCoursewareRecRel.class));
    }

    @Test
    @DisplayName("集成场景 - 查询会话并创建课程和课件关联")
    void shouldCompleteFullWorkflow_QueryAndCreateRelations() {
        // Given
        when(chatSessionMapper.selectSingleByUserId(100L)).thenReturn(chatSession);
        when(chatCourseRelMapper.insert(any(ChatCourseRel.class))).thenReturn(1L);
        when(chatCoursewareRecRelMapper.insert(any(ChatCoursewareRecRel.class))).thenReturn(1L);

        // When - 查询用户会话
        ChatSession session = chatPersistenceService.getChatSessionByUserId(100L);

        // When - 创建课程关联
        Long courseRelResult = chatPersistenceService.createChatCourseRel(
                session.getId(), 300L, session.getUserId()
        );

        // When - 创建课件记录关联
        Long coursewareRelResult = chatPersistenceService.createChatCoursewareRecRel(
                session.getId(), 200L, session.getUserId()
        );

        // Then
        assertThat(session).isNotNull();
        assertThat(courseRelResult).isEqualTo(1L);
        assertThat(coursewareRelResult).isEqualTo(1L);

        verify(chatSessionMapper).selectSingleByUserId(100L);
        verify(chatCourseRelMapper).insert(any(ChatCourseRel.class));
        verify(chatCoursewareRecRelMapper).insert(any(ChatCoursewareRecRel.class));
    }

    @Test
    @DisplayName("集成场景 - 查询不存在的会话后不创建关联")
    void shouldNotCreateRelations_WhenSessionNotFound() {
        // Given
        when(chatSessionMapper.selectSingleByUserId(999L)).thenReturn(null);

        // When
        ChatSession session = chatPersistenceService.getChatSessionByUserId(999L);

        // Then
        assertThat(session).isNull();
        verify(chatCourseRelMapper, never()).insert(any());
        verify(chatCoursewareRecRelMapper, never()).insert(any());
    }
}