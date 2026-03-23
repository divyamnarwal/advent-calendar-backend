package com.divyam.advent.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Service
public class CloudinaryCleanupService {

    private static final Logger logger = LoggerFactory.getLogger(CloudinaryCleanupService.class);

    private final String cloudName;
    private final String apiKey;
    private final String apiSecret;
    private final HttpClient httpClient;

    public CloudinaryCleanupService(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret
    ) {
        this.cloudName = cloudName;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.httpClient = HttpClient.newHttpClient();
    }

    public boolean destroyOnCloudinary(String publicId) {
        if (isBlank(publicId)) {
            logger.warn("Skipping Cloudinary destroy because publicId is blank");
            return false;
        }

        if (isBlank(cloudName) || isBlank(apiKey) || isBlank(apiSecret)) {
            logger.warn("Skipping Cloudinary destroy because Cloudinary is not configured");
            return false;
        }

        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String signature = sha1Hex("public_id=" + publicId + "&timestamp=" + timestamp + apiSecret);
        String requestBody = buildRequestBody(publicId, timestamp, signature);
        String endpoint = "https://api.cloudinary.com/v1_1/" + cloudName + "/image/destroy";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String normalizedBody = response.body()
                    .replace(" ", "")
                    .replace("\n", "")
                    .replace("\r", "");

            if (response.statusCode() >= 200 && response.statusCode() < 300
                    && normalizedBody.contains("\"result\":\"ok\"")) {
                return true;
            }

            logger.warn(
                    "Cloudinary destroy failed for publicId {} with status {} and body {}",
                    publicId,
                    response.statusCode(),
                    response.body()
            );
            return false;
        } catch (Exception ex) {
            logger.warn("Cloudinary destroy request failed for publicId {}", publicId, ex);
            return false;
        }
    }

    private String sha1Hex(String input) {
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            byte[] digest = sha1.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to generate Cloudinary cleanup signature", ex);
        }
    }

    private String buildRequestBody(String publicId, String timestamp, String signature) {
        return "public_id=" + encode(publicId)
                + "&api_key=" + encode(apiKey)
                + "&timestamp=" + encode(timestamp)
                + "&signature=" + encode(signature);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
