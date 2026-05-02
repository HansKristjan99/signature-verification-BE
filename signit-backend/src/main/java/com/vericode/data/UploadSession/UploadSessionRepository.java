package com.vericode.data.UploadSession;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UploadSessionRepository extends CrudRepository<UploadSessionEntity, UUID> {

    /**
     * Find an upload session by its ID.
     * Used to validate upload sessions when files are uploaded.
     */
    Optional<UploadSessionEntity> findById(UUID id);

    /**
     * Delete all expired upload sessions.
     * Used by cleanup scheduled task.
     */
    void deleteByExpiresAtBefore(LocalDateTime dateTime);
}
