package com.contactmanager.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

interface ContactEntityRepository
        extends JpaRepository<ContactEntity, UUID>, JpaSpecificationExecutor<ContactEntity> {

    Optional<ContactEntity> findByEmailIgnoreCase(String email);
}
