package com.minecraftarchipelago.mixin;

import com.minecraftarchipelago.APSession;
import com.minecraftarchipelago.DeathLinkHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class PlayerDeathMixin {

    @Inject(method = "onDeath", at = @At("HEAD"))
    private void onPlayerDeath(DamageSource source, CallbackInfo ci){
        // Don't send if we're dying FROM a received Death Link
        if (DeathLinkHandler.isReceivingDeathLink()) return;

        if( !APSession.CLIENT.isConnected()) return;
        if (!APSession.hasSlotData()) return;
        if (!APSession.getSlotData().isDeathLinkEnabled()) return;

        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        String cause = player.getName().getString() + " died in Minecraft";

        MinecraftClient.getInstance().execute(() -> {
            APSession.CLIENT.sendDeathlink(APSession.getPendingSlot(), cause);
        });
    }
}
