package com.minecraftarchipelago;


import com.minecraftarchipelago.apitems.APGiveItemRegistry;
import com.minecraftarchipelago.apitems.APItemRegistry;
import com.minecraftarchipelago.aplocations.CheckedLocationsState;
import com.minecraftarchipelago.aplocations.VictoryCondition;
import com.minecraftarchipelago.apstages.service.StageUnlockApplier;
import com.minecraftarchipelago.apstages.state.StageUnlockState;
import com.minecraftarchipelago.item.ArchipelagoCheckItem;
import com.minecraftarchipelago.loot.LootCheckAssignmentState;
import com.minecraftarchipelago.loot.LootCheckScoutPending;
import io.github.archipelagomw.events.*;
import io.github.archipelagomw.parts.NetworkItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class APEvents {

    @ArchipelagoEventListener
    public void onConnected(ConnectionResultEvent e){
        // e.slotData is the raw map from the APWorld's generate() method.
        // Verify the exact field name against the Java client library.
        SlotData data = SlotData.parse(e.getSlotData(Map.class));
        APSession.setSlotData(data);

        // Enable Death Link tag on the AP connection if the option is on.
        if (data.isDeathLinkEnabled()) {
            APSession.CLIENT.setDeathLinkEnabled(true);
        }

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
                String msg = "[AP] Connected! Goal: " + data.getAdvancementGoalPercent() + "% of advancements.";
                if (data.isDeathLinkEnabled()) msg += " Death Link is ON";
                player.sendMessage(Text.literal(msg));
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

        MinecraftClient mc = MinecraftClient.getInstance();
        // Re-scout any tokens in inventory that are assigned but not yet named
        if (mc.player != null) {
            ServerPlayerEntity serverPlayer =
                    server.getPlayerManager().getPlayer(mc.player.getUuid());
            if (serverPlayer != null) {
                List<Long> needsScout = new ArrayList<>();
                for (int i = 0; i < serverPlayer.getInventory().size(); i++) {
                    ItemStack stack = serverPlayer.getInventory().getStack(i);
                    if (stack.getItem() instanceof ArchipelagoCheckItem
                            && ArchipelagoCheckItem.isAssigned(stack)
                            && !ArchipelagoCheckItem.isScouted(stack)) {
                        needsScout.add(ArchipelagoCheckItem.getLocationId(stack));
                    }
                }
                if (!needsScout.isEmpty()) {
                    needsScout.forEach(id -> {
                        ArrayList<Long> locations = new ArrayList<>();
                        locations.add(id);
                        APSession.CLIENT.scoutLocations(locations);
                    });
                }
            }
        }
    }

    @ArchipelagoEventListener
    public void onItemReceived(ReceiveItemEvent e) {
        Long itemId = e.getItemID();
        int itemIndex = (int) e.getIndex();

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

                CheckedLocationsState checkedState = CheckedLocationsState.get(server);
                if (!checkedState.isNewItem(itemIndex)) {
                    MinecraftArchipelagoClient.LOGGER.info("[AP] → index {} already processed, skipping", itemIndex);
                    return;
                }

                // Case 1: filler give item — give directly, no stage needed
                if (APGiveItemRegistry.isGiveItem(apItemId)) {
                    MinecraftArchipelagoClient.LOGGER.info("[AP] → give item found");
                    var entry = APGiveItemRegistry.getEntry(apItemId);
                    Item item = Registries.ITEM.get(entry.itemId());
                    ItemStack stack = new ItemStack(item, entry.count());
                    if (!serverPlayer.getInventory().insertStack(stack)) {
                        serverPlayer.dropItem(stack, false);
                    }

                    serverPlayer.playSoundToPlayer(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0f, 1.0f);

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

        if (e.locations == null || e.locations.isEmpty()) return;


        MinecraftClient mc = MinecraftClient.getInstance();
        mc.execute(() -> {
            MinecraftServer server = mc.getServer();
            if (server == null) return;

            server.execute(() -> {
                LootCheckAssignmentState assignState = LootCheckAssignmentState.get(server);

                for (NetworkItem item : e.locations) {
                    long locationId = item.locationID;
                    String apItemName = item.itemName != null ? item.itemName : "???";
                    String apPlayer = item.playerName != null ? item.playerName : "???";
                    int checkIndex = assignState.getCheckedIndex(locationId);

                    LootCheckScoutPending.resolve(locationId, checkIndex, apItemName, apPlayer);

                    if (mc.player != null) {
                        ServerPlayerEntity serverPlayer =
                                server.getPlayerManager().getPlayer(mc.player.getUuid());
                        if (serverPlayer != null) {
                            updateInventory(serverPlayer, locationId, checkIndex, apItemName, apPlayer);
                        }
                    }
                }
            });
        });
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

    @ArchipelagoEventListener
    public void onDeathLink(DeathLinkEvent e){
        String source = e.source;

        MinecraftClient mc = MinecraftClient.getInstance();
        mc.execute(() -> {
            MinecraftServer server = mc.getServer();
            if (server == null) return;

            server.execute(() -> {
                if (mc.player == null) return;
                ServerPlayerEntity player =
                        server.getPlayerManager().getPlayer(mc.player.getName().getString());
                if (player == null || player.isDead()) return;

                // Flag to prevent the death from triggering another send
                DeathLinkHandler.setReceivingDeathLink(true);

                // Kill the player with void damage
                player.damage(
                        player.getDamageSources().outOfWorld(),
                        Float.MAX_VALUE
                );

                DeathLinkHandler.setReceivingDeathLink(false);

                // Show who send the death
                mc.execute(() -> {
                    if (mc.player != null) {
                        mc.player.sendMessage(
                                Text.literal("[AP] Death Link received. Sent by " + source)
                        );
                    }
                });
            });
        });
    }

    private static void updateInventory(ServerPlayerEntity player, long locationId, int checkIndex, String itemName, String playerName) {
        boolean changed = false;
        for (int i = 0; i < player.getInventory().size(); i++){
           ItemStack stack = player.getInventory().getStack(i);
           if (!(stack.getItem() instanceof ArchipelagoCheckItem)) continue;
           if (ArchipelagoCheckItem.getLocationId(stack) != locationId) continue;

           ArchipelagoCheckItem.setCheckData(stack, locationId, checkIndex, itemName, playerName);
           changed = true;
        }
        if (changed) player.getInventory().markDirty();
    }
}