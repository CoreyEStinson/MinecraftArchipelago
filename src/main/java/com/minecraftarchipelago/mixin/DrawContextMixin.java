package com.minecraftarchipelago.mixin;

import com.minecraftarchipelago.client.ClientLockedItems;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DrawContext.class)
public abstract class DrawContextMixin {

    @Unique
    private static final int LOCKED_SLOT_TINT = 0x77000000;
    @Unique
    private static final int LOCKED_X_COLOR = 0xAAFF3333;

    @Unique
    private static final int OVERLAY_Z = 175;

    @Inject(
            method = "drawItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;IIII)V",
            at = @At("RETURN")
    )
    private void minecraftarchipelago$drawLockedItemOverlay(
            @Nullable LivingEntity entity,
            @Nullable World world,
            ItemStack stack,
            int x,
            int y,
            int seed,
            int z,
            CallbackInfo ci
    ) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.currentScreen != null
                && !(client.currentScreen instanceof HandledScreen<?>)) {
            return;
        }

        if (!ClientLockedItems.isLocked(stack)) {
            return;
        }

        DrawContext context = (DrawContext) (Object) this;

        // Tints the entire 16x16 icon area.
        // context.fill(x, y, x + 16, y + 16, LOCKED_SLOT_TINT);

        // Two pixel wide red X spanning the entire icon.
        for (int offset = 0; offset < 16; offset++) {
            context.fill(
                    x + offset,
                    y + offset,
                    x + offset + 1,
                    y + offset + 2,
                    OVERLAY_Z,
                    LOCKED_X_COLOR
            );

            context.fill(
                    x + 15 - offset,
                    y + offset,
                    x + 16 - offset,
                    y + offset + 2,
                    OVERLAY_Z,
                    LOCKED_X_COLOR
            );

        }
    }
}
