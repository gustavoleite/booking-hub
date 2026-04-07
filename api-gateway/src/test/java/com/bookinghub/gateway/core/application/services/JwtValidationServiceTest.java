package com.bookinghub.gateway.core.application.services;

import com.bookinghub.gateway.core.domain.exceptions.InvalidTokenException;
import com.bookinghub.gateway.core.domain.exceptions.JwtConfigurationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtValidationServiceTest {

    private JwtValidationService jwtValidationService;
    private KeyPair keyPair;

    @BeforeEach
    void setUp() throws NoSuchAlgorithmException {
        jwtValidationService = new JwtValidationService();
        
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        keyPair = keyPairGenerator.generateKeyPair();
    }

    @Test
    void shouldValidateTokenWhenPublicKeyProvidedAsContent() {
        // Given
        String publicKeyPem = "-----BEGIN PUBLIC KEY-----\n" +
                Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()) +
                "\n-----END PUBLIC KEY-----";
        
        ReflectionTestUtils.setField(jwtValidationService, "publicKeyContent", publicKeyPem);
        
        String token = Jwts.builder()
                .setSubject("user123")
                .claim("role", "ROLE_USER")
                .setExpiration(new Date(System.currentTimeMillis() + 100000))
                .signWith(keyPair.getPrivate(), SignatureAlgorithm.RS256)
                .compact();

        // When
        Claims claims = jwtValidationService.validateTokenAndGetClaims(token);

        // Then
        assertEquals("user123", claims.getSubject());
        assertEquals("ROLE_USER", claims.get("role"));
    }

    @Test
    void shouldValidateTokenWhenPublicKeyProvidedAsResource() {
        // Given
        String publicKeyPem = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        ByteArrayResource resource = new ByteArrayResource(publicKeyPem.getBytes());
        
        ReflectionTestUtils.setField(jwtValidationService, "publicKeyResource", resource);
        
        String token = Jwts.builder()
                .setSubject("user456")
                .signWith(keyPair.getPrivate(), SignatureAlgorithm.RS256)
                .compact();

        // When
        Claims claims = jwtValidationService.validateTokenAndGetClaims(token);

        // Then
        assertEquals("user456", claims.getSubject());
    }

    @Test
    void shouldThrowJwtConfigurationExceptionWhenPublicKeyIsMissing() {
        // Given
        ReflectionTestUtils.setField(jwtValidationService, "publicKeyContent", null);
        
        // When & Then
        JwtConfigurationException exception = assertThrows(JwtConfigurationException.class, () -> 
            jwtValidationService.validateTokenAndGetClaims("some-token")
        );
        assertEquals("JWT public key content or resource is missing", exception.getMessage());
    }

    @Test
    void shouldThrowJwtConfigurationExceptionWhenPublicKeyIsInvalid() {
        // Given
        ReflectionTestUtils.setField(jwtValidationService, "publicKeyContent", "invalid-key");
        
        // When & Then
        JwtConfigurationException exception = assertThrows(JwtConfigurationException.class, () -> 
            jwtValidationService.validateTokenAndGetClaims("some-token")
        );
        assertTrue(exception.getMessage().contains("Could not initialize public key"));
    }

    @Test
    void shouldThrowJwtConfigurationExceptionWhenResourceIsNullAndContentIsEmpty() {
        // Given
        ReflectionTestUtils.setField(jwtValidationService, "publicKeyResource", null);
        ReflectionTestUtils.setField(jwtValidationService, "publicKeyContent", "");

        // When & Then
        JwtConfigurationException exception = assertThrows(JwtConfigurationException.class, () ->
            jwtValidationService.validateTokenAndGetClaims("some-token")
        );
        assertEquals("JWT public key content or resource is missing", exception.getMessage());
    }

    @Test
    void shouldFallbackToContentWhenResourceDoesNotExist() {
        // Given
        String publicKeyPem = "-----BEGIN PUBLIC KEY-----\n" +
                Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()) +
                "\n-----END PUBLIC KEY-----";
        ReflectionTestUtils.setField(jwtValidationService, "publicKeyContent", publicKeyPem);

        org.springframework.core.io.Resource nonExistentResource = mock(org.springframework.core.io.Resource.class);
        when(nonExistentResource.exists()).thenReturn(false);
        ReflectionTestUtils.setField(jwtValidationService, "publicKeyResource", nonExistentResource);

        String token = Jwts.builder()
                .setSubject("user789")
                .signWith(keyPair.getPrivate(), SignatureAlgorithm.RS256)
                .compact();

        // When
        Claims claims = jwtValidationService.validateTokenAndGetClaims(token);

        // Then
        assertEquals("user789", claims.getSubject());
    }

    @Test
    void shouldThrowInvalidTokenExceptionWhenTokenIsInvalid() {
        // Given
        String publicKeyPem = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        ReflectionTestUtils.setField(jwtValidationService, "publicKeyContent", publicKeyPem);
        
        // When & Then
        assertThrows(InvalidTokenException.class, () -> 
            jwtValidationService.validateTokenAndGetClaims("invalid.token.here")
        );
    }
}
