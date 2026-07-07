package com.example.Htcb.entity;

import jakarta.persistence.*;
//imports all the JPA annotations
import java.time.Instant;


@Entity //tells hibernate that this class should become a database table

@Table(name = "users")//tells table name
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
    //Instant is a datatype and Instant.now() return current time

    public Long getId() {
        return id;
    }
    public void setId(Long id)
    {
        this.id = id;
    }
    public String getUsername()
    {
        return username;
    }
    public void setUsername(String username)
    {
        this.username = username;
    }
    public String getPasswordHash()
    {
        return passwordHash;
    }
    public void setPasswordHash(String passwordHash)
    {
        this.passwordHash = passwordHash;
    }
    public Instant getCreatedAt()
    {
        return createdAt;
    }
}