package com.minecraftarchipelago.client;

import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.Set;

public final class ClientLockedItems {

    private static Set<Identifier> lockedItemIds = Set.of();

    public static boolean isLocked(ItemStack stack) {
        return !stack.isEmpty()
                && lockedItemIds.contains(Registries.ITEM.getId(stack.getItem()));
    }

    public static void replace(Set<Identifier> itemIds) {
        lockedItemIds = Set.copyOf(itemIds);
    }

    public static void clear() {
        lockedItemIds = Set.of();
    }

    private ClientLockedItems() {}
}
