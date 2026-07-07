package com.example.Htcb.entity;

import jakarta.persistence.*;
import java.time.Instant;

//This class represents one message sent by one user inside one chatroom

@Entity
@Table(
        name = "messages",
        indexes = @Index(name = "idx_room_timestamp", columnList = "room_id, timestamp")
        //Based roomid+timestamps , we can find out the messages uniquely
)
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    //Don't immediately load the entire ChatRoom object when I fetch a Message. Load it only when needed.
    @JoinColumn(name = "room_id", nullable = false)
    //using chatRoom room_id as message room_id
    private ChatRoom room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private Instant timestamp = Instant.now();

    public Long getId()
    {
        return id;
    }
    public ChatRoom getRoom()
    {
        return room;
    }
    public void setRoom(ChatRoom room)
    {
        this.room = room;
    }
    public User getSender()
    {
        return sender;
    }
    public void setSender(User sender)
    {
        this.sender = sender;
    }
    public String getContent()
    {
        return content;
    }
    public void setContent(String content)
    {
        this.content = content;
    }
    public Instant getTimestamp()
    {
        return timestamp;
    }
}