package com.minecraftarchipelago.victory;

import com.minecraftarchipelago.SlotData;
import com.minecraftarchipelago.aplocations.CheckedLocationsState;
import net.minecraft.server.MinecraftServer;

public class AdvancementGoalChecker implements VictoryConditionChecker{

    // Matches locations.py - advancements occupy IDs 42001 through 42118
    private static final long ADV_MIN = 42001;
    private static final long ADV_MAX_EXCL = 42119;
    private static final int TOTAL_ADVANCEMENTS = 117;

    @Override
    public String getId() { return "advancement_goal"; }

    @Override
    public boolean isEnabled(SlotData slotData) {
        return slotData.isAdvancementGoalActive(); // advancement_goal > 0
    }

    @Override
    public boolean isMet(MinecraftServer server,
                         CheckedLocationsState state,
                         SlotData slotData) {
        int checked = state.countCheckedInRange(ADV_MIN, ADV_MAX_EXCL);
        int required = requiredCount(slotData);
        return checked >= required;
    }

    @Override
    public VictoryProgress getProgress(MinecraftServer server,
                                       CheckedLocationsState state,
                                       SlotData slotData) {
        int checked = state.countCheckedInRange(ADV_MIN, ADV_MAX_EXCL);
        int required = requiredCount(slotData);
        boolean met = checked >= required;
        return new VictoryProgress("Advancements", checked, required,
                                        TOTAL_ADVANCEMENTS, met);
    }

    // ceil(total * percent / 100) - how many advancements must be checked
    private static int requiredCount(SlotData slotData) {
        return (int) Math.ceil(
                TOTAL_ADVANCEMENTS * slotData.getAdvancementGoalPercent() / 100.0
        );
    }
}
