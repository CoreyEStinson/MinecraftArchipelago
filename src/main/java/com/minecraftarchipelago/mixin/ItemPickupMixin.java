package com.minecraftarchipelago.mixin;

import com.minecraftarchipelago.APSession;
import com.minecraftarchipelago.aplocations.CheckedLocationsState;
import com.minecraftarchipelago.aplocations.VictoryCondition;
import com.minecraftarchipelago.item.ArchipelagoCheckItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public class ItemPickupMixin {

    @Inject(method = "onPlayerCollision", at = @At("HEAD"))
    private void ap$onCheckPickup(PlayerEntity player, CallbackInfo ci) {
        if (player.getWorld().isClient()) return;
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return;

        ItemEntity self = (ItemEntity) (Object)this;
        ItemStack stack = self.getStack();

        if (!(stack.getItem() instanceof ArchipelagoCheckItem)) return;

        long locationId = ArchipelagoCheckItem.getLocationId(stack);
        if (locationId == -1L) return;

        MinecraftServer server = serverPlayer.getServer();
        if (server == null) return;

        CheckedLocationsState checkedState = CheckedLocationsState.get(server);
        if (!checkedState.checkLocation(locationId)) return;

        MinecraftClient.getInstance().execute(() ->
                APSession.CLIENT.checkLocation(locationId)
        );

        VictoryCondition.checkAndAward(server);

        int checkIndex = getCheckIndex(stack);
        serverPlayer.playSoundToPlayer(
                SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1, 1
        );
        serverPlayer.sendMessage(
                Text.literal("[AP] Loot Check #" + checkIndex + " found!")
                        .formatted(Formatting.GREEN),
                false
        );
    }

    private static int getCheckIndex(ItemStack stack) {
        NbtCompound data = ArchipelagoCheckItem.getCheckData(stack);
        return (data != null && data.contains(ArchipelagoCheckItem.KEY_CHECK_INDEX))
                ? data.getInt(ArchipelagoCheckItem.KEY_CHECK_INDEX)
                : 0;
    }
}
