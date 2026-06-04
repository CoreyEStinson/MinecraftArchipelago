package com.minecraftarchipelago.aplocations;

import com.minecraftarchipelago.APSession;
import com.minecraftarchipelago.MinecraftArchipelago;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

public final class BossKillListener {

    public static void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(entity.getWorld() instanceof ServerWorld serverWorld)) return;

            Long locationId = BossKillLocationRegistry.getLocationId(entity.getType());
            if (locationId == null) return;

            MinecraftServer server = serverWorld.getServer();
            handleBossKill(server, locationId);
        });
    }

    public static boolean handleBossKill(MinecraftServer server, long locationId) {
        CheckedLocationsState state = CheckedLocationsState.get(server);
        if (!state.checkLocation(locationId)) return false;

        MinecraftArchipelago.LOGGER.info(
                "[AP] Boss killed, checking location {}", locationId
        );

        APSession.runtime().executeOnClient(() -> {
            if (!APSession.client().isConnected()) return;
            APSession.client().checkLocation(locationId);
        });

        VictoryCondition.checkAndAward(server);
        return true;
    }

    private BossKillListener() {}
}
