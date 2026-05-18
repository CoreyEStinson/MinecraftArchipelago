package com.minecraftarchipelago.item;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public class APItems {

    public static final ArchipelagoCheckItem CHECK_TOKEN = Registry.register(
            Registries.ITEM,
            Identifier.of("minecraftarchipelago", "check_token"),
            new ArchipelagoCheckItem(
                    new Item.Settings()
                            .maxCount(1)
                            .rarity(Rarity.EPIC)
                            .fireproof()
            )
    );

    public static void register() {}

    private APItems() {}
}
