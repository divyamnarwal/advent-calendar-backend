package com.divyam.advent.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    // Set CORS_ALLOWED_ORIGINS env var in prod, e.g.:
    // CORS_ALLOWED_ORIGINS=https://yourapp.com,https://www.yourapp.com
    @Value("${cors.allowed-origins}")
    private String allowedOriginsRaw;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        String[] origins = allowedOriginsRaw.split(",");
        config.setAllowedOrigins(Arrays.asList(origins));
        config.addAllowedHeader("*");         // Allows all headers
        config.addAllowedMethod("*");         // Allows all methods (GET, POST, PUT, etc.)
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
