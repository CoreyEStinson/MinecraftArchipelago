package com.minecraftarchipelago.aplocations;

import com.minecraftarchipelago.APSession;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public final class AdvancementLocationHandler {

    public static boolean handleCompletedAdvancement(MinecraftServer server,
                                                     ServerPlayerEntity player,
                                                     Identifier advancementId) {
        Long locationId = LocationRegistry.getLocationId(advancementId);
        if (locationId == null) return false;

        CheckedLocationsState state = CheckedLocationsState.get(server);
        boolean isNew = state.checkLocation(locationId);
        if (!isNew) return false;

        if (APSession.client().isConnected()) {
            APSession.runtime().executeOnClient(() -> {
                if (!APSession.client().isConnected()) return;
                APSession.client().checkLocation(locationId);
            });
        } else {
            APSession.runtime().executeOnClient(() -> {
                var currentPlayer = APSession.runtime().getCurrentPlayer();
                if (currentPlayer != null) {
                    currentPlayer.sendMessage(
                            Text.literal("[AP] You are not connected to the server. Check is saved - will sync when reconnected.")
                                    .formatted(Formatting.YELLOW),
                            true
                    );
                }
            });
        }

        VictoryCondition.checkAndAward(server);
        return true;
    }

    private AdvancementLocationHandler() {}
}
