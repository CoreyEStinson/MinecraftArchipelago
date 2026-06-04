package com.minecraftarchipelago.facades;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

public final class ServerSafeRuntimeFacade implements MinecraftRuntimeFacade {
    @Override
    public void executeOnClient(Runnable action) {
        action.run();
    }

    @Override
    public @Nullable MinecraftServer getCurrentServer() {
        return null;
    }

    @Override
    public @Nullable PlayerEntity getCurrentPlayer() {
        return null;
    }
}
