package com.bookinghub.auth.infrastructure.adapters.in.rest;

import com.bookinghub.auth.application.dto.LoginRequestDTO;
import com.bookinghub.auth.application.dto.RegisterRequestDTO;
import com.bookinghub.auth.core.domain.Role;
import com.bookinghub.auth.core.domain.User;
import com.bookinghub.auth.core.usecases.AuthenticateUserUseCase;
import com.bookinghub.auth.core.usecases.RegisterUserUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest {

    private MockMvc mockMvc;
    private RegisterUserUseCase registerUserUseCase;
    private AuthenticateUserUseCase authenticateUserUseCase;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        registerUserUseCase = Mockito.mock(RegisterUserUseCase.class);
        authenticateUserUseCase = Mockito.mock(AuthenticateUserUseCase.class);
        objectMapper = new ObjectMapper();
        
        AuthController authController = new AuthController(registerUserUseCase, authenticateUserUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    void shouldRegisterUser() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO("test@example.com", "Password123", Role.ROLE_CLIENT);
        User user = new User(UUID.randomUUID(), request.email(), "hashed", Set.of(request.role()), true);

        when(registerUserUseCase.execute(anyString(), anyString(), any(Role.class))).thenReturn(user);

        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(user.getId().toString()))
                .andExpect(jsonPath("$.email").value(user.getEmail()));
    }

    @Test
    void shouldLoginUser() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("test@example.com", "password123");
        String token = "valid.jwt.token";

        when(authenticateUserUseCase.execute(anyString(), anyString())).thenReturn(token);

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(token))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }
}
