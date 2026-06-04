package com.minecraftarchipelago.loot;

import com.minecraftarchipelago.APSession;
import com.minecraftarchipelago.item.ArchipelagoCheckItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;

public final class ChestOpenHandler {

    /**
     * Writes cached item name and recipient into the stack's NBT if a scout
     * result has arrived and the name hasn't been set yet.
     * Called from both AssignLootableCheckFunction and inventoryTick.
     */
    public static void maybeApplyCachedName(ItemStack stack, NbtCompound nbt) {
        if (!nbt.getBoolean(ArchipelagoCheckItem.NBT_ASSIGNED)) return;
        if (nbt.contains(ArchipelagoCheckItem.NBT_AP_ITEM_NAME)) return;

        long locationId = nbt.getLong(ArchipelagoCheckItem.NBT_LOCATION_ID);
        String[] info = LootableItemNameCache.get(locationId);
        if (info == null) return;

        nbt.putString(ArchipelagoCheckItem.NBT_AP_ITEM_NAME, info[0]);
        nbt.putString(ArchipelagoCheckItem.NBT_AP_PLAYER_NAME, info[1]);
        ArchipelagoCheckItem.setCustomData(stack, nbt);
    }

    /**
     * Requests a location scout from the AP server for the given location ID.
     * When the result arrives via the LocationInfo event in APEvents,
     * call LootableItemNameCache.put(...) and then applyNameToAllMatchingItems(...).
     */
    public static void requestScout(long locationId) {
        ArrayList<Long> locations = new ArrayList<>();
        locations.add(locationId);
        APSession.client().scoutLocations(locations);
    }

    /**
     * Scans all players in all loaded worlds for AP items with the given
     * location ID and applies the cached name to them.
     * Call this from APEvents when a scout result arrives.
     */
    public static void applyNameToAllMatchingItems(MinecraftServer server,
                                                   long locationId) {
        for (ServerWorld world : server.getWorlds()) {
            for (ServerPlayerEntity player : world.getPlayers()) {
                for (int i = 0; i < player.getInventory().size(); i++) {
                    ItemStack stack = player.getInventory().getStack(i);
                    if (!(stack.getItem() instanceof ArchipelagoCheckItem)) continue;

                    NbtCompound nbt = ArchipelagoCheckItem.getCustomData(stack);
                    if (nbt.getLong(ArchipelagoCheckItem.NBT_LOCATION_ID) == locationId) {
                        maybeApplyCachedName(stack, nbt);
                    }
                }
            }
        }
    }

    private ChestOpenHandler() {}
}
