package com.minecraftarchipelago.victory;

import com.minecraftarchipelago.SlotData;
import com.minecraftarchipelago.aplocations.CheckedLocationsState;
import net.minecraft.server.MinecraftServer;

import java.util.List;

public interface VictoryConditionChecker {

    /**
     * Unique string ID for this condition.
     * Used for logging and config/debug
     * Example: "advancement_goal", "boss_kills", "lootable_checks"
     */
    String getId();

    /**
     * Returns true if this condition is active for the current game.
     * If false, the condition is completely ignored - not shown in the HUD,
     * not checked for victory
     */
    boolean isEnabled(SlotData slotData);

    /**
     * Returns true if the player has satisfied this condition.
     * Only called when isEnabled returns true.
     */
    boolean isMet(MinecraftServer server,
                  CheckedLocationsState state,
                  SlotData slotData);

    /**
     * Returns a VictoryProgress snapshot for the HUD display.
     * Only called when isEnabled returns true.
     */
    VictoryProgress getProgress(MinecraftServer server,
                                CheckedLocationsState state,
                                SlotData slotData);

    /**
     * Returns the display names of items or objectives still needed to complete
     * this condition.
     */
    default List<String> getRemainingDetails(MinecraftServer server,
                                             CheckedLocationsState checkedState,
                                             SlotData slotData) {
        return List.of();
    }
}
