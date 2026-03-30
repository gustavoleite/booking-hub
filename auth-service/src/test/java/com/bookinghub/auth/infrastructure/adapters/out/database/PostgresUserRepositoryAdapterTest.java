package com.bookinghub.auth.infrastructure.adapters.out.database;

import com.bookinghub.auth.core.domain.Role;
import com.bookinghub.auth.core.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PostgresUserRepositoryAdapterTest {

    private JpaUserRepository jpaUserRepository;
    private PostgresUserRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        jpaUserRepository = Mockito.mock(JpaUserRepository.class);
        adapter = new PostgresUserRepositoryAdapter(jpaUserRepository);
    }

    @Test
    void shouldSaveUser() {
        User user = new User(UUID.randomUUID(), "test@example.com", "hashed", Set.of(Role.ROLE_CLIENT), true);
        UserEntity entity = new UserEntity();
        entity.setId(user.getId());
        entity.setEmail(user.getEmail());
        
        when(jpaUserRepository.save(any(UserEntity.class))).thenReturn(entity);

        User saved = adapter.save(user);

        assertNotNull(saved);
        assertEquals(user.getId(), saved.getId());
        verify(jpaUserRepository).save(any(UserEntity.class));
    }

    @Test
    void shouldFindByEmail() {
        String email = "test@example.com";
        UserEntity entity = new UserEntity();
        entity.setId(UUID.randomUUID());
        entity.setEmail(email);
        entity.setRoles(Set.of(Role.ROLE_CLIENT));

        when(jpaUserRepository.findByEmail(email)).thenReturn(Optional.of(entity));

        Optional<User> result = adapter.findByEmail(email);

        assertTrue(result.isPresent());
        assertEquals(email, result.get().getEmail());
    }

    @Test
    void shouldReturnEmptyWhenNotFoundByEmail() {
        when(jpaUserRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        Optional<User> result = adapter.findByEmail("nonexistent@example.com");

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldCheckIfExistsByEmail() {
        String email = "test@example.com";
        when(jpaUserRepository.existsByEmail(email)).thenReturn(true);

        boolean exists = adapter.existsByEmail(email);

        assertTrue(exists);
        verify(jpaUserRepository).existsByEmail(email);
    }
}
