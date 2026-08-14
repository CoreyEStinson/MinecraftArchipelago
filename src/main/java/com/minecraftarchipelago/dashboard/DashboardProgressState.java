package com.minecraftarchipelago.dashboard;

import com.minecraftarchipelago.victory.VictoryProgress;

import java.util.List;

/** Current Archipelago progress displayed by dashboard tabs. */
public final class DashboardProgressState {
    public static int locationsChecked = 0;
    public static int locationsTotal = 0;
    public static List<VictoryProgress> activeConditions = List.of();

    private DashboardProgressState() {
    }
}
