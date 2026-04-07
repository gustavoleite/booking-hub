package com.bookinghub.catalog.core.exceptions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ExceptionsTest {

    @Test
    void testExceptions() {
        String msg = "test";
        assertEquals(msg, new BusinessRuleException(msg).getMessage());
        assertEquals(msg, new ConflictException(msg).getMessage());
        assertEquals(msg, new ForbiddenException(msg).getMessage());
        assertEquals(msg, new NotFoundException(msg).getMessage());
    }
}
