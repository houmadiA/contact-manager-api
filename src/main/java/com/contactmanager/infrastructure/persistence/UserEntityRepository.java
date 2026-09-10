package com.contactmanager.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface UserEntityRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByUsernameIgnoreCase(String username);
}
