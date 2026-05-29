package com.minecraftarchipelago.hud;

import com.minecraftarchipelago.victory.VictoryProgress;
import java.util.*;

public final class APHudState {

    public static boolean visible = true;

    // --- Connection ---
    public static boolean connected = false;
    public static String  address   = "—";

    // --- Location totals (all types combined) ---
    public static int     locationsChecked = 0;
    public static int     locationsTotal   = 0;
    public static boolean goalAchieved     = false;

    // --- Advancement tracking ---
    public static int advancementsChecked = 0;
    public static int advancementsTotal   = 0;
    public static int goalPercent         = 70;  // updated from slot data each tick

    // --- Boss kill tracking ---
    public static final int BOSS_KILLS_TOTAL = 4;
    // Ordered map: display name → killed?
    // LinkedHashMap preserves the order bosses were registered
    public static Map<String, Boolean> bossKills = new LinkedHashMap<>();
    // Display names of bosses that are REQUIRED win condition targets
    public static Set<String> requiredBossDisplayNames = new HashSet<>();
    public static int bossKillsChecked = 0;

    // --- Lootable check tracking ---
    public static int lootableChecksFound = 0;
    public static int lootableChecksTotal = 0;

    // --- Win conditions (populated from VictoryConditionRegistry each tick) ---
    public static List<VictoryProgress> activeConditions = new ArrayList<>();

    // --- Equipment ---
    public static int    stagesUnlocked = 0;
    public static String armorTier      = "None";
    public static int    armorColor     = 0xFF555555;
    public static String toolTier       = "None";
    public static int    toolColor      = 0xFF555555;

    // --- Computed helpers ---

    /** Fill fraction (0–1) for the raw advancement progress bar. */
    public static float progressFraction() {
        if (advancementsTotal == 0) return 0f;
        return Math.min(1f, (float) advancementsChecked / advancementsTotal);
    }

    private APHudState() {}
}