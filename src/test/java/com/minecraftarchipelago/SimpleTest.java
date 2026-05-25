package com.minecraftarchipelago;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SimpleTest {

    @Test
    void testSimpleAssertion() {
        assertTrue(true);
        assertEquals(2, 1 + 1);
        assertNotNull("Hello World");
    }
}