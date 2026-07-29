package com.superyoudi.coursewareservice.websocket;

import jakarta.websocket.*;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.websocket.server.ServerEndpointConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@ServerEndpoint(value = "/generate", configurator = HtmlEndpoint.Configurator.class)
public class HtmlEndpoint {

    private static final ConcurrentHashMap<Integer, Session> SESSIONS = new ConcurrentHashMap<>();

    public static void notifyHtmlGeneratingStatus(String message) {
        Session session = SESSIONS.get(1);
        if (session != null && session.isOpen()) {
            log.info("[通知HTML生成中] {}", message);
            sendMessage(session, message);
        } else {
            log.warn("[无法通知HTML生成中] 对应的连接不存在或已关闭, message: {}", message);
        }
    }

    private static void sendMessage(Session session, String message) {
        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            log.error("发送消息失败", e);
        }
    }

    @OnMessage
    public void onMessage(String message, Session session, @PathParam("userId") Integer userId) {
        log.info("[收到客户端消息] 会话ID: {}, 消息: {}", userId, message);
    }

    @OnOpen
    public void onOpen(Session session) {
        SESSIONS.put(1, session);

        sendMessage(session, "连接已建立");
        log.info("[HTML连接建立]");
    }

    @OnClose
    public void onClose(Session session) {
        SESSIONS.remove(1);
        log.info("[HTML连接关闭]");
    }

    public static class Configurator extends ServerEndpointConfig.Configurator {
        @Override
        public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
            sec.getUserProperties().put("maxIdleTimeout", 600000);
        }
    }
}
