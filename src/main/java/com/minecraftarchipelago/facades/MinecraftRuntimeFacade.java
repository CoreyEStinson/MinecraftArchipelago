package com.minecraftarchipelago.facades;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

public interface MinecraftRuntimeFacade {
    void executeOnClient(Runnable action);

    @Nullable
    MinecraftServer getCurrentServer();

    @Nullable
    PlayerEntity getCurrentPlayer();
}
