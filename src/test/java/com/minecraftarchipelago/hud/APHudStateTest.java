package com.minecraftarchipelago.hud;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class APHudStateTest {

    @Test
    void testAPHudStateStaticFields() {
        // Test that static fields are accessible and have expected default values
        assertTrue(APHudState.visible);
        assertFalse(APHudState.connected);
        assertEquals("-", APHudState.address);
        assertEquals(0, APHudState.locationsChecked);
        assertEquals(0, APHudState.locationsTotal);
        assertEquals(70, APHudState.goalPercent);
        assertFalse(APHudState.goalAchieved);
        assertEquals(0, APHudState.advancementsChecked);
        assertEquals(0, APHudState.advancementsTotal);
        assertEquals(0, APHudState.bossKillsChecked);
        assertEquals(0, APHudState.stagesUnlocked);
        assertEquals("None", APHudState.armorTier);
        assertEquals(0xFF555555, APHudState.armorColor);
        assertEquals("None", APHudState.toolTier);
        assertEquals(0xFF555555, APHudState.toolColor);
    }

    @Test
    void testAPHudStateStaticMethods() {
        // Test that static methods work correctly
        assertEquals(0f, APHudState.progressFraction());
        
        // Test with some values set
        APHudState.locationsTotal = 100;
        APHudState.locationsChecked = 30;

        APHudState.advancementsTotal = 50;
        APHudState.advancementsChecked = 10;
        assertEquals(0.2f, APHudState.progressFraction());
        
        // Reset
        APHudState.locationsTotal = 0;
        APHudState.locationsChecked = 0;
        APHudState.advancementsTotal = 0;
        APHudState.advancementsChecked = 0;
    }
}