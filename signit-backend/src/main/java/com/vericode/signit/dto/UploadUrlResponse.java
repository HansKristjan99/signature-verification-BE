package com.vericode.signit.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Response DTO for GET /files/newUploadURL/ endpoint.
 * Returns both the pre-signed URL and the upload session ID.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UploadUrlResponse {

    private String uploadUrl;
    private String uploadSessionId;
    private String s3Key;  // The full S3 key where file should be uploaded
    private int expiresInSeconds;

    public UploadUrlResponse() {}

    public UploadUrlResponse(String uploadUrl, String uploadSessionId, String s3Key, int expiresInSeconds) {
        this.uploadUrl = uploadUrl;
        this.uploadSessionId = uploadSessionId;
        this.s3Key = s3Key;
        this.expiresInSeconds = expiresInSeconds;
    }

    // Getters and setters
    public String getUploadUrl() {
        return uploadUrl;
    }

    public void setUploadUrl(String uploadUrl) {
        this.uploadUrl = uploadUrl;
    }

    public String getUploadSessionId() {
        return uploadSessionId;
    }

    public void setUploadSessionId(String uploadSessionId) {
        this.uploadSessionId = uploadSessionId;
    }

    public String getS3Key() {
        return s3Key;
    }

    public void setS3Key(String s3Key) {
        this.s3Key = s3Key;
    }

    public int getExpiresInSeconds() {
        return expiresInSeconds;
    }

    public void setExpiresInSeconds(int expiresInSeconds) {
        this.expiresInSeconds = expiresInSeconds;
    }
}
