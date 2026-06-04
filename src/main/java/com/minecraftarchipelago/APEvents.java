package com.minecraftarchipelago;

import com.minecraftarchipelago.apitems.APGiveItemRegistry;
import com.minecraftarchipelago.apitems.APItemRegistry;
import com.minecraftarchipelago.aplocations.CheckedLocationsState;
import com.minecraftarchipelago.aplocations.VictoryCondition;
import com.minecraftarchipelago.apstages.service.StageUnlockApplier;
import com.minecraftarchipelago.apstages.state.StageUnlockState;
import io.github.archipelagomw.events.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Map;

public class APEvents {

    @ArchipelagoEventListener
    public void onConnected(ConnectionResultEvent e) {
        SlotData data = SlotData.parse(e.getSlotData(Map.class));
        APSession.setSlotData(data);

        if (data.isDeathLinkEnabled()) {
            APSession.client().setDeathLinkEnabled(true);
        }

        MinecraftArchipelago.LOGGER.info(
                "[AP] Connected. Slot data received: {}", data);

        APSession.runtime().executeOnClient(() -> {
            MinecraftServer currentServer = APSession.runtime().getCurrentServer();
            if (currentServer != null) {
                currentServer.execute(() -> APConnectionState.get(currentServer).save(
                        APSession.getPendingHost(),
                        APSession.getPendingPort(),
                        APSession.getPendingSlot(),
                        APSession.getPendingPassword()
                ));
            }

            var player = APSession.runtime().getCurrentPlayer();
            if (player != null) {
                String msg = "[AP] Connected! Goal: " + data.getAdvancementGoalPercent() + "% of advancements.";
                if (data.isDeathLinkEnabled()) msg += " Death Link is ON";
                player.sendMessage(Text.literal(msg));
            }
        });

        MinecraftServer server = APSession.runtime().getCurrentServer();
        if (server == null) return;
        server.execute(() -> resendCheckedLocationsOnConnect(server));
    }

    @ArchipelagoEventListener
    public void onItemReceived(ReceiveItemEvent e) {
        Long itemId = e.getItemID();
        int itemIndex = (int) e.getIndex();

        if (itemId == null) {
            MinecraftArchipelago.LOGGER.warn("[AP] Received item with null ID, skipping");
            return;
        }

        long apItemId = itemId;
        MinecraftArchipelago.LOGGER.info("[AP] Item received, ID: {}", apItemId);

        APSession.runtime().executeOnClient(() -> {
            MinecraftServer server = APSession.runtime().getCurrentServer();
            var currentPlayer = APSession.runtime().getCurrentPlayer();
            if (server == null || currentPlayer == null) return;

            server.execute(() -> {
                ServerPlayerEntity serverPlayer =
                        server.getPlayerManager().getPlayer(currentPlayer.getName().getString());
                if (serverPlayer == null) return;

                handleReceivedItem(server, serverPlayer, apItemId, itemIndex);
            });
        });
    }

    @ArchipelagoEventListener
    public void onLocationInfo(LocationInfoEvent e) {
        MinecraftArchipelago.LOGGER.info("[AP] Got location info for {} locations", e.locations.size());

        APSession.runtime().executeOnClient(() -> {
            MinecraftServer server = APSession.runtime().getCurrentServer();
            if (server == null) return;

            server.execute(() -> {
                for (io.github.archipelagomw.parts.NetworkItem item : e.locations) {
                    com.minecraftarchipelago.loot.LootableItemNameCache.put(item.locationID, item.itemName, item.playerName);
                    com.minecraftarchipelago.loot.ChestOpenHandler.applyNameToAllMatchingItems(server, item.locationID);
                }
            });
        });
    }

    @ArchipelagoEventListener
    public void onRetrieved(RetrievedEvent e) {
        System.out.println("Datastorage retrieved: " + e);
    }

    @ArchipelagoEventListener
    public void onCheckedLocations(CheckedLocationsEvent e) {
        APSession.runtime().executeOnClient(() -> {
            MinecraftServer server = APSession.runtime().getCurrentServer();
            if (server == null) return;

            server.execute(() -> {
                CheckedLocationsState state = CheckedLocationsState.get(server);
                boolean changed = false;
                for (Long locationId : e.checkedLocations) {
                    if (state.checkLocation(locationId)) {
                        changed = true;
                    }
                }
                if (changed) {
                    MinecraftArchipelago.LOGGER.info("[AP] Updated checked locations from server sync.");
                    VictoryCondition.checkAndAward(server);
                }
            });
        });
    }

