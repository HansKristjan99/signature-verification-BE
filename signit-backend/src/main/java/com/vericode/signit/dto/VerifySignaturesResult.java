package com.vericode.signit.dto;

import java.util.List;

public class VerifySignaturesResult {
    private boolean success;
    private String filename;
    private boolean valid;
    private String containerFormat;
    private int dataFileCount;
    private int signatureCount;
    private List<SignatureInfo> signatures;
    private List<String> containerErrors;
    private List<String> containerWarnings;
    private String error;
    private String errorType;
    private long timestamp;

    public VerifySignaturesResult() {}

    public VerifySignaturesResult(boolean success, String filename, boolean valid, String containerFormat,
                                 int dataFileCount, int signatureCount, List<SignatureInfo> signatures,
                                 List<String> containerErrors, List<String> containerWarnings,
                                 String error, String errorType, long timestamp) {
        this.success = success;
        this.filename = filename;
        this.valid = valid;
        this.containerFormat = containerFormat;
        this.dataFileCount = dataFileCount;
        this.signatureCount = signatureCount;
        this.signatures = signatures;
        this.containerErrors = containerErrors;
        this.containerWarnings = containerWarnings;
        this.error = error;
        this.errorType = errorType;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getContainerFormat() {
        return containerFormat;
    }

    public void setContainerFormat(String containerFormat) {
        this.containerFormat = containerFormat;
    }

    public int getDataFileCount() {
        return dataFileCount;
    }

    public void setDataFileCount(int dataFileCount) {
        this.dataFileCount = dataFileCount;
    }

    public int getSignatureCount() {
        return signatureCount;
    }

    public void setSignatureCount(int signatureCount) {
        this.signatureCount = signatureCount;
    }

    public List<SignatureInfo> getSignatures() {
        return signatures;
    }

    public void setSignatures(List<SignatureInfo> signatures) {
        this.signatures = signatures;
    }

    public List<String> getContainerErrors() {
        return containerErrors;
    }

    public void setContainerErrors(List<String> containerErrors) {
        this.containerErrors = containerErrors;
    }

    public List<String> getContainerWarnings() {
        return containerWarnings;
    }

    public void setContainerWarnings(List<String> containerWarnings) {
        this.containerWarnings = containerWarnings;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
