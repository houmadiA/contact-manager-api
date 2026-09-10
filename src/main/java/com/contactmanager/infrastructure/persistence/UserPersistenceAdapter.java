package com.contactmanager.infrastructure.persistence;

import com.contactmanager.domain.model.User;
import com.contactmanager.domain.port.out.UserRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserRepository {

    private final UserEntityRepository users;

    @Override
    public Optional<User> findByUsername(String username) {
        return users.findByUsernameIgnoreCase(username).map(UserPersistenceAdapter::toDomain);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return users.findById(id).map(UserPersistenceAdapter::toDomain);
    }

    private static User toDomain(UserEntity entity) {
        return new User(
                entity.getId(),
                entity.getUsername(),
                entity.getPasswordHash(),
                entity.getDisplayName(),
                entity.getRoles());
    }
}
