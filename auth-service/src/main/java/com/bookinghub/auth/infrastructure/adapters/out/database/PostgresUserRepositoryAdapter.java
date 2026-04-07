package com.bookinghub.auth.infrastructure.adapters.out.database;

import com.bookinghub.auth.core.domain.User;
import com.bookinghub.auth.core.ports.UserRepository;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class PostgresUserRepositoryAdapter implements UserRepository {

  private final JpaUserRepository jpaUserRepository;

  public PostgresUserRepositoryAdapter(JpaUserRepository jpaUserRepository) {
    this.jpaUserRepository = jpaUserRepository;
  }

  @Override
  public User save(User user) {
    UserEntity entity = toEntity(user);
    UserEntity saved = jpaUserRepository.save(entity);
    return toDomain(saved);
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return jpaUserRepository.findByEmail(email).map(this::toDomain);
  }

  @Override
  public boolean existsByEmail(String email) {
    return jpaUserRepository.existsByEmail(email);
  }

  private UserEntity toEntity(User user) {
    UserEntity entity = new UserEntity();
    entity.setId(user.getId());
    entity.setEmail(user.getEmail());
    entity.setPasswordHash(user.getPasswordHash());
    entity.setRoles(user.getRoles());
    entity.setActive(user.isActive());
    return entity;
  }

  private User toDomain(UserEntity entity) {
    return new User(
        entity.getId(),
        entity.getEmail(),
        entity.getPasswordHash(),
        entity.getRoles(),
        entity.isActive()
    );
  }
}
