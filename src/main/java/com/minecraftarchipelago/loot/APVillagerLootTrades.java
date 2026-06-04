package com.minecraftarchipelago.loot;

import com.minecraftarchipelago.item.ModItems;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradedItem;
import net.minecraft.village.VillagerProfession;

import java.util.Optional;

public final class APVillagerLootTrades {
    public static void register() {
        for (VillagerProfession profession : Registries.VILLAGER_PROFESSION) {
            if (profession == VillagerProfession.NONE || profession == VillagerProfession.NITWIT) {
                continue;
            }
            registerLevel(profession, 4);
            registerLevel(profession, 5);
        }
    }

    public static TradeOffer createOffer(String sourceId, String sourceName, ItemConvertible costItem, int costCount) {
        return new TradeOffer(
                new TradedItem(costItem, costCount),
                Optional.empty(),
                APLootSourceItemFactory.create(sourceId, sourceName),
                1,
                12,
                0.05f
        );
    }

    private static void registerLevel(VillagerProfession profession, int level) {
        TradeOfferHelper.registerVillagerOffers(profession, level, factories -> {
            factories.add((entity, random) -> APLootSourceRegistry.getVillagerTradeSourcesForLevel(level).stream()
                    .filter(source -> random.nextFloat() < source.chance())
                    .findFirst()
                    .map(source -> createOffer(
                            source.id(),
                            source.displayName(),
                            Items.EMERALD,
                            level == 5 ? 24 : 16))
                    .orElse(null));
        });
    }

    private APVillagerLootTrades() {
    }
}
