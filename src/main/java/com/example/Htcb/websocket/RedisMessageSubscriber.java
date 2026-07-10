package com.example.Htcb.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
public class RedisMessageSubscriber implements MessageListener {

    @Autowired
    private ChatWebSocketHandler chatWebSocketHandler;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel());
        String payload = new String(message.getBody());

        // channel looks like "room:1" — extract the numeric room ID
        Long roomId = Long.parseLong(channel.substring(channel.indexOf(":") + 1));

        chatWebSocketHandler.deliverToLocalUsersInRoom(roomId, payload);
    }
}