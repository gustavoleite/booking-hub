package com.bookinghub.auth.infrastructure.adapters.out.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BCryptPasswordEncoderAdapterTest {

    private BCryptPasswordEncoderAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new BCryptPasswordEncoderAdapter();
    }

    @Test
    void shouldEncodePassword() {
        String raw = "password123";
        String encoded = adapter.encode(raw);
        
        assertNotNull(encoded);
        assertNotEquals(raw, encoded);
        assertTrue(adapter.matches(raw, encoded));
    }

    @Test
    void shouldNotMatchWrongPassword() {
        String raw = "password123";
        String encoded = adapter.encode(raw);
        
        assertFalse(adapter.matches("wrongpassword", encoded));
    }
}
