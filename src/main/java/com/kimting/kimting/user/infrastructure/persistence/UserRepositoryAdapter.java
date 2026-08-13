package com.kimting.kimting.user.infrastructure.persistence;

import com.kimting.kimting.user.domain.User;
import com.kimting.kimting.user.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final UserJpaRepository jpa;

    @Override public User save(User user) { return jpa.save(user); }
    @Override public Optional<User> findById(UUID id) { return jpa.findById(id); }
    @Override public Optional<User> findByEmail(String email) { return jpa.findByEmail(email); }
    @Override public boolean existsByEmail(String email) { return jpa.existsByEmail(email); }
}
