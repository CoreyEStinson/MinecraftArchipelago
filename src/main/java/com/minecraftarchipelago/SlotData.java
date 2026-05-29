package com.minecraftarchipelago;

import java.util.*;

public final class SlotData {

    public static final long LOOTABLE_CHECK_BASE_ID = 42500;
    public static final int LOOTABLE_CHECK_MAX = 41;

    // Boss Name -> AP Location ID Mapping
    // Single source for all boss-related lookups
    public static final Map<String, Long> BOSS_LOCATION_IDS = Map.of(
            "ender_dragon",   42119L,
            "wither",         42120L,
            "elder_guardian", 42121L,
            "warden",         42122L
    );

    //  --- Fields ---
    private final int advancementGoalPercent;
    private final boolean deathLink;
    private final int lootableChecks;
    private final Set<String> requiredBossKills;
    private final int requiredLootableChecks;


    // --- Constructor ---
    private SlotData(int advancementGoalPercent,
                     boolean deathLink,
                     int lootableChecks,
                     Set<String> requiredBossKills,
                     int requiredLootableChecks) {
        this.advancementGoalPercent = advancementGoalPercent;
        this.deathLink = deathLink;
        this.lootableChecks = lootableChecks;
        this.requiredBossKills = requiredBossKills;
        this.requiredLootableChecks = requiredLootableChecks;
    }

    // --- Getters ---
    public int         getAdvancementGoalPercent()  { return advancementGoalPercent; }
    public boolean     isDeathLinkEnabled()          { return deathLink; }
    public int         getLootableChecks()           { return lootableChecks; }
    public Set<String> getRequiredBossKills()        { return requiredBossKills; }
    public int         getRequiredLootableChecks()   { return requiredLootableChecks; }

    // --- Win condition helpers ---

    /** True if the advancement percentage goal is an active win condition. */
    public boolean isAdvancementGoalActive() {
        return advancementGoalPercent > 0;
    }

    /** True if killing specific bosses is an active win condition. */
    public boolean isBossGoalActive() {
        return !requiredBossKills.isEmpty();
    }

    /** True if claiming a number of lootable checks is an active win condition. */
    public boolean isLootableGoalActive() {
        return requiredLootableChecks > 0;
    }

    /** True if the named boss is required for the boss kill win condition. */
    public boolean isBossRequired(String bossName) {
        return requiredBossKills.contains(bossName);
    }

    /**
     * Returns the AP location ID for the named boss kill, or -1 if unknown.
     * Example: getBossLocationId("wither") → 42114
     */
    public static long getBossLocationId(String bossName) {
        return BOSS_LOCATION_IDS.getOrDefault(bossName, -1L);
    }

    /**
     * Returns the set of AP location IDs that must be checked to satisfy
     * the boss kill win condition. Empty if boss goal is not active.
     */
    public Set<Long> getRequiredBossLocationIds() {
        if (requiredBossKills.isEmpty()) return Set.of();
        Set<Long> ids = new HashSet<>();
        for (String boss : requiredBossKills) {
            long id = getBossLocationId(boss);
            if (id >= 0) ids.add(id);
        }
        return Collections.unmodifiableSet(ids);
    }

    // --- Lootable ID helpers ---

    public boolean isLootableCheckLocation(long locationId) {
        return locationId >= LOOTABLE_CHECK_BASE_ID
                && locationId < LOOTABLE_CHECK_BASE_ID + lootableChecks;
    }

    public long getLootableCheckId(int slot) {
        if (slot < 0 || slot >= lootableChecks) {
            throw new IndexOutOfBoundsException(
                    "Lootable check slot " + slot + " out of range (0-" + (lootableChecks -1) + ")");
        }
        return LOOTABLE_CHECK_BASE_ID + slot;
    }

    // --- Parser ---
    public static SlotData parse(Map<String, Object> raw){
        int goal = getInt(raw, "advancement_goal", 70);
        goal = Math.max(0, Math.min(100, goal));

        boolean deathLink = getBool(raw, "death_link", false);

        int lootable = getInt(raw, "lootable_checks", 20);
        lootable = Math.max(0, Math.min(LOOTABLE_CHECK_MAX, lootable));

        Set<String> requiredBosses = getStringSet(raw, "required_bosses");
        if (requiredBosses.isEmpty()) {
            requiredBosses = getStringSet(raw, "required_boss_kills");
        }

        int reqLootable = getInt(raw, "required_loot_checks", -1);
        if (reqLootable == -1) {
            reqLootable = getInt(raw, "required_lootable_checks", 0);
        }
        reqLootable = Math.max(0, Math.min(lootable, reqLootable));

        return new SlotData(goal, deathLink, lootable, requiredBosses, reqLootable);
    }

    // --- Helper methods ---

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

    private static Set<String> getStringSet(Map<String, Object> raw, String key) {
        Object val = raw.get(key);
        if (!(val instanceof List<?> list)) return Set.of();
        Set<String> result = new HashSet<>();
        for (Object item : list) {
            if (item instanceof String s) result.add(s);
        }
        return result;
    }

    // --- toString ---
    @Override
    public String toString() {
        return "SlotData{"
                + "advancementGoal=" + advancementGoalPercent
                + ", deathLink=" + deathLink
                + ", lootableChecks=" + lootableChecks
                + ", requiredBossKills=" + requiredBossKills
                + ", requiredLootableChecks=" + requiredLootableChecks
                + "}";
    }
}
