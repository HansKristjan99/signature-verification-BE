package com.vericode.signit.dto;

import java.util.List;

public class SignatureInfo {
    private String id;
    private String signerName;
    private String signatureMethod;
    private String signingTime;
    private String city;
    private String stateOrProvince;
    private String postalCode;
    private String countryName;
    private boolean valid;
    private List<String> errors;
    private List<String> warnings;

    public SignatureInfo() {}

    public SignatureInfo(String id, String signerName, String signatureMethod, String signingTime,
                        String city, String stateOrProvince, String postalCode, String countryName,
                        boolean valid, List<String> errors, List<String> warnings) {
        this.id = id;
        this.signerName = signerName;
        this.signatureMethod = signatureMethod;
        this.signingTime = signingTime;
        this.city = city;
        this.stateOrProvince = stateOrProvince;
        this.postalCode = postalCode;
        this.countryName = countryName;
        this.valid = valid;
        this.errors = errors;
        this.warnings = warnings;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSignerName() {
        return signerName;
    }

    public void setSignerName(String signerName) {
        this.signerName = signerName;
    }

    public String getSignatureMethod() {
        return signatureMethod;
    }

    public void setSignatureMethod(String signatureMethod) {
        this.signatureMethod = signatureMethod;
    }

    public String getSigningTime() {
        return signingTime;
    }

    public void setSigningTime(String signingTime) {
        this.signingTime = signingTime;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStateOrProvince() {
        return stateOrProvince;
    }

    public void setStateOrProvince(String stateOrProvince) {
        this.stateOrProvince = stateOrProvince;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }
}
