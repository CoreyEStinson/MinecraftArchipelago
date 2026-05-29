package com.minecraftarchipelago.victory;

import com.minecraftarchipelago.SlotData;
import com.minecraftarchipelago.aplocations.CheckedLocationsState;
import net.minecraft.server.MinecraftServer;

import java.util.Set;

public class BossKillsChecker implements VictoryConditionChecker {

    // Total possible bosses — matches the four boss kill locations
    private static final int TOTAL_BOSSES = 4;

    @Override
    public String getId() { return "boss_kills"; }

    @Override
    public boolean isEnabled(SlotData slotData) {
        return slotData.isBossGoalActive();  // required_boss_kills non-empty
    }

    @Override
    public boolean isMet(MinecraftServer server,
                         CheckedLocationsState state,
                         SlotData slotData) {
        for (long locationId : slotData.getRequiredBossLocationIds()) {
            if (!state.isLocationChecked(locationId)) return false;
        }
        return true;
    }

    @Override
    public VictoryProgress getProgress(MinecraftServer server,
                                       CheckedLocationsState state,
                                       SlotData slotData) {
        Set<Long> required = slotData.getRequiredBossLocationIds();

        // Count how many of the REQUIRED bosses have been killed
        int current = 0;
        for (long id : required) {
            if (state.isLocationChecked(id)) current++;
        }

        int requiredCount = required.size();

        // total = all required bosses (bar fills fully when all are done)
        // Use TOTAL_BOSSES as the outer bound so the bar looks proportional
        // when only some bosses are required (e.g. 2 of 4)
        return new VictoryProgress(
                "Boss Kills",
                current,
                requiredCount,
                TOTAL_BOSSES,
                current >= requiredCount
        );
    }
}