package com.minecraftarchipelago;

import io.github.archipelagomw.Client;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class APClient extends Client
{
    @Override
    public void onError(Exception ex) {
        // called when the websocket/client hits an error
        ex.printStackTrace();
    }

    @Override
    public void onClose(String reason, int code) {
        // Clear session state
        APSession.clearSlotData();

        // Show disconnect message on client thread
        MinecraftClient mc = MinecraftClient.getInstance();
        mc.execute(() -> {
            var player = mc.player;
            if (player == null) return;

            // Red text so it's hard to miss
            player.sendMessage(
                    Text.literal("[AP] Disconnected from Archipelago.")
                            .formatted(Formatting.RED)
            );

            // Only show reason if it's actually informative
            if (reason != null && !reason.isBlank() && !reason.equals("1000")) {
                player.sendMessage(
                        Text.literal("[AP] Reason: " + reason)
                                .formatted(Formatting.GRAY)
                );
            }

            player.sendMessage(
                    Text.literal("[AP] Checks will be saved and synced when reconnected.")
                            .formatted(Formatting.YELLOW)
            );
        });
    }
}
