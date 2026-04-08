package com.minecraftarchipelago.mixin;

import com.minecraftarchipelago.APSession;
import com.minecraftarchipelago.MinecraftArchipelago;
import com.minecraftarchipelago.aplocations.LocationRegistry;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.client.MinecraftClient;
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
        // grantCriterion returns false if the criterion was already done - skip
        if (!cir.getReturnValue()) return;

        // Check if ALL criteria on this advancement are now complete
        AdvancementProgress progress = getProgress(advancement);
        if (!progress.isDone()) return;

        // Look up the AP location ID for this advancement
        Long locationId = LocationRegistry.getLocationId(advancement.id());
        if (locationId == null) return;

        MinecraftClient.getInstance().execute(() -> {
            if (!APSession.CLIENT.isConnected()) return;
            APSession.CLIENT.checkLocation(locationId);
            MinecraftArchipelago.LOGGER.info("Advancement received! {}", advancement.id());
        });
    }
}
