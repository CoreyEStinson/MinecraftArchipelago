package com.minecraftarchipelago.loot;

import com.minecraftarchipelago.item.ModItems;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.util.Identifier;

import java.util.Set;

public class APLootTableModifier {

    /** Probability than an AP check item appears in any given chest roll */
    private static final float SPAWN_CHANCE = 1f;

    /** All loot tables that may contain an Archipelago check item */
    private static final Set<Identifier> LOOT_TABLES = Set.of(

            // ── Village ───────────────────────────────────────────────────────
            Identifier.ofVanilla("chests/village/village_armorer"),
            Identifier.ofVanilla("chests/village/village_butcher"),
            Identifier.ofVanilla("chests/village/village_cartographer"),
            Identifier.ofVanilla("chests/village/village_desert_house"),
            Identifier.ofVanilla("chests/village/village_fisher"),
            Identifier.ofVanilla("chests/village/village_fletcher"),
            Identifier.ofVanilla("chests/village/village_mason"),
            Identifier.ofVanilla("chests/village/village_plains_house"),
            Identifier.ofVanilla("chests/village/village_savanna_house"),
            Identifier.ofVanilla("chests/village/village_shepherd"),
            Identifier.ofVanilla("chests/village/village_snowy_house"),
            Identifier.ofVanilla("chests/village/village_taiga_house"),
            Identifier.ofVanilla("chests/village/village_tannery"),
            Identifier.ofVanilla("chests/village/village_temple"),
            Identifier.ofVanilla("chests/village/village_tool_smith"),
            Identifier.ofVanilla("chests/village/village_weaponsmith"),

            // ── Overworld structures ──────────────────────────────────────────
            Identifier.ofVanilla("chests/abandoned_mineshaft"),
            Identifier.ofVanilla("chests/ancient_city"),
            Identifier.ofVanilla("chests/ancient_city_ice_box"),
            Identifier.ofVanilla("chests/buried_treasure"),
            Identifier.ofVanilla("chests/desert_pyramid"),
            Identifier.ofVanilla("chests/igloo_chest"),
            Identifier.ofVanilla("chests/jungle_temple"),
            Identifier.ofVanilla("chests/jungle_temple_dispenser"),
            Identifier.ofVanilla("chests/pillager_outpost"),
            Identifier.ofVanilla("chests/ruined_portal"),
            Identifier.ofVanilla("chests/shipwreck_map"),
            Identifier.ofVanilla("chests/shipwreck_supply"),
            Identifier.ofVanilla("chests/shipwreck_treasure"),
            Identifier.ofVanilla("chests/simple_dungeon"),
            Identifier.ofVanilla("chests/underwater_ruin_big"),
            Identifier.ofVanilla("chests/underwater_ruin_small"),
            Identifier.ofVanilla("chests/woodland_mansion"),

            // ── Stronghold ────────────────────────────────────────────────────
            Identifier.ofVanilla("chests/stronghold_corridor"),
            Identifier.ofVanilla("chests/stronghold_crossing"),
            Identifier.ofVanilla("chests/stronghold_library"),

            // ── Nether ────────────────────────────────────────────────────────
            Identifier.ofVanilla("chests/bastion_bridge"),
            Identifier.ofVanilla("chests/bastion_hoglin_stable"),
            Identifier.ofVanilla("chests/bastion_other"),
            Identifier.ofVanilla("chests/bastion_treasure"),
            Identifier.ofVanilla("chests/nether_bridge"),

            // ── The End ───────────────────────────────────────────────────────
            Identifier.ofVanilla("chests/end_city_treasure")
    );

    public static void register() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {

            if (!source.isBuiltin()) return;
            if (!LOOT_TABLES.contains(key.getValue())) return;

            // Add a separate pool so the AP item is independent of the
            // chest's normal loot rolls. The pool itself has a 30% chance
            // of running. If it runs, exactly one AP check item is added.
            tableBuilder.pool(
                    LootPool.builder()
                            .rolls(ConstantLootNumberProvider.create(1))
                            .conditionally(RandomChanceLootCondition.builder(SPAWN_CHANCE))
                            .with(ItemEntry.builder(ModItems.ARCHIPELAGO_CHECK)
                                    .apply(AssignLootableCheckFunction.builder()))
            );
        });
    }

    private APLootTableModifier() {}
}
