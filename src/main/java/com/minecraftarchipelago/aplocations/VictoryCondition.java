package com.minecraftarchipelago.aplocations;

import com.minecraftarchipelago.APSession;
import com.minecraftarchipelago.SlotData;
import com.minecraftarchipelago.victory.VictoryConditionRegistry;
import io.github.archipelagomw.ClientStatus;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class VictoryCondition {

    /**
     * Called every time a location is checked (advancements, boss kills,
     * lootable claims). Evaluates all registered win conditions and fires
     * goalAchieved() on the AP client if all are satisfied.
     */
    public static void checkAndAward(MinecraftServer server) {
        if (!APSession.hasSlotData()) return;

        CheckedLocationsState state = CheckedLocationsState.get(server);
        if (state.isGoalAchieved()) return;  // already won, nothing to do

        SlotData slotData = APSession.getSlotData();

        if (!VictoryConditionRegistry.allMet(server, state, slotData)) return;

        // ── All conditions satisfied ──────────────────────────────────────────
        state.markGoalAchieved();

        MinecraftClient.getInstance().execute(() ->
                APSession.CLIENT.setGameState(ClientStatus.CLIENT_GOAL)
        );

        server.getPlayerManager().broadcast(
                Text.empty()
                        .append(Text.literal("⚡ ").formatted(Formatting.YELLOW))
                        .append(Text.literal("Goal achieved! ")
                                .formatted(Formatting.GOLD, Formatting.BOLD))
                        .append(Text.literal("Sending victory to Archipelago...")
                                .formatted(Formatting.YELLOW)),
                false
        );
    }

    /**
     * Called when the player reconnects to AP.
     * Resends the goal-achieved signal if it was already earned this session.
     */
    public static void resendIfAchieved(MinecraftServer server) {
        if (!CheckedLocationsState.get(server).isGoalAchieved()) return;

        MinecraftClient.getInstance().execute(() ->
                APSession.CLIENT.setGameState(ClientStatus.CLIENT_GOAL)
        );
    }

    private VictoryCondition() {}
}