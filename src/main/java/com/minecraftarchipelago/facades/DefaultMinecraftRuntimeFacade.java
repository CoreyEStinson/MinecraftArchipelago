package com.minecraftarchipelago.facades;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

public final class DefaultMinecraftRuntimeFacade implements MinecraftRuntimeFacade {
    @Override
    public void executeOnClient(Runnable action) {
        MinecraftClient.getInstance().execute(action);
    }

    @Override
    public @Nullable MinecraftServer getCurrentServer() {
        return MinecraftClient.getInstance().getServer();
    }

    @Override
    public @Nullable PlayerEntity getCurrentPlayer() {
        return MinecraftClient.getInstance().player;
    }
}
