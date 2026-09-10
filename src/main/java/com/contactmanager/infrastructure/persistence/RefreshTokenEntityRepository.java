package com.contactmanager.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface RefreshTokenEntityRepository extends JpaRepository<RefreshTokenEntity, UUID> {

    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);

    void deleteByTokenHash(String tokenHash);
}
