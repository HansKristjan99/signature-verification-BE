package com.vericode.signit.dto;

public class PrepareSigningRequest {
    private String filename;
    private String certificate; // Base64 DER
    private SignatureAlgorithmDto[] supportedSignatureAlgorithms;

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getCertificate() { return certificate; }
    public void setCertificate(String certificate) { this.certificate = certificate; }

    public SignatureAlgorithmDto[] getSupportedSignatureAlgorithms() { return supportedSignatureAlgorithms; }
    public void setSupportedSignatureAlgorithms(SignatureAlgorithmDto[] supportedSignatureAlgorithms) {
        this.supportedSignatureAlgorithms = supportedSignatureAlgorithms;
    }
}
