package com.vericode.signit.dto;

public class FinalizeSigningRequest {
    private String signingSessionId;
    private String signature; // Base64 signature bytes from Web-eID

    public String getSigningSessionId() { return signingSessionId; }
    public void setSigningSessionId(String signingSessionId) { this.signingSessionId = signingSessionId; }

    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }
}
