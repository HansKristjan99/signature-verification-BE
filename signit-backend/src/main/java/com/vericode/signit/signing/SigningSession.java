package com.vericode.signit.signing;

import org.digidoc4j.Container;
import org.digidoc4j.DataToSign;

import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

public class SigningSession {
    private final String originalFilename;
    private final Path tempFilePath;
    private final Container container;
    private final DataToSign dataToSign;
    private final Instant createdAt;
    private final UUID userId;

    public SigningSession(String originalFilename, Path tempFilePath, Container container, DataToSign dataToSign, UUID userId) {
        this.originalFilename = originalFilename;
        this.tempFilePath = tempFilePath;
        this.container = container;
        this.dataToSign = dataToSign;
        this.createdAt = Instant.now();
        this.userId = userId;
    }

    public String getOriginalFilename() { return originalFilename; }
    public Path getTempFilePath() { return tempFilePath; }
    public Container getContainer() { return container; }
    public DataToSign getDataToSign() { return dataToSign; }
    public Instant getCreatedAt() { return createdAt; }
    public UUID getUserId() { return userId; }
}
