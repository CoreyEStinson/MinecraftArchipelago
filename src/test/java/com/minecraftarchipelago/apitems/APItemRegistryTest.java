package com.minecraftarchipelago.apitems;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class APItemRegistryTest {

    @Test
    void testAPItemRegistryStaticMethods() {
        // Test that APItemRegistry static methods work
        assertEquals(0, APItemRegistry.size());
        
        // Test putting and getting items
        Identifier testId = Identifier.of("test:stage1");
        APItemRegistry.put(123L, testId);
        assertEquals(1, APItemRegistry.size());
        assertEquals(testId, APItemRegistry.getStageId(123L));
        
        // Test progressive items
        List<Identifier> progressiveTiers = List.of(
            Identifier.of("test:tier1"),
            Identifier.of("test:tier2"),
            Identifier.of("test:tier3")
        );
        APItemRegistry.putProgressive(456L, progressiveTiers);
        assertEquals(2, APItemRegistry.size());
        assertTrue(APItemRegistry.isProgressive(456L));
        
        // Test getNextTier
        HashSet<Identifier> alreadyUnlocked = new HashSet<>();
        alreadyUnlocked.add(Identifier.of("test:tier1"));
        Identifier nextTier = APItemRegistry.getNextTier(456L, alreadyUnlocked);
        assertEquals("test:tier2", nextTier.toString());
        
        // Test clearing
        APItemRegistry.clear();
        assertEquals(0, APItemRegistry.size());
        assertNull(APItemRegistry.getStageId(123L));
        assertFalse(APItemRegistry.isProgressive(456L));
    }
}