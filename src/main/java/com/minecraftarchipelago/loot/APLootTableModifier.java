package com.minecraftarchipelago.loot;

import com.minecraftarchipelago.item.ModItems;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;

public class APLootTableModifier {

    public static void register() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (!source.isBuiltin()) return;

            var lootSource = APLootSourceRegistry.findLootTableSource(key.getValue());
            if (lootSource.isEmpty()) return;

            APLootSource sourceDef = lootSource.get();
            tableBuilder.pool(
                    LootPool.builder()
                            .rolls(ConstantLootNumberProvider.create(1))
                            .conditionally(RandomChanceLootCondition.builder(sourceDef.chance()))
                            .with(ItemEntry.builder(ModItems.ARCHIPELAGO_CHECK)
                                    .apply(SetLootSourceFunction.builder(
                                            sourceDef.id(),
                                            sourceDef.displayName()))
                                    .apply(AssignLootableCheckFunction.builder()))
            );
        });
    }

    private APLootTableModifier() {
    }
}
