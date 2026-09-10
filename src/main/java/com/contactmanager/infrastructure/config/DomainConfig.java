package com.contactmanager.infrastructure.config;

import com.contactmanager.domain.port.in.AuthenticationService;
import com.contactmanager.domain.port.in.ContactService;
import com.contactmanager.domain.port.out.AccessTokenIssuer;
import com.contactmanager.domain.port.out.ContactCsvReader;
import com.contactmanager.domain.port.out.ContactCsvWriter;
import com.contactmanager.domain.port.out.ContactRepository;
import com.contactmanager.domain.port.out.PasswordHasher;
import com.contactmanager.domain.port.out.RefreshTokenGenerator;
import com.contactmanager.domain.port.out.RefreshTokenStore;
import com.contactmanager.domain.port.out.UserRepository;
import com.contactmanager.domain.service.AuthenticationServiceImpl;
import com.contactmanager.domain.service.ContactServiceImpl;
import com.contactmanager.infrastructure.security.BCryptPasswordHasher;
import com.contactmanager.infrastructure.security.SecureRandomRefreshTokenGenerator;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DomainConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    ContactService contactService(
            ContactRepository contactRepository,
            ContactCsvReader csvReader,
            ContactCsvWriter csvWriter,
            Clock clock) {
        return new ContactServiceImpl(contactRepository, csvReader, csvWriter, clock);
    }

    @Bean
    AuthenticationService authenticationService(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            AccessTokenIssuer accessTokenIssuer,
            RefreshTokenStore refreshTokenStore,
            RefreshTokenGenerator refreshTokenGenerator,
            Clock clock,
            ContactManagerProperties properties) {
        return new AuthenticationServiceImpl(
                userRepository,
                passwordHasher,
                accessTokenIssuer,
                refreshTokenStore,
                refreshTokenGenerator,
                clock,
                properties.jwt().refreshTokenTtl());
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    PasswordHasher passwordHasher(PasswordEncoder passwordEncoder) {
        return new BCryptPasswordHasher(passwordEncoder);
    }

    @Bean
    RefreshTokenGenerator refreshTokenGenerator() {
        return new SecureRandomRefreshTokenGenerator();
    }
}
