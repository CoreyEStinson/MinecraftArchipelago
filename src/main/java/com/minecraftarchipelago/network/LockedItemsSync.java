package com.minecraftarchipelago.network;

import com.minecraftarchipelago.apstages.item.ItemAccessHelper;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class LockedItemsSync {

    public static void send(ServerPlayerEntity player) {
        List<Identifier> lockedItemIds = new ArrayList<>();

        for (Item item : Registries.ITEM) {
            ItemStack stack = new ItemStack(item);

            if (ItemAccessHelper.isLocked(player, stack)) {
                lockedItemIds.add(Registries.ITEM.getId(item));
            }
        }

        lockedItemIds.sort(Comparator.comparing(Identifier::toString));

        ServerPlayNetworking.send(player, new LockedItemsPayload(lockedItemIds));
    }

    private LockedItemsSync() {}
}
