package com.superyoudi.coursewareservice.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.superyoudi.coursewareservice.websocket.dto.GenerationMessage;
import jakarta.websocket.*;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpointConfig;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI文件生成状态通知WebSocket接口
 * 用于向客户端实时推送AI文件生成的状态信息
 *
 * @author WuTao
 */
@Slf4j
//@Component
//@ServerEndpoint(value = "/generate/status/{userId}", configurator = AIGenerationStatusEndpoint.Configurator.class)
public class AIGenerationStatusEndpoint {

    // 存储所有连接的会话
    private static final ConcurrentHashMap<Integer, Session> SESSIONS = new ConcurrentHashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * AI调用此方法通知前端文件生成状态
     *
     * @param message 消息
     */
    public static void notifyFileGeneratingStatus(GenerationMessage message) {
        int userId = message.getUserId();
        Session session = SESSIONS.get(userId);
        if (session != null && session.isOpen()) {
            log.info("[通知文件生成中] 会话ID: {}, 文件名: {}", userId, message);
            // 拼接文件名，返回给前端：文件【xxx】正在生成
            sendMessage(session, message);
        } else {
            log.warn("[无法通知文件生成中] 会话ID {} 对应的连接不存在或已关闭, message: {}", userId, message);
        }
    }

    /**
     * AI调用此方法通知前端文件生成完成
     *
     * @param userId    目标会话ID
     * @param fileNames AI传递的键值对数据（如文件名、文件路径、文件大小等）
     */
    public static void notifyFileGenerateCompleted(Integer userId, Map<String, String> fileNames) {
        Session session = SESSIONS.get(userId);
        if (session != null && session.isOpen()) {
            log.info("[通知文件生成完成] 用户ID: {}, 传递数据: {}", userId, fileNames);

            sendMessage(session, GenerationMessage.ofFileInfo(userId, fileNames));
        } else {
            log.warn("[无法通知文件生成完成] 用户ID {} 对应的连接不存在或已关闭，传递数据: {}", userId, fileNames);
        }
    }

    /**
     * 发送Result对象给客户端（JSON格式）
     */
    private static void sendMessage(Session session, GenerationMessage generationMessage) {
        try {
            String json = objectMapper.writeValueAsString(generationMessage);
            log.info("[发送消息] 会话ID: {}, 用户ID: {}, 消息: {}", session.getId(), generationMessage.getUserId(), json);
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
     * @param session WebSocket会话
     * @param userId  会话ID，用于区分不同的客户端连接
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Integer userId) {
        log.info("[AI生成状态连接建立] 用户ID: {}", userId);

        session.setMaxIdleTimeout(600000);

        Session oldSession = SESSIONS.put(userId, session);

        // 关闭旧的连接
        if (oldSession != null && oldSession.isOpen()) {
            try {
                sendMessage(oldSession, GenerationMessage.ofMessage(userId, "重复的连接，此连接将被关闭"));
                oldSession.close();
            } catch (IOException e) {
                log.warn("关闭旧连接失败", e);
            }
        }

        sendMessage(session, GenerationMessage.ofMessage(userId, "连接已建立，等待AI生成状态通知"));
//        sendMessage(session, GenerationMessage.ofFileInfo(userId, Map.of(FileZipper.FILE_PPT, "《赤壁赋》——旷达心境与文言虚词探析.pptx")));
        //        Map<String, String> fileMap = new HashMap<>();
//        fileMap.put(FileZipper.FILE_PPT, "祝福1、2学时   课件.pptx");
//        fileMap.put(FileZipper.FILE_ZIP, "7b719cce-ec41-4d97-b4b8-62a4c80bc7e5.zip");
//        sendMessage(session, GenerationMessage.ofFileInfo(userId, fileMap));
    }

    /**
     * 当收到客户端消息时触发
     * 客户端可以发送心跳或其他控制消息
     */
    @OnMessage
    public void onMessage(String message, Session session, @PathParam("userId") Integer userId) {
        log.info("[收到客户端消息] 会话ID: {}, 消息: {}", userId, message);

        // 处理心跳消息
        if ("ping".equalsIgnoreCase(message)) {
            sendHeartbeat(session);
        }
    }

    /**
     * 当连接关闭时触发
     */
    @OnClose
    public void onClose(Session session, @PathParam("userId") Integer userId) {
        log.info("[AI生成状态连接关闭] 会话ID: {}", userId);
        SESSIONS.remove(userId);

    }

    @OnError
    public void onError(Session session, @PathParam("userId") Integer userId, Throwable throwable) throws IOException {
        log.error("[AI生成状态连接错误] 会话ID: {}", userId, throwable);
        SESSIONS.remove(userId);

    }

    public static class Configurator extends ServerEndpointConfig.Configurator {
        @Override
        public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
            sec.getUserProperties().put("maxIdleTimeout", 600000);
        }
    }
}
