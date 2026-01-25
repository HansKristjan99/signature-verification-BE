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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.vericode.data.User.UserEntity;
import com.vericode.data.User.UserRepository;
import com.vericode.data.UserSession.UserSessionEntity;
import com.vericode.data.UserSession.UserSessionRepository;
import com.vericode.signit.security.AuditLogger;

import jakarta.servlet.http.HttpServletRequest;

@CrossOrigin(origins= "*")
@Controller
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogger auditLogger;
    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired
	public UserController(UserRepository userRepository, UserSessionRepository userSessionRepository,
                         PasswordEncoder passwordEncoder, AuditLogger auditLogger) {
		this.userRepository = userRepository;
		this.userSessionRepository = userSessionRepository;
		this.passwordEncoder = passwordEncoder;
		this.auditLogger = auditLogger;
	}

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestParam String email, @RequestParam String password,
                                          HttpServletRequest request) {
        String clientIp = AuditLogger.getClientIp(request);

        // Input validation
        if (email == null || email.trim().isEmpty()) {
            auditLogger.logRegistrationFailure(email, clientIp, "Empty email");
            return ResponseEntity.status(400).body("Email is required");
        }

        if (password == null || password.length() < 8) {
            auditLogger.logRegistrationFailure(email, clientIp, "Weak password");
            return ResponseEntity.status(400).body("Password must be at least 8 characters");
        }

        UserEntity existingUser = userRepository.findByEmail(email);
        if (existingUser != null) {
            auditLogger.logRegistrationFailure(email, clientIp, "Email already exists");
            return ResponseEntity.status(400).body("User with this email already exists");
        }

        // Hash password with BCrypt (secure, salted, adaptive)
        String passwordHash = passwordEncoder.encode(password);
        userRepository.save(new UserEntity(email, passwordHash));

        auditLogger.logRegistrationSuccess(email, clientIp);
        return ResponseEntity.ok().body("Registration successful");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam String email, @RequestParam String password,
                                       HttpServletRequest request) {
        String clientIp = AuditLogger.getClientIp(request);

        // Input validation
        if (email == null || email.trim().isEmpty()) {
            auditLogger.logLoginFailure(email, clientIp, "Empty email");
            return ResponseEntity.status(400).body("Email is required");
        }

        if (password == null || password.isEmpty()) {
            auditLogger.logLoginFailure(email, clientIp, "Empty password");
            return ResponseEntity.status(400).body("Password is required");
        }

        UserEntity existingUser = userRepository.findByEmail(email);
        if (existingUser == null) {
            auditLogger.logLoginFailure(email, clientIp, "User not found");
            return ResponseEntity.status(401).body("Invalid credentials");
        }

        String storedHash = existingUser.getPasswordHash();
        boolean passwordMatches = false;
        boolean needsMigration = false;

        // Check if password hash is BCrypt format (starts with $2a$, $2b$, or $2y$)
        if (storedHash.startsWith("$2")) {
            // Modern BCrypt hash - verify directly
            passwordMatches = passwordEncoder.matches(password, storedHash);
        } else {
            // Legacy SHA-256 hash - verify and migrate to BCrypt
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
                String passwordHash = HexFormat.of().formatHex(hash);

                passwordMatches = storedHash.equals(passwordHash);
                if (passwordMatches) {
                    needsMigration = true; // Flag for transparent migration
                }
            } catch (NoSuchAlgorithmException e) {
                auditLogger.logLoginFailure(email, clientIp, "Hash algorithm error");
                return ResponseEntity.status(500).body("Internal server error");
            }
        }

        if (!passwordMatches) {
            auditLogger.logLoginFailure(email, clientIp, "Incorrect password");
            return ResponseEntity.status(401).body("Invalid credentials");
        }

        // Transparent migration: upgrade legacy SHA-256 to BCrypt
        if (needsMigration) {
            String newBcryptHash = passwordEncoder.encode(password);
            existingUser.setPasswordHash(newBcryptHash);
            // Will be saved below with lastLogin update
        }

        // Generate secure random session token
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        String sessionToken = HexFormat.of().formatHex(tokenBytes);

        // Create session with 30-minute expiration (sliding window)
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);
        UserSessionEntity session = new UserSessionEntity(sessionToken, existingUser, expiresAt);
        userSessionRepository.save(session);

        // Update last login timestamp (and password hash if migrated)
        existingUser.setLastLogin(new Timestamp(System.currentTimeMillis()));
        userRepository.save(existingUser);

        auditLogger.logLoginSuccess(email, clientIp);
        auditLogger.logSessionCreated(email, clientIp, sessionToken);

        return ResponseEntity.ok().body(sessionToken);
    }
    


    
}
