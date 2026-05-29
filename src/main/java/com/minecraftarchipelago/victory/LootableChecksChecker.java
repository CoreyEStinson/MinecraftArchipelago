package com.minecraftarchipelago.victory;

import com.minecraftarchipelago.SlotData;
import com.minecraftarchipelago.aplocations.CheckedLocationsState;
import net.minecraft.server.MinecraftServer;

public class LootableChecksChecker implements VictoryConditionChecker {

    @Override
    public String getId() { return "lootable_checks"; }

    @Override
    public boolean isEnabled(SlotData slotData) {
        return slotData.isLootableGoalActive();  // required_lootable_checks > 0
    }

    @Override
    public boolean isMet(MinecraftServer server,
                         CheckedLocationsState state,
                         SlotData slotData) {
        int checked  = checkedLootable(state, slotData);
        return checked >= slotData.getRequiredLootableChecks();
    }

    @Override
    public VictoryProgress getProgress(MinecraftServer server,
                                       CheckedLocationsState state,
                                       SlotData slotData) {
        int checked  = checkedLootable(state, slotData);
        int required = slotData.getRequiredLootableChecks();
        int total    = slotData.getLootableChecks();  // full pool size for bar
        return new VictoryProgress(
                "Lootable Checks",
                checked,
                required,
                total,
                checked >= required
        );
    }

    private static int checkedLootable(CheckedLocationsState state, SlotData slotData) {
        return state.countCheckedInRange(
                SlotData.LOOTABLE_CHECK_BASE_ID,
                SlotData.LOOTABLE_CHECK_BASE_ID + slotData.getLootableChecks()
        );
    }
}