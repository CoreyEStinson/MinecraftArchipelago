package com.minecraftarchipelago.mixin;

import com.minecraftarchipelago.apstages.item.ItemAccessHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class CraftingOutputSlotMixin {

    @Inject(
            method = "canTakeItems",
            at = @At("HEAD"),
            cancellable = true
    )
    private void minecraftarchipelago$preventLockedCrafting(
            PlayerEntity player,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!((Object) this instanceof CraftingResultSlot slot)) {
            return;
        }

        if (!(player instanceof ServerPlayerEntity serverPlayer)) {
            return;
        }

        ItemStack craftedStack = slot.getStack();

        if (ItemAccessHelper.isLocked(serverPlayer, craftedStack)) {
            serverPlayer.sendMessage(
                    Text.literal("That item is locked"),
                    true
            );

            cir.setReturnValue(false);
        }
    }
}
