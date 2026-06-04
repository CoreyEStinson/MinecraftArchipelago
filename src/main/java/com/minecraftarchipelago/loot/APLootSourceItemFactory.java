package com.minecraftarchipelago.loot;

import com.minecraftarchipelago.item.ArchipelagoCheckItem;
import com.minecraftarchipelago.item.ModItems;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public final class APLootSourceItemFactory {
    public static ItemStack create(String sourceId, String sourceName) {
        ItemStack stack = new ItemStack(ModItems.ARCHIPELAGO_CHECK);
        NbtCompound nbt = ArchipelagoCheckItem.getCustomData(stack);
        nbt.putString(ArchipelagoCheckItem.NBT_LOOT_SOURCE, sourceId);
        nbt.putString(ArchipelagoCheckItem.NBT_LOOT_SOURCE_NAME, sourceName);
        ArchipelagoCheckItem.setCustomData(stack, nbt);
        return stack;
    }

    private APLootSourceItemFactory() {
    }
}
