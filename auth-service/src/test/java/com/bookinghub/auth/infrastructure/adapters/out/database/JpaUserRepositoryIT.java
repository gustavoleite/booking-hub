package com.bookinghub.auth.infrastructure.adapters.out.database;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class JpaUserRepositoryIT {

    @Autowired
    private JpaUserRepository userRepository;

    @Test
    void shouldSaveAndFindUser() {
        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setPasswordHash("hash");
        
        UserEntity savedUser = userRepository.save(user);
        
        assertThat(savedUser.getId()).isNotNull();
        assertThat(userRepository.findByEmail("test@example.com")).isPresent();
    }
}
