package com.divyam.advent.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-IP rate limiter on a handful of auth-adjacent endpoints.
 *
 * <p>Why these paths specifically:
 * <ul>
 *   <li>{@code POST /auth/ensure-user} — would otherwise trivially hammer
 *       Clerk JWKS + create-or-update DB writes.</li>
 *   <li>{@code POST /user-challenges/start} — creating challenge records.</li>
 * </ul>
 *
 * <p>Buckets live in-memory; for a multi-replica deployment switch to a
 * shared store (Redis). Default ceiling: 30 requests / minute / IP.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final List<String> RATE_LIMITED_PREFIXES = List.of(
            "/auth/ensure-user",
            "/user-challenges/start"
    );

    private static final Bandwidth LIMIT = Bandwidth.builder()
            .capacity(30)
            .refillIntervally(30, Duration.ofMinutes(1))
            .build();

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) return true;
        String uri = request.getRequestURI();
        for (String prefix : RATE_LIMITED_PREFIXES) {
            if (uri.startsWith(prefix)) return false;
        }
        return true;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String key = clientKey(request);
        Bucket bucket = buckets.computeIfAbsent(key, k -> Bucket.builder().addLimit(LIMIT).build());
        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
            return;
        }
        response.setStatus(429); // 429 Too Many Requests — not a SC_* constant in the Servlet API

        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"RATE_LIMITED\",\"message\":\"Too many requests, slow down.\"}");
    }

    private static String clientKey(HttpServletRequest request) {
        String fwd = request.getHeader("X-Forwarded-For");
        if (fwd != null && !fwd.isBlank()) {
            int comma = fwd.indexOf(',');
            return (comma > 0 ? fwd.substring(0, comma) : fwd).trim();
        }
        return request.getRemoteAddr();
    }
}
