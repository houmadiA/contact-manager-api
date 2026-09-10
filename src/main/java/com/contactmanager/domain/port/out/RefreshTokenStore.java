package com.contactmanager.domain.port.out;

import com.contactmanager.domain.model.RefreshToken;
import java.util.Optional;

public interface RefreshTokenStore {

    void save(RefreshToken refreshToken);

    Optional<RefreshToken> findByToken(String token);

    void delete(String token);
}
