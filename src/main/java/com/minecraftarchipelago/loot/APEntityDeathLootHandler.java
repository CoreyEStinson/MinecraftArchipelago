package com.minecraftarchipelago.loot;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.registry.Registries;

public final class APEntityDeathLootHandler {
    public static void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            var entityId = Registries.ENTITY_TYPE.getId(entity.getType());
            var server = entity.getServer();
            if (server == null) return;

            for (APLootSource source : APLootSourceRegistry.getAll()) {
                if (source.type() != APLootSource.Type.ENTITY_DEATH_FALLBACK) continue;
                if (!source.entities().contains(entityId)) continue;
                if (entity.getRandom().nextFloat() >= source.chance()) continue;

                var stack = APLootSourceItemFactory.create(source.id(), source.displayName());
                AssignLootableCheckFunction.assignLootableCheck(stack, server);
                entity.dropStack(stack);
            }
        });
    }

    private APEntityDeathLootHandler() {
    }
}
