package com.minecraftarchipelago;

import java.util.Map;


public final class SlotData {

    // Percentage of AP locations that must be checked to win.
    // Default to 70%
    private final int advancementGoalPercent;
    private final boolean deathLink;
    private final int lootCheckCount;

    private SlotData(int advancementGoalPercent, boolean deathLink, int lootCheckCount){
        this.advancementGoalPercent = advancementGoalPercent;
        this.deathLink = deathLink;
        this.lootCheckCount = lootCheckCount;
    }

    public int getAdvancementGoalPercent(){
        return advancementGoalPercent;
    }
    public boolean isDeathLinkEnabled() { return deathLink; }
    public int getLootCheckCount() { return lootCheckCount; }

    // Called with whatever map the AP server sends as slot_data
    // Values come from JSON so they may be Int, Double, Long, ect
    public static SlotData parse(Map<String, Object> raw){
        int goal = getInt(raw, "advancement_goal", 70);
        goal = Math.max(1, Math.min(100, goal)); // clamp to 1-100
        boolean deathLink = getBool(raw, "death_link", false);
        int lootCheckCount = getInt(raw, "loot_check_count", 0);
        return new SlotData(goal, deathLink, lootCheckCount);
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
        return "SlotData{advancementGoal=" + advancementGoalPercent + "%, lootCheckCount=" + lootCheckCount + "}";
    }
}
