package com.minecraftarchipelago;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class APSessionTest {

    @Test
    void testAPSessionStaticMethods() {
        // Test that APSession static methods work
        assertNotNull(APSession.CLIENT);
        
        // Test slot data methods
        assertNull(APSession.getSlotData());
        assertFalse(APSession.hasSlotData());
        
        // Test pending credentials methods
        assertNull(APSession.getPendingHost());
        assertNull(APSession.getPendingPort());
        assertNull(APSession.getPendingSlot());
        assertNull(APSession.getPendingPassword());
        
        // Test setting and clearing
        APSession.setPendingCredentials("test", "1234", "slot1", "pass");
        assertEquals("test", APSession.getPendingHost());
        assertEquals("1234", APSession.getPendingPort());
        assertEquals("slot1", APSession.getPendingSlot());
        assertEquals("pass", APSession.getPendingPassword());
        
        APSession.clearSlotData();
        assertNull(APSession.getSlotData());
    }
}