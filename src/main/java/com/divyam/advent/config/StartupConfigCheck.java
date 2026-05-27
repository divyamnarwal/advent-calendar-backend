package com.divyam.advent.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Fail-fast on dangerous config defaults so the app refuses to come up with a
 * half-configured production environment.
 *
 * <p>Two tiers:
 * <ul>
 *   <li><b>Hard fail (ERROR + exit)</b> — Clerk JWT verification is enabled
 *       but the issuer is still the placeholder, OR Cloudinary credentials
 *       are blank (every authenticated photo flow would 500).</li>
 *   <li><b>Warn</b> — admin user id list is empty (no super-admin can be
 *       bootstrapped), or CORS uses the dev default in a non-dev environment.</li>
 * </ul>
 */
@Component
public class StartupConfigCheck {

    private static final Logger log = LoggerFactory.getLogger(StartupConfigCheck.class);
    private static final String PLACEHOLDER_ISSUER = "https://your-clerk-domain.clerk.accounts.dev";
    private static final String DEV_CORS = "http://localhost:3000";

    @Value("${clerk.jwt.enabled:false}")
    private boolean clerkEnabled;

    @Value("${clerk.jwt.issuer:}")
    private String clerkIssuer;

    @Value("${cloudinary.cloud-name:}")
    private String cloudinaryCloudName;

    @Value("${cloudinary.api-key:}")
    private String cloudinaryApiKey;

    @Value("${cloudinary.api-secret:}")
    private String cloudinaryApiSecret;

    @Value("${admin.clerk-user-id:}")
    private String superAdminClerkIds;

    @Value("${cors.allowed-origins:}")
    private String corsAllowedOrigins;

    @EventListener(ApplicationReadyEvent.class)
    public void check() {
        // Local-only / test profile? Skip strict checks; Clerk-disabled mode
        // is effectively read-only anyway (SecurityConfig denyAll).
        if (!clerkEnabled) {
            log.warn("[Startup] clerk.jwt.enabled=false — running in read-only mode. Do NOT use for production.");
            return;
        }

        List<String> fatal = new ArrayList<>();
        if (clerkIssuer == null || clerkIssuer.isBlank() || PLACEHOLDER_ISSUER.equals(clerkIssuer.trim())) {
            fatal.add("clerk.jwt.issuer is unset or still the placeholder (" + PLACEHOLDER_ISSUER + ")");
        }
        if (cloudinaryCloudName == null || cloudinaryCloudName.isBlank()) {
            fatal.add("cloudinary.cloud-name is blank");
        }
        if (cloudinaryApiKey == null || cloudinaryApiKey.isBlank()) {
            fatal.add("cloudinary.api-key is blank");
        }
        if (cloudinaryApiSecret == null || cloudinaryApiSecret.isBlank()) {
            fatal.add("cloudinary.api-secret is blank");
        }

        if (!fatal.isEmpty()) {
            String msg = "Fatal config problems — refusing to serve traffic:\n  - "
                    + String.join("\n  - ", fatal);
            log.error("[Startup] {}", msg);
            throw new IllegalStateException(msg);
        }

        if (superAdminClerkIds == null || superAdminClerkIds.isBlank()) {
            log.warn("[Startup] admin.clerk-user-id is empty — no super-admin can be bootstrapped via env.");
        }
        if (corsAllowedOrigins != null && corsAllowedOrigins.trim().equals(DEV_CORS)) {
            log.warn("[Startup] cors.allowed-origins still points at {} — looks like the dev default.", DEV_CORS);
        }
    }
}
