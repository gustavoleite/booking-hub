package com.bookinghub.auth.core.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RoleTest {

    @Test
    void shouldHaveAllRoles() {
        assertNotNull(Role.valueOf("ROLE_CLIENT"));
        assertNotNull(Role.valueOf("ROLE_PROFESSIONAL"));
        assertNotNull(Role.valueOf("ROLE_OWNER"));
        assertEquals(3, Role.values().length);
    }
}
