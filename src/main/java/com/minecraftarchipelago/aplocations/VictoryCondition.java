package com.minecraftarchipelago.aplocations;

import com.minecraftarchipelago.APSession;
import io.github.archipelagomw.ClientStatus;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;

public class VictoryCondition {

    /**
     * Called after each new location check and on reconnect.
     * Fires goalAchieved() to AP if the threshold is met for the first time.
     * Must be called on the server thread
     */
    public static void checkAndAward(MinecraftServer server){
        if (!APSession.hasSlotData()) return;;
        if (!APSession.CLIENT.isConnected()) return;

        CheckedLocationsState state = CheckedLocationsState.get(server);
        if (state.isGoalAchieved()) return; // already won, nothing to do

        int total = LocationRegistry.size();
        if (total == 0) return;

        int checked = state.checkedCount();
        int goalPercent = APSession.getSlotData().getAdvancementGoalPercent();

        if (checked * 100 < total * goalPercent) return;;

        // Threshold met - mark it first to prevent double firing
        boolean wasNew = state.markGoalAchieved();
        if (!wasNew) return;

        int finalChecked = checked;
        MinecraftClient.getInstance().execute(() -> {
            APSession.CLIENT.setGameState(ClientStatus.CLIENT_GOAL);

            var player = MinecraftClient.getInstance().player;
            if (player != null) {
                player.sendMessage(Text.literal(
                        "[AP] Goal achieved! Completed " + finalChecked
                        + "/" + total + " locations (" + goalPercent + "% required). You win!"
                ));
            }
        });
    }

    /**
     * Called on reconnect if the goal was already achieved in a previous
     * session. Silently resends the status without chat
     */
    public static void resendIfAchieved(MinecraftServer server){
        if (!APSession.CLIENT.isConnected()) return;

        CheckedLocationsState state = CheckedLocationsState.get(server);
        if (!state.isGoalAchieved()) return;

        APSession.CLIENT.setGameState(ClientStatus.CLIENT_GOAL);
    }

    private VictoryCondition() {}
}
