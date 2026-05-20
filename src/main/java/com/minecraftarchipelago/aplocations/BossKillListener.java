package com.minecraftarchipelago.aplocations;

import com.minecraftarchipelago.APSession;
import com.minecraftarchipelago.MinecraftArchipelago;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

public final class BossKillListener {

    public static void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(entity.getWorld() instanceof ServerWorld serverWorld)) return;

            Long locationId = BossKillLocationRegistry.getLocationId(entity.getType());
            if (locationId == null) return;

            MinecraftServer server = serverWorld.getServer();
            CheckedLocationsState state = CheckedLocationsState.get(server);

            if (!state.checkLocation(locationId)) return;

            MinecraftArchipelago.LOGGER.info(
                    "[AP] Boss killed, checking location {}", locationId
            );

            MinecraftClient.getInstance().execute(() -> {
                if (!APSession.CLIENT.isConnected()) return;
                APSession.CLIENT.checkLocation(locationId);
            });

            VictoryCondition.checkAndAward(server);
        });
    }

    private BossKillListener() {}
}
