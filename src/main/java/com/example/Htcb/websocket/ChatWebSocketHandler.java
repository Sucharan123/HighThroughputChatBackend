package com.example.Htcb.websocket;

import com.example.Htcb.dto.ChatMessageRequest;
import com.example.Htcb.security.JwtUtil;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<Long, WebSocketSession> activeSessions = new ConcurrentHashMap<>();
    private final Map<Long, Long> userRooms = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = extractUserId(session);
        if (userId != null) {
            activeSessions.put(userId, session);
            System.out.println("User connected: " + userId + " | Total online (this instance): " + activeSessions.size());
        } else {
            try {
                session.close(CloseStatus.NOT_ACCEPTABLE);
            } catch (Exception ignored) {}
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        Long senderId = extractUserId(session);
        if (senderId == null) return;

        try {
            ChatMessageRequest chatMessage = objectMapper.readValue(message.getPayload(), ChatMessageRequest.class);
            Long roomId = chatMessage.getRoomId();
            if (roomId == null) return;

            userRooms.put(senderId, roomId);

            String outgoingPayload = objectMapper.writeValueAsString(
                    new OutgoingMessage(senderId, roomId, chatMessage.getContent())
            );

            // Instead of delivering locally, PUBLISH to Redis. Every instance (including this
            // one) will receive it back via RedisMessageSubscriber and deliver to its own local users.
            redisTemplate.convertAndSend("room:" + roomId, outgoingPayload);

            System.out.println("Published to room:" + roomId + " -> " + outgoingPayload);

        } catch (Exception e) {
            System.out.println("Failed to process message from user " + senderId + ": " + e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = extractUserId(session);
        if (userId != null) {
            activeSessions.remove(userId);
            userRooms.remove(userId);
            System.out.println("User disconnected: " + userId);
        }
    }

    // Called by RedisMessageSubscriber whenever ANY instance publishes to a room this instance cares about
    public void deliverToLocalUsersInRoom(Long roomId, String payload) {
        for (Map.Entry<Long, Long> entry : userRooms.entrySet()) {
            Long recipientUserId = entry.getKey();
            Long recipientRoom = entry.getValue();

            if (recipientRoom.equals(roomId)) {
                WebSocketSession recipientSession = activeSessions.get(recipientUserId);
                if (recipientSession != null && recipientSession.isOpen()) {
                    try {
                        recipientSession.sendMessage(new TextMessage(payload));
                    } catch (Exception e) {
                        System.out.println("Failed to deliver to user " + recipientUserId + ": " + e.getMessage());
                    }
                }
            }
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

    private record OutgoingMessage(Long senderId, Long roomId, String content) {}
}