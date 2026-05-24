package com.minecraftarchipelago.hud;

public final class APHudState {

    // Toggle - press the keybinding to flip this
    public static boolean visible = true;

    // Connection
    public static boolean connected = false;
    public static String address = "-";

    // All locations
    public static int locationsChecked = 0;
    public static int locationsTotal = 0;
    public static int goalPercent = 70;
    public static boolean goalAchieved = false;

    // Advancements
    public static int advancementsChecked = 0;
    public static int advancementsTotal   = 0;

    // Boss kills
    public static java.util.Map<String, Boolean> bossKills = new java.util.LinkedHashMap<>();
    public static int bossKillsChecked = 0;

    // Stages
    public static int stagesUnlocked = 0;

    // Equipment
    public static String armorTier = "None";
    public static int armorColor = 0xFF555555;
    public static String toolTier = "None";
    public static int toolColor = 0xFF555555;

    // Computed helpers
    public static int locationsRequired() {
        return (int) Math.ceil(locationsTotal * goalPercent / 100.0);
    }

    public static int locationsRemaining(){
        return Math.max(0, locationsRequired() - locationsChecked);
    }

    // For advancements only
    public static float progressFraction() {
        if (advancementsTotal == 0) return 0f;
        return Math.min(1f, (float) advancementsChecked / advancementsTotal);
    }

    private APHudState() {}
}
