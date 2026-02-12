package com.divyam.advent.dto;

public class PhotoUploadSignatureResponse {
    private String cloudName;
    private String apiKey;
    private String folder;
    private long timestamp;
    private String signature;

    public PhotoUploadSignatureResponse() {
    }

    public PhotoUploadSignatureResponse(
            String cloudName,
            String apiKey,
            String folder,
            long timestamp,
            String signature
    ) {
        this.cloudName = cloudName;
        this.apiKey = apiKey;
        this.folder = folder;
        this.timestamp = timestamp;
        this.signature = signature;
    }

    public String getCloudName() {
        return cloudName;
    }

    public void setCloudName(String cloudName) {
        this.cloudName = cloudName;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
