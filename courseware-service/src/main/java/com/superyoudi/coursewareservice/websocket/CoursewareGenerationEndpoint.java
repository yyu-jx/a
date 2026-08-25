package com.superyoudi.coursewareservice.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.superyoudi.coursewareservice.pojo.dto.GenerationStatus;
import jakarta.websocket.*;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.websocket.server.ServerEndpointConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI文件生成状态通知WebSocket接口
 * 用于向客户端实时推送AI文件生成的状态信息
 */
@Slf4j
@Component
@ServerEndpoint(value = "/course/preparing/preview/{coursewareId}", configurator = CoursewareGenerationEndpoint.Configurator.class)
public class CoursewareGenerationEndpoint {

    // 存储所有连接的会话
    private static final ConcurrentHashMap<Long, Session> SESSIONS = new ConcurrentHashMap<>();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 向前端推送通知。
     * <p>
     * AI 调用此方法通知前端文件生成状态
     */
    public static void notifyGeneratingStatus(GenerationStatus generationStatus) {
        long coursewareId = generationStatus.getCoursewareId();
        Session session = SESSIONS.get(coursewareId);
        if (session != null && session.isOpen()) {
            sendMessage(session, generationStatus);
        } else {
            log.warn(" 会话ID {} 对应的连接不存在或已关闭, generationStatus: {}", coursewareId, generationStatus);
        }
    }

    /**
     * 发送Result对象给客户端（JSON格式）
     */
    private static void sendMessage(Session session, GenerationStatus generationStatus) {
        try {
            String json = OBJECT_MAPPER.writeValueAsString(generationStatus);
            log.info("发送消息: 会话ID: {}, 消息: {}", generationStatus.getCoursewareId(), json);
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

    /**
     * 当连接建立时触发
     *
     * @param session      WebSocket会话
     * @param coursewareId 会话ID，用于区分不同的客户端连接
     */
    @OnOpen
    @SuppressWarnings("unused")
    public void onOpen(Session session, @PathParam("coursewareId") Long coursewareId) {
        log.info("[AI生成状态连接建立] 用户ID: {}", coursewareId);

        session.setMaxIdleTimeout(600000);

        Session oldSession = SESSIONS.put(coursewareId, session);

        // 关闭旧的连接
        if (oldSession != null && oldSession.isOpen()) {
            try {
                log.info("重复的连接，此连接 {} 将被关闭", coursewareId);
                oldSession.close();
            } catch (IOException e) {
                log.warn("关闭旧连接失败", e);
            }
        }

//        new Thread(() -> {
//            try {
//                sendHeartbeat(session);
//                Thread.sleep(1000);
//                sendHeartbeat(session);
//                Thread.sleep(1000);
//                sendHeartbeat(session);
//                Thread.sleep(1000);
//                sendHeartbeat(session);
//                Thread.sleep(1000);
//                sendHeartbeat(session);
//                Thread.sleep(1000);
//                sendHeartbeat(session);
//                Thread.sleep(1000);
//                sendHeartbeat(session);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        }).start();

    }

    /**
     * 当收到客户端消息时触发
     * 客户端可以发送心跳或其他控制消息
     */
    @OnMessage
    @SuppressWarnings("unused")
    public void onMessage(String message, Session session, @PathParam("coursewareId") Integer coursewareId) {
        log.info("[收到客户端消息] 会话ID: {}, 消息: {}", coursewareId, message);

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
    public void onClose(Session session, @PathParam("coursewareId") Integer coursewareId) {
        log.info("[AI生成状态连接关闭] 会话ID: {}", coursewareId);
        SESSIONS.remove(coursewareId);

    }

    @SuppressWarnings("unused")
    @OnError
    public void onError(Session session, @PathParam("coursewareId") Integer coursewareId, Throwable throwable) {
        log.error("[AI生成状态连接错误] 会话ID: {}", coursewareId, throwable);
        SESSIONS.remove(coursewareId);

    }

    public static class Configurator extends ServerEndpointConfig.Configurator {
        @Override
        public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
            sec.getUserProperties().put("maxIdleTimeout", 600000);
        }
    }
}
