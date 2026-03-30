package com.bookinghub.auth.infrastructure.adapters.out.jwt;

import com.bookinghub.auth.core.domain.Role;
import com.bookinghub.auth.core.domain.User;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class NimbusTokenGeneratorAdapterTest {

    private NimbusTokenGeneratorAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new NimbusTokenGeneratorAdapter();
        // Não setamos privateKeyContent nem privateKeyResource para testar o fallback (geração automática)
    }

    @Test
    void shouldGenerateValidToken() throws Exception {
        User user = new User(UUID.randomUUID(), "test@example.com", "hashed", Set.of(Role.ROLE_CLIENT), true);

        String token = adapter.generateToken(user);

        assertNotNull(token);
        assertFalse(token.isBlank());

        SignedJWT signedJWT = SignedJWT.parse(token);
        assertEquals(user.getId().toString(), signedJWT.getJWTClaimsSet().getSubject());
        assertEquals(user.getEmail(), signedJWT.getJWTClaimsSet().getClaim("email"));
        assertEquals("ROLE_CLIENT", signedJWT.getJWTClaimsSet().getClaim("role"));
    }

    @Test
    void shouldHandleCustomPrivateKey() throws Exception {
        // RSA 2048 private key in PKCS#8 format (dummy for test)
        // Gerando uma de verdade para garantir que o parser funciona
        java.security.KeyPairGenerator kpg = java.security.KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        java.security.PrivateKey privateKey = kpg.generateKeyPair().getPrivate();
        String encodedKey = java.util.Base64.getEncoder().encodeToString(privateKey.getEncoded());
        String pem = "-----BEGIN PRIVATE KEY-----\n" + encodedKey + "\n-----END PRIVATE KEY-----";

        ReflectionTestUtils.setField(adapter, "privateKeyContent", pem);

        User user = new User(UUID.randomUUID(), "test@example.com", "hashed", Set.of(Role.ROLE_CLIENT), true);
        String token = adapter.generateToken(user);

        assertNotNull(token);
        SignedJWT signedJWT = SignedJWT.parse(token);
        assertEquals(user.getId().toString(), signedJWT.getJWTClaimsSet().getSubject());
    }

    @Test
    void shouldFallbackWhenKeyIsInvalid() {
        ReflectionTestUtils.setField(adapter, "privateKeyContent", "invalid-key-content");

        User user = new User(UUID.randomUUID(), "test@example.com", "hashed", Set.of(Role.ROLE_CLIENT), true);
        
        // Deve funcionar pois faz fallback para geração automática
        String token = adapter.generateToken(user);
        assertNotNull(token);
    }
}
