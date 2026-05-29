package com.minecraftarchipelago.victory;

/**
 * Snapshot of a single win condition's current state.
 * Used by both VictoryConditionRegistry (for checking) and
 * APHudRenderer (for display). Immutable — created fresh each tick.
 */
public record VictoryProgress(
        String  label,     // Display name: "Advancements", "Boss Kills", etc.
        int     current,   // How many have been completed so far
        int     required,  // How many are needed to meet this condition
        int     total,     // Total possible (bar fills to total, goal marker at required)
        boolean met        // Whether this condition is currently satisfied
) {
    /**
     * Fill fraction for the progress bar (0.0 – 1.0), based on current/required.
     */
    public float barFraction() {
        if (required == 0) return 1f;
        return Math.min(1f, (float) current / required);
    }

    /**
     * Where the goal marker tick should sit on the bar (0.0 – 1.0),
     * based on required/total.
     */
    public float goalFraction() {
        if (total == 0) return 1f;
        return Math.min(1f, (float) required / total);
    }
}
