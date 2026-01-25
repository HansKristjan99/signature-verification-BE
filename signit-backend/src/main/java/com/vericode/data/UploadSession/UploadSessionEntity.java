package com.vericode.data.UploadSession;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "upload_sessions")
public class UploadSessionEntity {

    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @Column(name = "user_id", nullable = false, columnDefinition = "UUID")
    private UUID userId;

    @Column(name = "s3_key_prefix", nullable = false, length = 500)
    private String s3KeyPrefix;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used", nullable = false)
    private boolean used;

    // Hibernate expects entities to have a no-arg constructor
    protected UploadSessionEntity() {}

    /**
     * Constructor for creating a new upload session.
     *
     * @param userId The user who is creating this upload session
     * @param durationMinutes How long the session is valid (typically 10 minutes)
     */
    public UploadSessionEntity(UUID userId, int durationMinutes) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = this.createdAt.plusMinutes(durationMinutes);
        this.used = false;
        this.generateS3KeyPrefix();
    }

    /**
     * Checks if this upload session has expired.
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    /**
     * Builds the S3 key prefix path for this session.
     * Must be called after entity ID is generated.
     */
    public void generateS3KeyPrefix() {
        if (this.id == null) {
            throw new IllegalStateException("Cannot generate S3 key prefix before entity ID is generated");
        }
        this.s3KeyPrefix = "uploads/" + this.id.toString() + "/";
    }

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getS3KeyPrefix() {
        return s3KeyPrefix;
    }

    public void setS3KeyPrefix(String s3KeyPrefix) {
        this.s3KeyPrefix = s3KeyPrefix;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }
}
