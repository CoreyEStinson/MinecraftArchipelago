package com.minecraftarchipelago.mixin;

import com.minecraftarchipelago.APSession;
import com.minecraftarchipelago.MinecraftArchipelago;
import com.minecraftarchipelago.aplocations.CheckedLocationsState;
import com.minecraftarchipelago.aplocations.LocationRegistry;
import com.minecraftarchipelago.aplocations.VictoryCondition;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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

        Long locationId = LocationRegistry.getLocationId(advancement.id());
        if (locationId == null) return;

        // We're on the server thread here — safe to read/write server state
        MinecraftServer server = owner.getServer();
        if (server == null) return;

        // Deduplication: returns false if already sent in a previous session
        CheckedLocationsState state = CheckedLocationsState.get(server);
        boolean isNew = state.checkLocation(locationId);
        if (!isNew) return;

        boolean isConnected = APSession.CLIENT.isConnected();
        if (isConnected) {
            MinecraftClient.getInstance().execute(() -> {
                if (!APSession.CLIENT.isConnected()) return;
                APSession.CLIENT.checkLocation(locationId);
            });
        } else {
            // Disconnected - check is already saved to NBT by checkLocation above.
            // Let the player know it will sync automatically
            MinecraftClient.getInstance().execute(() -> {
                var player = MinecraftClient.getInstance().player;
                if (player != null) {
                    player.sendMessage(
                            Text.literal("[AP] You are not connected to the server. Check is saved - will sync when reconnected.")
                                    .formatted(Formatting.YELLOW),
                            true
                    );
                }
            });
        }

        VictoryCondition.checkAndAward(server);

        // Newly checked — dispatch to client thread to send to AP
        MinecraftClient.getInstance().execute(() -> {
            if (!APSession.CLIENT.isConnected()) return;
            APSession.CLIENT.checkLocation(locationId);
        });
    }
}
