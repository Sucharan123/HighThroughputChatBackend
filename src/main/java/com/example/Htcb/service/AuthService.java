package com.example.Htcb.service;

import com.example.Htcb.entity.User;
import com.example.Htcb.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User register(String username, String rawPassword)
    {
        if (userRepository.findByUsername(username).isPresent())
        {
            throw new RuntimeException("Username already taken");
        }
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        //password encoder runs on Bcrypt shredder on the plain password
        //before saving
        return userRepository.save(user);
    }

    public User validateLogin(String username, String rawPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new RuntimeException("Invalid username or password");
        }
        return user;
    }
}