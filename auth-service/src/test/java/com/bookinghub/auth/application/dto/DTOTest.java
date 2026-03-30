package com.bookinghub.auth.application.dto;

import com.bookinghub.auth.core.domain.Role;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class DTOTest {

    @Test
    void testLoginRequestDTO() {
        LoginRequestDTO dto = new LoginRequestDTO("test@example.com", "password");
        assertEquals("test@example.com", dto.email());
        assertEquals("password", dto.password());
    }

    @Test
    void testRegisterRequestDTO() {
        RegisterRequestDTO dto = new RegisterRequestDTO("test@example.com", "password", Role.ROLE_CLIENT);
        assertEquals("test@example.com", dto.email());
        assertEquals("password", dto.password());
        assertEquals(Role.ROLE_CLIENT, dto.role());
    }

    @Test
    void testTokenResponseDTO() {
        TokenResponseDTO dto = new TokenResponseDTO("token", 3600, "Bearer");
        assertEquals("token", dto.accessToken());
        assertEquals(3600, dto.expiresIn());
        assertEquals("Bearer", dto.tokenType());
    }

    @Test
    void testUserResponseDTO() {
        UUID id = UUID.randomUUID();
        UserResponseDTO dto = new UserResponseDTO(id, "test@example.com");
        assertEquals(id, dto.id());
        assertEquals("test@example.com", dto.email());
    }
}
