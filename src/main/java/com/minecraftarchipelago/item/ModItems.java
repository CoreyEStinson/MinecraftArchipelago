package com.minecraftarchipelago.item;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Rarity;
import net.minecraft.util.Identifier;

public class ModItems {

    public static final Item ARCHIPELAGO_CHECK = Registry.register(
            Registries.ITEM,
            Identifier.of("minecraftarchipelago", "ap_check_item"),
            new ArchipelagoCheckItem(
                    new Item.Settings()
                            .maxCount(1)
                            .fireproof()
                            .rarity(Rarity.EPIC)
            )
    );

    public static void register() {}

    private ModItems() {}
}
