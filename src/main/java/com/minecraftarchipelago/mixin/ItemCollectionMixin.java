package com.minecraftarchipelago.mixin;

import com.minecraftarchipelago.aplocations.VictoryCondition;
import com.minecraftarchipelago.collections.CollectedItemsState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInventory.class)
public class ItemCollectionMixin {

    @Shadow
    public PlayerEntity player;

    /**
     * Fires whenever any inventory slot changes. Intercepts non-empty stacks
     * server side and records the item type as ever-held.
     */
    @Inject(method = "setStack", at = @At("HEAD"))
    private void onSetStack(int slot, ItemStack stack, CallbackInfo ci) {
        // Only non-empty stacks
        if(stack.isEmpty()) return;
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return;

        MinecraftServer server = serverPlayer.getServer();
        if (server == null) return;

        Identifier itemId = Registries.ITEM.getId(stack.getItem());
        CollectedItemsState state = CollectedItemsState.get(server);

        if (state.markCollected(itemId)) {
            VictoryCondition.checkAndAward(server);
        }
    }

    /**
     * Fires when any item is inserted into the inventory (including ground pickups
     * that merge with existing partial stacks). The setStack injection misses the
     * merge case because incrementing a stack count bypasses setStack entirely.
     *
     * This covers: picking up item entities from the ground, hopper delivery,
     * and any code path that calls insertStack(ItemStack) directly.
     *
     * Using HEAD so we record the item type before any merging/slot logic runs.
     * The CallbackInfoReturnable import is needed for the boolean return type.
     */
    @Inject(method = "insertStack(Lnet/minecraft/item/ItemStack;)Z", at = @At("HEAD"))
    private void onInsertStack(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.isEmpty()) return;
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return;

        MinecraftServer server = serverPlayer.getServer();
        if (server == null) return;

        Identifier itemId = Registries.ITEM.getId(stack.getItem());
        if (CollectedItemsState.get(server).markCollected(itemId)) {
            VictoryCondition.checkAndAward(server);
        }
    }
}
