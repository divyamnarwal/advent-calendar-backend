package com.divyam.advent.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter jwtAuthenticationConverter,
            @Value("${clerk.jwt.enabled:false}") boolean clerkJwtEnabled
    ) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        if (clerkJwtEnabled) {
            http
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/health", "/error").permitAll()
                            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                            .requestMatchers(HttpMethod.GET, "/challenges", "/challenges/category/**").permitAll()
                            .requestMatchers(HttpMethod.GET, "/pulse/today").authenticated()
                            .anyRequest().authenticated()
                    )
                    .oauth2ResourceServer(
                            oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                    );
        } else {
            http
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/health", "/error").permitAll()
                            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                            .requestMatchers(HttpMethod.GET, "/challenges", "/challenges/category/**").permitAll()
                            .anyRequest().denyAll()
                    );
        }

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        return new JwtAuthenticationConverter();
    }

    @Bean
    @ConditionalOnProperty(name = "clerk.jwt.enabled", havingValue = "true")
    public JwtDecoder jwtDecoder(
            @Value("${clerk.jwt.issuer}") String issuer,
            @Value("${clerk.jwt.audience:}") String audience,
            @Value("${clerk.jwt.validate-audience:false}") boolean validateAudience
    ) {
        NimbusJwtDecoder decoder = (NimbusJwtDecoder) JwtDecoders.fromIssuerLocation(issuer);
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);

        List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();
        validators.add(withIssuer);

        if (validateAudience && audience != null && !audience.trim().isEmpty()) {
            OAuth2TokenValidator<Jwt> withAudience = jwt -> {
                if (jwt.getAudience() != null && jwt.getAudience().contains(audience)) {
                    return OAuth2TokenValidatorResult.success();
                }
                OAuth2Error error = new OAuth2Error(
                        "invalid_token",
                        "Token does not contain the required audience",
                        null
                );
                return OAuth2TokenValidatorResult.failure(error);
            };
            validators.add(withAudience);
        }

        decoder.setJwtValidator(new org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator<>(
                validators
        ));
        return decoder;
    }
}
