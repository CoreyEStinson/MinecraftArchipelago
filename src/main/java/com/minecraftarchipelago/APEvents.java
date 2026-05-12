package com.minecraftarchipelago;


import com.minecraftarchipelago.apitems.APItemRegistry;
import com.minecraftarchipelago.aplocations.CheckedLocationsState;
import com.minecraftarchipelago.aplocations.VictoryCondition;
import com.minecraftarchipelago.apstages.service.StageUnlockApplier;
import com.minecraftarchipelago.apstages.state.StageUnlockState;
import io.github.archipelagomw.events.ArchipelagoEventListener;
import io.github.archipelagomw.events.ReceiveItemEvent;
import io.github.archipelagomw.events.LocationInfoEvent;
import io.github.archipelagomw.events.PrintJSONEvent;
import io.github.archipelagomw.events.RetrievedEvent;
import io.github.archipelagomw.events.ConnectionResultEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Map;

public class APEvents {

    @ArchipelagoEventListener
    public void onConnected(ConnectionResultEvent e){
        // e.slotData is the raw map from the APWorld's generate() method.
        // Verify the exact field name against the Java client library.
        SlotData data = SlotData.parse(e.getSlotData(Map.class));
        APSession.setSlotData(data);

        MinecraftArchipelagoClient.LOGGER.info(
                "[AP] Connected. Slot data received: {}", data);

        // Show confirmation in game chat
        MinecraftClient.getInstance().execute(() -> {
            var player = MinecraftClient.getInstance().player;
            if (player != null){
                player.sendMessage(Text.literal(
                        "[AP] Connected! Goal: " + data.getAdvancementGoalPercent()
                        + "% of advancements."
                ));
            }
        });

        // Resend all previously checked locations so the AP server
        // knows what we're already done. Without this, if you reconnect
        // mid-game the server thinks you've checked nothing
        MinecraftServer server = MinecraftClient.getInstance().getServer();
        if (server == null) return;;
        server.execute(() -> {
            CheckedLocationsState state = CheckedLocationsState.get(server);
            var allChecked = state.getAllChecked();

            if (allChecked.isEmpty()) return;

            MinecraftArchipelagoClient.LOGGER.info(
                    "[AP] Resending {} previously check locations.", allChecked.size());

            for (long locationId : allChecked){
                APSession.CLIENT.checkLocation(locationId);
            }

            // If goal was already achieved in a previous session, silently resend
            VictoryCondition.resendIfAchieved(server);
        });
    }

    @ArchipelagoEventListener
    public void onItemReceived(ReceiveItemEvent e){
        long apItemId = e.getItemID(); // verify field name against the library

        Identifier stageId = APItemRegistry.getStageId(apItemId);
        if (stageId == null) return; // not a stage unlock, ignore

        // AP events fire on a background websocket thread.
        // Need to get onto the server thread to safely touch
        // player inventory, gamerules, and persistent state.
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        minecraftClient.execute(() -> {
            MinecraftServer server = minecraftClient.getServer();
            if (server == null) return; // Not in a world

            server.execute(() -> {
                if (minecraftClient.player == null) return;
                String playerName = minecraftClient.player.getName().getString();

                ServerPlayerEntity serverPlayer =
                        server.getPlayerManager().getPlayer(playerName);
                if (serverPlayer == null) return;

                // unlock() returns false if already unlocked
                // prevents re-applying gamerules and re-granting packages on reconnect
                StageUnlockState state = StageUnlockState.get(server);
                boolean added = state.unlock(serverPlayer.getUuid(), stageId);

                if (added){
                    StageUnlockApplier.apply(serverPlayer, stageId);
                }
            });
        });
    }

    @ArchipelagoEventListener
    public void onLocationInfo(LocationInfoEvent e) {
        System.out.println("Got location info: " + e);
    }

    @ArchipelagoEventListener
    public void onRetrieved(RetrievedEvent e) {
        System.out.println("Datastorage retrieved: " + e);
    }

    @ArchipelagoEventListener
    public void onPrint(PrintJSONEvent e){
        String msg = e.apPrint.getPlainText();
        MinecraftClient.getInstance().execute(() ->{
            var player = MinecraftClient.getInstance().player;
            if (player != null) player.sendMessage(Text.literal("[AP] " + msg));
        });
    }


}