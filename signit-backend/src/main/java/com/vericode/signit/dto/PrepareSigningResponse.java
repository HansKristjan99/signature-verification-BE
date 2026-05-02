package com.vericode.signit.dto;

public class PrepareSigningResponse {
    private final String signingSessionId;
    private final String hash;         // Base64-encoded digest bytes
    private final String hashFunction; // "SHA-256", "SHA-384", etc.

    public PrepareSigningResponse(String signingSessionId, String hash, String hashFunction) {
        this.signingSessionId = signingSessionId;
        this.hash = hash;
        this.hashFunction = hashFunction;
    }

    public String getSigningSessionId() { return signingSessionId; }
    public String getHash() { return hash; }
    public String getHashFunction() { return hashFunction; }
}
