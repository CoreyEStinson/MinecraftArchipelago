package com.minecraftarchipelago;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class APConnectionStateTest {

    @Test
    void testAPConnectionStateCreation() {
        // Test that APConnectionState can be instantiated
        APConnectionState state = new APConnectionState();
        assertNotNull(state);
    }

    @Test
    void testAPConnectionStateMethods() {
        // Test basic methods exist and can be called
        APConnectionState state = new APConnectionState();
        assertNotNull(state);
        
        // Test that methods can be called
        state.save("test", "1234", "slot1", "pass");
        assertEquals("test", state.getHost());
        assertEquals("1234", state.getPort());
        assertEquals("slot1", state.getSlot());
        assertEquals("pass", state.getPassword());
        
        state.clear();
        assertNull(state.getHost());
        assertNull(state.getPort());
        assertNull(state.getSlot());
        assertNull(state.getPassword());
    }
}