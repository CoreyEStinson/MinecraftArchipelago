package com.minecraftarchipelago.apitems.state;

import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProgressiveGiftState extends PersistentState {
    public static final String DATA_ID = "ap_progressive_gifts";

    public static final PersistentState.Type<ProgressiveGiftState> TYPE =
            new PersistentState.Type<>(
                    ProgressiveGiftState::new,
                    ProgressiveGiftState::fromNbt,
                    DataFixTypes.LEVEL
            );

    private final Map<UUID, Map<Long, Integer>> grantedTierCounts = new HashMap<>();

    public static ProgressiveGiftState get(MinecraftServer server) {
        ServerWorld overworld = server.getOverworld();
        PersistentStateManager stateManager = overworld.getPersistentStateManager();
        return stateManager.getOrCreate(TYPE, DATA_ID);
    }

    private static ProgressiveGiftState fromNbt(
            NbtCompound nbt,
            RegistryWrapper.WrapperLookup registries
    ) {
        ProgressiveGiftState state = new ProgressiveGiftState();

        NbtList players = nbt.getList("players", NbtElement.COMPOUND_TYPE);
        for (int playerIndex = 0; playerIndex < players.size(); playerIndex++) {
            NbtCompound playerEntry = players.getCompound(playerIndex);
            UUID playerId = playerEntry.getUuid("uuid");

            Map<Long, Integer> itemCounts = new HashMap<>();
            NbtList items = playerEntry.getList("items", NbtElement.COMPOUND_TYPE);

            for (int itemIndex = 0; itemIndex < items.size(); itemIndex++) {
                NbtCompound itemEntry = items.getCompound(itemIndex);
                long apItemId = itemEntry.getLong("ap_item_id");
                int grantedCount = itemEntry.getInt("granted_count");

                if (grantedCount > 0) {
                    itemCounts.put(apItemId, grantedCount);
                }
            }

            if (!itemCounts.isEmpty()) {
                state.grantedTierCounts.put(playerId, itemCounts);
            }
        }

        return state;
    }

    @Override
    public NbtCompound writeNbt(
            NbtCompound nbt,
            RegistryWrapper.WrapperLookup registries
    ) {
        NbtList players = new NbtList();

        for (var playerEntry : grantedTierCounts.entrySet()) {
            NbtCompound playerNbt = new NbtCompound();
            playerNbt.putUuid("uuid", playerEntry.getKey());

            NbtList items = new NbtList();
            for (var itemEntry : playerEntry.getValue().entrySet()) {
                NbtCompound itemNbt = new NbtCompound();
                itemNbt.putLong("ap_item_id", itemEntry.getKey());
                itemNbt.putInt("granted_count", itemEntry.getValue());
                items.add(itemNbt);
            }

            playerNbt.put("items", items);
            players.add(playerNbt);
        }

        nbt.put("players", players);
        return nbt;
    }

    public int getNextTier(UUID playerId, long apItemId, int tierCount) {
        int grantedCount = grantedTierCounts
                .getOrDefault(playerId, Map.of())
                .getOrDefault(apItemId, 0);

        return grantedCount < tierCount ? grantedCount : -1;
    }

    public boolean markTierGranted(UUID playerId, long apItemId, int tierIndex) {
        Map<Long, Integer> itemCounts =
                grantedTierCounts.computeIfAbsent(playerId, ignored -> new HashMap<>());

        int currentCount = itemCounts.getOrDefault(apItemId, 0);
        if (currentCount != tierIndex) {
            return false;
        }

        itemCounts.put(apItemId, currentCount + 1);
        markDirty();
        return true;
    }
}
