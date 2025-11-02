package com.vericode.signit.controllers;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HexFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.vericode.data.User.UserEntity;
import com.vericode.data.User.UserRepository;
import com.vericode.data.UserSession.UserSessionEntity;
import com.vericode.data.UserSession.UserSessionRepository;

@CrossOrigin(origins= "*")
@Controller
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired
	public UserController(UserRepository userRepository, UserSessionRepository userSessionRepository) {
		this.userRepository = userRepository;
		this.userSessionRepository = userSessionRepository;
	}

    @PostMapping("/register") 
    public ResponseEntity<String> register(@RequestParam String email, @RequestParam String password)
    {
        UserEntity existingUser = userRepository.findByEmail(email);
        if (existingUser != null) {
            return ResponseEntity.status(400).body("User with this email already exists");
        }
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        String passwordHash = HexFormat.of().formatHex(hash);
        userRepository.save(new UserEntity(email, passwordHash));
        return ResponseEntity.ok().body("Registration successful");
        
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam String email, @RequestParam String password){
        UserEntity existingUser = userRepository.findByEmail(email);
        if (existingUser == null) {
            throw new IllegalArgumentException("User with this email does not exist");
        }
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        String passwordHash = HexFormat.of().formatHex(hash);
        if (!existingUser.getPasswordHash().equals(passwordHash)) {
            return ResponseEntity.status(401).body("Incorrect password");
        }

        // Generate secure random session token
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        String sessionToken = HexFormat.of().formatHex(tokenBytes);

        // Create session with 30-minute expiration (sliding window)
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);
        UserSessionEntity session = new UserSessionEntity(sessionToken, existingUser, expiresAt);
        userSessionRepository.save(session);

        // Update last login timestamp
        existingUser.setLastLogin(new Timestamp(System.currentTimeMillis()));
        userRepository.save(existingUser);

        return ResponseEntity.ok().body(sessionToken);
    }
    


    
}
