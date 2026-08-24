package com.superyoudi.businessservice.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.superyoudi.aiservice.pojo.entity.ChatMessage;
import com.superyoudi.aiservice.pojo.entity.ChatSession;
import com.superyoudi.aiservice.service.ChatPersistenceService;
import com.superyoudi.businessservice.pojo.dto.AgentTaskResultDTO;
import jakarta.annotation.Nonnull;
import jakarta.websocket.*;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.websocket.server.ServerEndpointConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用于向前端的AI聊天发送任务执行结果的数据。
 * <p>
 * ChatMessage应该在数据库存储后传入发送方法。
 * <p>
 * Websocket以chatId标识
 * <p>
 * 一个包装过的ChatMessage, role为system，message存储数据的JSON文本
 * <p>
 * 生成测验：Task<p>
 * 批改任务：Task<p>
 * 布置任务：Task<p>
 * 课件生成：CoursewareGenerationRequest.DTO
 *
 * @author Sijayyx
 */
@Slf4j
@Component
@ServerEndpoint(value = "/ai/chat/websocket/{chatId}", configurator = AgentTaskResultMessageEndpoint.Configurator.class)
public class AgentTaskResultMessageEndpoint {

    private static final ConcurrentHashMap<Long, Session> SESSIONS = new ConcurrentHashMap<>();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule()) // 注册时间序列化模块
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    /**
     * 通知前端任务执行结果，发送ChatMessage
     *
     * @param userId                 用户ID
     * @param result                 任务执行结果
     * @param taskType               数据类型。请查看AgentTaskResultDTO的TYPE_TASK和TYPE_COURSEWARE_GEN_REQUEST
     * @param chatPersistenceService 聊天持久化服务
     * @param <T>                    Task或CoursewareGenerationRequest.DTO
     */
    public static <T> void notifyTaskCompletion(Long userId, @Nonnull T result, @Nonnull String taskType, ChatPersistenceService chatPersistenceService) {
        try {
            // JSON化result文本
            AgentTaskResultDTO<T> taskResultDTO = new AgentTaskResultDTO<>(taskType, result);
            String resultJson = OBJECT_MAPPER.writeValueAsString(taskResultDTO);
            // 存表
            ChatSession session = chatPersistenceService.getOrCreateChatSessionByUserId(userId);
            ChatMessage chatMessage = new ChatMessage(session.getId(), resultJson, ChatMessage.ROLE_SYSTEM);

            Long msgId = chatPersistenceService.appendChatMessage(chatMessage);
            chatMessage.setId(msgId);

            // 通知前端
            notifyTaskResult(chatMessage);

        } catch (JsonProcessingException e) {
            log.error("JSON转换失败", e);
        }

    }

    /**
     * 将一条ChatMessage推送到前端
     *
     * @param chatMessage 要发送的消息，必须指定ChatId
     */
    private static void notifyTaskResult(ChatMessage chatMessage) {

        Assert.notNull(chatMessage.getChatId(), "chatId 不能为空");

        long chatId = chatMessage.getChatId();
        Session session = SESSIONS.get(chatId);
        if (session != null && session.isOpen()) {
            sendMessage(session, chatMessage);
        } else {
            log.warn(" 会话ID {} 对应的连接不存在或已关闭, chatMessage: {}", chatId, chatMessage);
        }
    }

    private static void sendMessage(Session session, ChatMessage chatMessage) {
        try {
            String json = OBJECT_MAPPER.writeValueAsString(chatMessage);
            log.info("发送消息: 会话ID: {}, 消息: {}", chatMessage.getChatId(), json);
            session.getBasicRemote().sendText(json);
        } catch (IOException e) {
            log.error("发送消息失败", e);
        }
    }

    /**
     * 发送心跳包给客户端
     */
    private static void sendHeartbeat(Session session) {
        try {
            session.getBasicRemote().sendText("pong");
        } catch (IOException e) {
            log.error("发送心跳失败", e);
        }
    }

    @OnOpen
    @SuppressWarnings("unused")
    public void onOpen(Session session, @PathParam("chatId") Long chatId) {
        log.info("[AI工具执行状态连接建立] 用户ID: {}", chatId);

        session.setMaxIdleTimeout(600000);

        Session oldSession = SESSIONS.put(chatId, session);

        // 关闭旧的连接
        if (oldSession != null && oldSession.isOpen()) {
            try {
                log.info("重复的连接，此连接 {} 将被关闭", chatId);
                oldSession.close();
            } catch (IOException e) {
                log.warn("关闭旧连接失败", e);
            }
        }
    }

    @OnMessage
    @SuppressWarnings("unused")
    public void onMessage(String message, Session session, @PathParam("chatId") Long chatId) {
        log.info("[收到客户端消息] Chat ID: {}, 消息: {}", chatId, message);

        // 处理心跳消息
        if ("ping".equalsIgnoreCase(message)) {
            sendHeartbeat(session);
        }
    }


    /**
     * 当连接关闭时触发
     */
    @SuppressWarnings("unused")
    @OnClose
    public void onClose(Session session, @PathParam("chatId") Long chatId) {
        log.info("[AI工具执行状态连接关闭], Chat ID: {}", chatId);
        SESSIONS.remove(chatId);

    }

    @SuppressWarnings("unused")
    @OnError
    public void onError(Session session, @PathParam("chatId") Long chatId, Throwable throwable) {
        log.error("[AI工具执行状态连接错误] Chat ID: {}, 异常: {}", chatId, throwable.getMessage());
        SESSIONS.remove(chatId);

    }

    public static class Configurator extends ServerEndpointConfig.Configurator {
        @Override
        public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
            sec.getUserProperties().put("maxIdleTimeout", 600000);
        }
    }

}
