package com.minecraftarchipelago;

import java.util.Map;

public final class SlotData {

    public static final long LOOTABLE_CHECK_BASE_ID = 42500;
    public static final int LOOTABLE_CHECK_MAX = 41;

    // Percentage of AP locations that must be checked to win.
    // Default to 70%
    private final int advancementGoalPercent;
    private final boolean deathLink;
    private final int lootableChecks;

    private SlotData(int advancementGoalPercent, boolean deathLink, int lootableChecks){
        this.advancementGoalPercent = advancementGoalPercent;
        this.deathLink = deathLink;
        this.lootableChecks = lootableChecks;
    }

    public int getAdvancementGoalPercent(){
        return advancementGoalPercent;
    }
    public boolean isDeathLinkEnabled() { return deathLink; }
    public int getLootableChecks() { return lootableChecks; }

    public long getLootableCheckId(int slot) {
        if (slot < 0 || slot >= lootableChecks) {
            throw new IndexOutOfBoundsException(
                    "Lootable check slot " + slot + " out of range (0-" + (lootableChecks -1) + ")");
        }
        return LOOTABLE_CHECK_BASE_ID + slot;
    }

    // Called with whatever map the AP server sends as slot_data
    // Values come from JSON so they may be Int, Double, Long, ect
    public static SlotData parse(Map<String, Object> raw){
        int goal = getInt(raw, "advancement_goal", 70);
        goal = Math.max(1, Math.min(100, goal)); // clamp to 1-100

        boolean deathLink = getBool(raw, "death_link", false);

        int lootable = getInt(raw, "lootable_checks", 20);
        lootable = Math.max(0, Math.min(LOOTABLE_CHECK_MAX, lootable));

        return new SlotData(goal, deathLink, lootable);
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
    public String toString() {
        return "SlotData{advancementGoal=" + advancementGoalPercent
                + ", deathLink=" + deathLink
                + ", lootableChecks=" + lootableChecks + "}";
    }
}
