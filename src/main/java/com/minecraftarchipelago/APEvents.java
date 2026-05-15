package com.minecraftarchipelago;


import com.minecraftarchipelago.apitems.APGiveItemRegistry;
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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
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
            // Persist credentials for auto-reconnect on next world load
            MinecraftServer server = MinecraftClient.getInstance().getServer();
            if (server != null) {
                server.execute(() -> {
                    APConnectionState.get(server).save(
                            APSession.getPendingHost(),
                            APSession.getPendingPort(),
                            APSession.getPendingSlot(),
                            APSession.getPendingPassword()
                    );
                });
            }

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
    public void onItemReceived(ReceiveItemEvent e) {
        Long itemId = e.getItemID();
        if (itemId == null) {
            MinecraftArchipelagoClient.LOGGER.warn("[AP] Received item with null ID, skipping");
            return;
        }
        long apItemId = itemId;
        MinecraftArchipelagoClient.LOGGER.info("[AP] Item received, ID: {}", apItemId);

        MinecraftClient mc = MinecraftClient.getInstance();
        mc.execute(() -> {
            MinecraftServer server = mc.getServer();
            if (server == null) return;

            server.execute(() -> {
                if (mc.player == null) return;
                String playerName = mc.player.getName().getString();
                ServerPlayerEntity serverPlayer =
                        server.getPlayerManager().getPlayer(playerName);
                if (serverPlayer == null) return;

                // Case 1: filler give item — give directly, no stage needed
                if (APGiveItemRegistry.isGiveItem(apItemId)) {
                    MinecraftArchipelagoClient.LOGGER.info("[AP] → give item found");
                    var entry = APGiveItemRegistry.getEntry(apItemId);
                    Item item = Registries.ITEM.get(entry.itemId());
                    ItemStack stack = new ItemStack(item, entry.count());
                    if (!serverPlayer.getInventory().insertStack(stack)) {
                        serverPlayer.dropItem(stack, false);
                    }
                    return;
                }

                StageUnlockState state = StageUnlockState.get(server);

                // Case 2: progressive item — find and apply the next unlocked tier
                if (APItemRegistry.isProgressive(apItemId)) {
                    MinecraftArchipelagoClient.LOGGER.info("[AP] → progressive item found");
                    Identifier stageId = APItemRegistry.getNextTier(
                            apItemId,
                            state.getUnlocked(serverPlayer.getUuid())
                    );
                    if (stageId == null) return; // all tiers already unlocked

                    if (state.unlock(serverPlayer.getUuid(), stageId)) {
                        StageUnlockApplier.apply(serverPlayer, stageId);
                    }
                    return;
                }

                // Case 3: standard unlock or gamerule item
                Identifier stageId = APItemRegistry.getStageId(apItemId);
                MinecraftArchipelagoClient.LOGGER.info("[AP] → stage lookup result: {}", stageId);
                if (stageId == null) return;

                if (state.unlock(serverPlayer.getUuid(), stageId)) {
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