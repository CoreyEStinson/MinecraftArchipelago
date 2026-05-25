package com.minecraftarchipelago.aplocations;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BossKillListenerTest {

    @Test
    void testBossKillListenerStaticMethods() {
        // Test that BossKillListener static methods work
        // The register method is meant to be called by the mod initialization
        // We can test that it doesn't throw exceptions
        assertDoesNotThrow(() -> BossKillListener.register());
    }
}