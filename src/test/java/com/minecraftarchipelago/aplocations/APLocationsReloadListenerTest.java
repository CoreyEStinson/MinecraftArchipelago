package com.minecraftarchipelago.aplocations;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class APLocationsReloadListenerTest {

    @Test
    void testAPLocationsReloadListenerConstants() {
        // Test that constants are accessible
        APLocationsReloadListener listener = new APLocationsReloadListener();
        assertNotNull(listener.getFabricId());
        assertEquals("minecraftarchipelago:aplocations_reload", listener.getFabricId().toString());
    }
}