    @ArchipelagoEventListener
    public void onPrint(PrintJSONEvent e) {
        int itemFlags = (e.item != null) ? e.item.flags : -1;

        Text message = APMessageFormatter.build(e.apPrint.getPlainText(), itemFlags);
        APSession.runtime().executeOnClient(() -> {
            var player = APSession.runtime().getCurrentPlayer();
            if (player != null) player.sendMessage(message);
        });
    }

    @ArchipelagoEventListener
    public void onDeathLink(DeathLinkEvent e) {
        String source = e.source;

        APSession.runtime().executeOnClient(() -> {
            MinecraftServer server = APSession.runtime().getCurrentServer();
            var currentPlayer = APSession.runtime().getCurrentPlayer();
            if (server == null || currentPlayer == null) return;

            server.execute(() -> {
                ServerPlayerEntity player =
                        server.getPlayerManager().getPlayer(currentPlayer.getName().getString());
                DeathLinkHandler.applyReceivedDeathLink(player, source);
            });
        });
    }

    static void handleReceivedItem(MinecraftServer server,
                                   ServerPlayerEntity serverPlayer,
                                   long apItemId,
                                   int itemIndex) {
        ReceivedItemDecision decision = decideReceivedItem(server, serverPlayer.getUuid(), apItemId, itemIndex);
        if (decision.duplicate()) {
            MinecraftArchipelago.LOGGER.info("[AP] -> index {} already processed, skipping", itemIndex);
            return;
        }

        if (decision.giveEntry() != null) {
            MinecraftArchipelago.LOGGER.info("[AP] -> give item found");
            Item item = Registries.ITEM.get(decision.giveEntry().itemId());
            ItemStack stack = new ItemStack(item, decision.giveEntry().count());
            if (!serverPlayer.getInventory().insertStack(stack)) {
                serverPlayer.dropItem(stack, false);
            }

            serverPlayer.playSoundToPlayer(
                    SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
                    SoundCategory.PLAYERS,
                    1.0f,
                    1.0f
            );
            return;
        }

        if (decision.stageId() != null) {
            StageUnlockState state = StageUnlockState.get(server);
            if (state.unlock(serverPlayer.getUuid(), decision.stageId())) {
                StageUnlockApplier.apply(serverPlayer, decision.stageId());
            }
        }
    }

    static ReceivedItemDecision decideReceivedItem(MinecraftServer server,
                                                   java.util.UUID playerId,
                                                   long apItemId,
                                                   int itemIndex) {
        CheckedLocationsState checkedState = CheckedLocationsState.get(server);
        if (!checkedState.isNewItem(itemIndex)) {
            return new ReceivedItemDecision(true, null, null);
        }

        if (APGiveItemRegistry.isGiveItem(apItemId)) {
            return new ReceivedItemDecision(false, APGiveItemRegistry.getEntry(apItemId), null);
        }

        if (APItemRegistry.isProgressive(apItemId)) {
            StageUnlockState state = StageUnlockState.get(server);
            Identifier stageId = APItemRegistry.getNextTier(apItemId, state.getUnlocked(playerId));
            MinecraftArchipelago.LOGGER.info("[AP] -> progressive item found");
            return new ReceivedItemDecision(false, null, stageId);
        }

        Identifier stageId = APItemRegistry.getStageId(apItemId);
        MinecraftArchipelago.LOGGER.info("[AP] -> stage lookup result: {}", stageId);
        return new ReceivedItemDecision(false, null, stageId);
    }

    static void resendCheckedLocationsOnConnect(MinecraftServer server) {
        CheckedLocationsState state = CheckedLocationsState.get(server);

        if (!state.getAllChecked().isEmpty()) {
            MinecraftArchipelago.LOGGER.info(
                    "[AP] Resending {} previously check locations.",
                    state.getAllChecked().size()
            );
        }

        ServerPlayerEntity player = server.getPlayerManager().getPlayerList().isEmpty()
                ? null
                : server.getPlayerManager().getPlayerList().getFirst();
        if (player != null) {
            net.minecraft.advancement.PlayerAdvancementTracker tracker = player.getAdvancementTracker();
            for (net.minecraft.advancement.AdvancementEntry adv : server.getAdvancementLoader().getAdvancements()) {
                if (tracker.getProgress(adv).isDone()) {
                    Long locId = com.minecraftarchipelago.aplocations.LocationRegistry.getLocationId(adv.id());
                    if (locId != null) {
                        state.checkLocation(locId);
                    }
                }
            }
        }

        for (long locationId : state.getAllChecked()) {
            APSession.client().checkLocation(locationId);
        }

        VictoryCondition.resendIfAchieved(server);
    }

    record ReceivedItemDecision(boolean duplicate,
                                APGiveItemRegistry.GiveEntry giveEntry,
                                Identifier stageId) {
    }
}
