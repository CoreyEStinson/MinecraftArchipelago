package com.minecraftarchipelago.mixin;

import com.minecraftarchipelago.APSession;
import com.minecraftarchipelago.DeathLinkHandler;
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
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        DeathLinkHandler.maybeSendDeathLink(player);
    }
}
