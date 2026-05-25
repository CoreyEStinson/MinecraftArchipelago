package com.minecraftarchipelago;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MinecraftArchipelagoClientTest {

    @Test
    void testMinecraftArchipelagoClientConstants() {
        // Test that constants are accessible
        assertNotNull(MinecraftArchipelagoClient.MOD_ID);
        assertNotNull(MinecraftArchipelagoClient.LOGGER);
        
        assertEquals("minecraftarchipelago", MinecraftArchipelagoClient.MOD_ID);
    }
}