package com.vericode.signit.signing;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SigningSessionStore {
    private final ConcurrentHashMap<String, SigningSession> sessions = new ConcurrentHashMap<>();

    public void store(String id, SigningSession s) { sessions.put(id, s); }
    public Optional<SigningSession> get(String id) { return Optional.ofNullable(sessions.get(id)); }
    public void remove(String id) { sessions.remove(id); }

    @Scheduled(fixedDelay = 60_000)
    public void evictExpired() {
        Instant cutoff = Instant.now().minusSeconds(300); // 5 min TTL
        sessions.entrySet().removeIf(e -> e.getValue().getCreatedAt().isBefore(cutoff));
    }
}
