package com.minecraftarchipelago;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DeathLinkHandlerTest {

    @Test
    void testDeathLinkHandlerStaticMethods() {
        // Test that DeathLinkHandler static methods work
        assertFalse(DeathLinkHandler.isReceivingDeathLink());
        
        DeathLinkHandler.setReceivingDeathLink(true);
        assertTrue(DeathLinkHandler.isReceivingDeathLink());
        
        DeathLinkHandler.setReceivingDeathLink(false);
        assertFalse(DeathLinkHandler.isReceivingDeathLink());
    }
}