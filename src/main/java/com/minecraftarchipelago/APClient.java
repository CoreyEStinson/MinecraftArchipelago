package com.minecraftarchipelago;

import io.github.archipelagomw.Client;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class APClient extends Client
{
    @Override
    public void onError(Exception ex) {
        // called when the websocket/client hits an error
        ex.printStackTrace();
    }

    @Override
    public void onClose(String reason, int attemptingReconnect) {
        System.out.println("AP connection closed: " + reason);
        APSession.clearSlotData();

        MinecraftClient.getInstance().execute(() -> {
            var player = MinecraftClient.getInstance().player;
            if (player != null){
                player.sendMessage(Text.literal(
                        "[AP] Disconnected:" + reason));
            }
        });
    }
}
