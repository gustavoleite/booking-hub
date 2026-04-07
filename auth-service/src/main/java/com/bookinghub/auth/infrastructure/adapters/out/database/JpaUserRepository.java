package com.bookinghub.auth.infrastructure.adapters.out.database;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaUserRepository extends JpaRepository<UserEntity, UUID> {
  Optional<UserEntity> findByEmail(String email);

  boolean existsByEmail(String email);
}
