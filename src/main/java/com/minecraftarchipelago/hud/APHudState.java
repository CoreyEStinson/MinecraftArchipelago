package com.minecraftarchipelago.hud;

public final class APHudState {

    // Toggle - press the keybinding to flip this
    public static boolean visible = true;

    // Connection
    public static boolean connected = false;
    public static String address = "-";

    // Progress (updated every tick)
    public static int locationsChecked = 0;
    public static int locationsTotal = 0;
    public static int goalPercent = 70;
    public static int stagesUnlocked;
    public static boolean goalAchieved = false;

    // Computed helpers
    public static int locationsRequired() {
        return (int) Math.ceil(locationsTotal * goalPercent / 100.0);
    }

    public static int locationsRemaining(){
        return Math.max(0, locationsRequired() - locationsChecked);
    }

    public static float progressFraction() {
        if (locationsTotal == 0) return 0f;
        return Math.min(1f, (float) locationsChecked / locationsTotal);
    }

    private APHudState() {}
}
