package com.minecraftarchipelago;

import java.util.Map;

public final class SlotData {

    // Percentage of AP locations that must be checked to win.
    // Default to 70%
    private final int advancementGoalPercent;

    private SlotData(int advancementGoalPercent){
        this.advancementGoalPercent = advancementGoalPercent;
    }

    public int getAdvancementGoalPercent(){
        return advancementGoalPercent;
    }

    // Called with whatever map the AP server sends as slot_data
    // Values come from JSON so they may be Int, Double, Long, ect
    public static SlotData parse(Map<String, Object> raw){
        int goal = getInt(raw, "advancement_goal", 70);
        goal = Math.max(1, Math.min(100, goal)); // clamp to 1-100
        return new SlotData(goal);
    }

    private static int getInt(Map<String, Object> raw, String key, int defaultValue){
        Object val = raw.get(key);
        if (val instanceof Number) return ((Number) val).intValue();
        return defaultValue;
    }

    @Override
    public String toString(){
        return "SlotData{advancementGoal=" + advancementGoalPercent + "$}";
    }
}
