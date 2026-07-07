package com.example.Htcb.websocket;
//This is kind of receptionist answering every call
import com.example.Htcb.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private JwtUtil jwtUtil;

    // Maps userId -> their active WebSocket session
    private final Map<Long, WebSocketSession> activeSessions = new ConcurrentHashMap<>();
    //Here we checks the Wristband of every user who log in
    //Whether they are legal users or not
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = extractUserId(session);
        if (userId != null) {
            activeSessions.put(userId, session);
            System.out.println("User connected: " + userId + " | Total online: " + activeSessions.size());
        } else {
            try {
                session.close(CloseStatus.NOT_ACCEPTABLE);
            } catch (Exception ignored) {}
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long senderId = extractUserId(session);
        String payload = message.getPayload();
        System.out.println("Message from user " + senderId + ": " + payload);

        // For now: broadcast to every connected user (we'll restrict this to "same room" logic
        // properly once Redis is introduced in Phase 3 — this simple version proves the wiring works)
        for (WebSocketSession s : activeSessions.values()) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage("User " + senderId + " says: " + payload));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = extractUserId(session);
        if (userId != null) {
            activeSessions.remove(userId);
            System.out.println("User disconnected: " + userId + " | Total online: " + activeSessions.size());
        }
    }

    private Long extractUserId(WebSocketSession session) {
        String query = session.getUri() != null ? session.getUri().getQuery() : null;
        if (query != null && query.startsWith("token=")) {
            String token = query.substring(6);
            if (jwtUtil.isTokenValid(token)) {
                return jwtUtil.extractUserId(token);
            }
        }
        return null;
    }
}