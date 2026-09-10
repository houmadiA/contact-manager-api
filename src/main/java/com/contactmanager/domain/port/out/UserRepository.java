package com.contactmanager.domain.port.out;

import com.contactmanager.domain.model.User;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    Optional<User> findByUsername(String username);

    Optional<User> findById(UUID id);
}
