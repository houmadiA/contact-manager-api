package com.contactmanager.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "refresh_tokens")
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class RefreshTokenEntity {

    @Id
    private UUID id;

    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    UUID getUserId() {
        return userId;
    }

    Instant getExpiresAt() {
        return expiresAt;
    }
}
