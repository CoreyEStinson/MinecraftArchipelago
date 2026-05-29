package com.minecraftarchipelago.victory;

import com.minecraftarchipelago.SlotData;
import com.minecraftarchipelago.aplocations.CheckedLocationsState;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VictoryConditionRegistry {

    private static final List<VictoryConditionChecker> CHECKERS = new ArrayList<>();

    // --- Registration ---

    /**
     * Registers a win condition checks. Call once per checker during
     * mod initialization, before any worlds are loaded.
     * Checkers are evaluated in registration order.
     */
    public static void register(VictoryConditionChecker checker) {
        CHECKERS.add(checker);
    }

    // --- Victory check ---

    /**
     * Returns true only if every enabled win condition is satisfied.
     * Returns false if no checkers are registered
     */
    public static boolean allMet(MinecraftServer server,
                                 CheckedLocationsState state,
                                 SlotData slotData) {
        boolean anyActive = false;
        for (VictoryConditionChecker checker : CHECKERS) {
            if (!checker.isEnabled(slotData)) continue;
            anyActive = true;
            if (!checker.isMet(server, state, slotData)) return false;
        }
        return anyActive;
    }

    // --- Progress snapshots ---

    /**
     * Returns a VictoryProgress for each currently enabled condition.
     * The list is in registration order, so the HUD renders consistently
     */
    public static List<VictoryProgress> getActiveProgress(MinecraftServer server,
                                                          CheckedLocationsState state,
                                                          SlotData slotData) {
        List<VictoryProgress> result = new ArrayList<>();
        for (VictoryConditionChecker checker : CHECKERS) {
            if (checker.isEnabled(slotData)) {
                result.add(checker.getProgress(server, state, slotData));
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns how many conditions are currently enabled.
     */
    public static int activeCount(SlotData slotData) {
        int count = 0;
        for (VictoryConditionChecker checker : CHECKERS) {
            if (checker.isEnabled(slotData)) count++;
        }
        return count;
    }

    private VictoryConditionRegistry() {}
}
