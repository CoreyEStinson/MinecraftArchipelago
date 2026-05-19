package com.minecraftarchipelago;

import java.util.Map;

import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;

public final class SlotData {

    // Percentage of AP locations that must be checked to win.
    // Default to 70%
    private final int advancementGoalPercent;
    private final boolean deathLink;

    private SlotData(int advancementGoalPercent, boolean deathLink){
        this.advancementGoalPercent = advancementGoalPercent;
        this.deathLink = deathLink;
    }

    public int getAdvancementGoalPercent(){
        return advancementGoalPercent;
    }
    public boolean isDeathLinkEnabled() { return deathLink; }

    // Called with whatever map the AP server sends as slot_data
    // Values come from JSON so they may be Int, Double, Long, ect
    public static SlotData parse(Map<String, Object> raw){
        int goal = getInt(raw, "advancement_goal", 70);
        goal = Math.max(1, Math.min(100, goal)); // clamp to 1-100
        boolean deathLink = getBool(raw, "death_link", false);
        return new SlotData(goal, deathLink);
    }

    private static int getInt(Map<String, Object> raw, String key, int defaultValue){
        Object val = raw.get(key);
        if (val instanceof Number) return ((Number) val).intValue();
        return defaultValue;
    }

    private static boolean getBool(Map<String, Object> raw, String key, boolean defaultValue){
        Object val = raw.get(key);
        if (val instanceof Boolean) return (Boolean) val;
        if (val instanceof Number) return ((Number) val).intValue() != 0;
        return defaultValue;
    }

    @Override
    public String toString(){
        return "SlotData{advancementGoal=" + advancementGoalPercent + "$}";
    }
}
