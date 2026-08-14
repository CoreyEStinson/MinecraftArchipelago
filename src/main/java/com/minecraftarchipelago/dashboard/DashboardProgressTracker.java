package com.minecraftarchipelago.dashboard;

import com.minecraftarchipelago.APSession;
import com.minecraftarchipelago.aplocations.BossKillLocationRegistry;
import com.minecraftarchipelago.aplocations.CheckedLocationsState;
import com.minecraftarchipelago.aplocations.LocationRegistry;
import com.minecraftarchipelago.victory.VictoryConditionRegistry;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;

public final class DashboardProgressTracker {
    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(DashboardProgressTracker::update);
    }

    private static void update(MinecraftClient client) {
        MinecraftServer server = client.getServer();
        if (server == null || client.player == null) {
            DashboardProgressState.locationsChecked = 0;
            DashboardProgressState.locationsTotal = 0;
            DashboardProgressState.activeConditions = java.util.List.of();
            return;
        }

        CheckedLocationsState locations = CheckedLocationsState.get(server);
        int lootableChecks = APSession.hasSlotData()
                ? APSession.getSlotData().getLootableChecks()
                : 0;

        DashboardProgressState.locationsChecked = locations.checkedCount();
        DashboardProgressState.locationsTotal = LocationRegistry.size()
                + BossKillLocationRegistry.size()
                + lootableChecks;

        DashboardProgressState.activeConditions = APSession.hasSlotData()
                ? VictoryConditionRegistry.getActiveProgress(server, locations, APSession.getSlotData())
                : java.util.List.of();
    }

    private DashboardProgressTracker() {
    }
}
