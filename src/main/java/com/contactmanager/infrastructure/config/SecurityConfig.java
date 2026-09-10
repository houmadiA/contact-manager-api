package com.contactmanager.infrastructure.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(ContactManagerProperties.class)
public class SecurityConfig {

    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/auth/login", "/api/auth/refresh", "/api/auth/logout",
    };

    private static final String[] DOCUMENTATION_ENDPOINTS = {
            "/v3/api-docs", "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**",
    };

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http, CorsConfigurationSource corsConfigurationSource, BearerTokenResolver bearerTokenResolver)
            throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**")
                        .permitAll()
                        .requestMatchers(PUBLIC_ENDPOINTS)
                        .permitAll()
                        .requestMatchers(DOCUMENTATION_ENDPOINTS)
                        .permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**")
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.bearerTokenResolver(bearerTokenResolver)
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .build();
    }

    private static JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            List<String> roles = jwt.getClaimAsStringList("roles");
            if (roles == null) {
                return List.of();
            }
            return roles.stream()
                    .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role))
                    .toList();
        });
        return converter;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(ContactManagerProperties properties) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(properties.cors().allowedOrigins());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Content-Disposition"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
