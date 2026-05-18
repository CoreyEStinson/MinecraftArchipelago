package com.minecraftarchipelago.loot;

import com.minecraftarchipelago.item.APItems;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.util.Identifier;

import java.util.Set;

public final class APLootTables {

    private static final float SPAWN_CHANCE = 0.30f;

    // Target loot tables

    private static final Set<Identifier> TARGET_TABLES = Set.of(

            // Overworld Structures
            Identifier.of("minecraft", "chests/abandoned_mineshaft"),
            Identifier.of("minecraft", "chests/buried_treasure"),
            Identifier.of("minecraft", "chests/desert_pyramid"),
            Identifier.of("minecraft", "chests/igloo_chest"),
            Identifier.of("minecraft", "chests/jungle_temple"),
            Identifier.of("minecraft", "chests/pillager_outpost"),
            Identifier.of("minecraft", "chests/shipwreck_map"),
            Identifier.of("minecraft", "chests/shipwreck_supply"),
            Identifier.of("minecraft", "chests/shipwreck_treasure"),
            Identifier.of("minecraft", "chests/simple_dungeon"),
            Identifier.of("minecraft", "chests/stronghold_corridor"),
            Identifier.of("minecraft", "chests/stronghold_crossing"),
            Identifier.of("minecraft", "chests/stronghold_library"),
            Identifier.of("minecraft", "chests/underwater_ruin_big"),
            Identifier.of("minecraft", "chests/underwater_ruin_small"),
            Identifier.of("minecraft", "chests/woodland_mansion"),

            // Villages
            Identifier.of("minecraft", "chests/village/village_armorer"),
            Identifier.of("minecraft", "chests/village/village_butcher"),
            Identifier.of("minecraft", "chests/village/village_cartographer"),
            Identifier.of("minecraft", "chests/village/village_desert_house"),
            Identifier.of("minecraft", "chests/village/village_fisher"),
            Identifier.of("minecraft", "chests/village/village_fletcher"),
            Identifier.of("minecraft", "chests/village/village_mason"),
            Identifier.of("minecraft", "chests/village/village_plains_house"),
            Identifier.of("minecraft", "chests/village/village_savanna_house"),
            Identifier.of("minecraft", "chests/village/village_shepherd"),
            Identifier.of("minecraft", "chests/village/village_snowy_house"),
            Identifier.of("minecraft", "chests/village/village_taiga_house"),
            Identifier.of("minecraft", "chests/village/village_tannery"),
            Identifier.of("minecraft", "chests/village/village_temple"),
            Identifier.of("minecraft", "chests/village/village_toolsmith"),
            Identifier.of("minecraft", "chests/village/village_weaponsmith"),

            // Nether
            Identifier.of("minecraft", "chests/nether_bridge"),

            // The End
            Identifier.of("minecraft", "chests/end_city_treasure")
    );

    public static void register() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (!TARGET_TABLES.contains(key.getValue())) return;
            tableBuilder.pool(buildCheckPool());
        });
    }

    private static LootPool.Builder buildCheckPool() {
        return LootPool.builder()
                .rolls(ConstantLootNumberProvider.create(1))
                .conditionally(RandomChanceLootCondition.builder(SPAWN_CHANCE))
                .with(ItemEntry.builder(APItems.CHECK_TOKEN));
    }

    private APLootTables() {}
}
