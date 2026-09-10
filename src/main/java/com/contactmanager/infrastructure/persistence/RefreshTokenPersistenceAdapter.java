package com.contactmanager.infrastructure.persistence;

import com.contactmanager.domain.model.RefreshToken;
import com.contactmanager.domain.port.out.RefreshTokenStore;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
@RequiredArgsConstructor
public class RefreshTokenPersistenceAdapter implements RefreshTokenStore {

    private final RefreshTokenEntityRepository refreshTokens;

    @Override
    public void save(RefreshToken refreshToken) {
        refreshTokens.save(new RefreshTokenEntity(
                UUID.randomUUID(),
                hash(refreshToken.token()),
                refreshToken.userId(),
                refreshToken.expiresAt()));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokens
                .findByTokenHash(hash(token))
                .map(entity -> new RefreshToken(
                        entity.getUserId(), token, entity.getExpiresAt()));
    }

    @Override
    public void delete(String token) {
        refreshTokens.deleteByTokenHash(hash(token));
    }

    private static String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required by every JVM", e);
        }
    }
}
