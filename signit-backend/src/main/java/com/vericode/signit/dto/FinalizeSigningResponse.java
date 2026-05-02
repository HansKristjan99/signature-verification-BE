package com.vericode.signit.dto;

public class FinalizeSigningResponse {
    private final boolean success;
    private final String filename; // S3 key of new signed container

    public FinalizeSigningResponse(boolean success, String filename) {
        this.success = success;
        this.filename = filename;
    }

    public boolean isSuccess() { return success; }
    public String getFilename() { return filename; }
}
