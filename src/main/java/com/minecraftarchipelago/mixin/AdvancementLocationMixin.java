package com.minecraftarchipelago.mixin;

import com.minecraftarchipelago.aplocations.AdvancementLocationHandler;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerAdvancementTracker.class)
public abstract class AdvancementLocationMixin {
    @Shadow private ServerPlayerEntity owner;
    @Shadow public abstract AdvancementProgress getProgress(AdvancementEntry advancement);

    @Inject(method = "grantCriterion", at = @At("RETURN"))
    private void onCriterionGranted(
            AdvancementEntry advancement,
            String criterionName,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!cir.getReturnValue()) return;

        AdvancementProgress progress = getProgress(advancement);
        if (!progress.isDone()) return;

        MinecraftServer server = owner.getServer();
        if (server == null) return;

        AdvancementLocationHandler.handleCompletedAdvancement(server, owner, advancement.id());
    }
}
