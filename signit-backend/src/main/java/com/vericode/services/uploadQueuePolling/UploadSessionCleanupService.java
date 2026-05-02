package com.vericode.services.uploadQueuePolling;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vericode.data.UploadSession.UploadSessionRepository;

/**
 * Service to periodically clean up expired upload sessions.
 * Runs every hour to delete sessions that have expired.
 */
@Service
public class UploadSessionCleanupService {

    private static final Logger logger = LoggerFactory.getLogger(UploadSessionCleanupService.class);

    private final UploadSessionRepository uploadSessionRepository;

    @Autowired
    public UploadSessionCleanupService(UploadSessionRepository uploadSessionRepository) {
        this.uploadSessionRepository = uploadSessionRepository;
    }

    /**
     * Scheduled task that runs every hour to clean up expired upload sessions.
     * Deletes all sessions where expires_at < current time.
     */
    @Scheduled(fixedRate = 3600000)  // Run every hour (3600000 ms)
    @Transactional
    public void cleanupExpiredSessions() {
        try {
            logger.info("Starting cleanup of expired upload sessions...");

            LocalDateTime now = LocalDateTime.now();
            uploadSessionRepository.deleteByExpiresAtBefore(now);

            logger.info("Completed cleanup of expired upload sessions");
        } catch (Exception e) {
            logger.error("Error during upload session cleanup: {}", e.getMessage(), e);
        }
    }
}
