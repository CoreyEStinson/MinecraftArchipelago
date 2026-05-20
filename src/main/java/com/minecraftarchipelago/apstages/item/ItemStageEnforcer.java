package com.minecraftarchipelago.apstages.item;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Equipment;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;

public class ItemStageEnforcer {
    public static void register(){
        registerUseItem();
        registerUseBlock();
        registerArmorSweep();
    }

    private static void registerUseItem(){
        registerItemRightClick();
        registerItemLeftClick();
        registerItemAttack();
    }

    private static void registerItemRightClick() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClient()){
                return TypedActionResult.pass(player.getStackInHand(hand));
            }

            if (!(player instanceof ServerPlayerEntity serverPlayer)) {
                return TypedActionResult.pass(player.getStackInHand(hand));
            }

            ItemStack stack = player.getStackInHand(hand);
            if (stack.isEmpty()){
                return TypedActionResult.pass(stack);
            }

            if (ItemAccessHelper.isLocked(serverPlayer, stack)){
                player.sendMessage(Text.literal("That item is locked"), true);
                return TypedActionResult.fail(stack);
            }

            return TypedActionResult.pass(stack);
        });
    }
    private static void registerItemLeftClick() {
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (world.isClient()){
                return TypedActionResult.pass(player.getStackInHand(hand)).getResult();
            }

            if (!(player instanceof ServerPlayerEntity serverPlayer)) {
                return TypedActionResult.pass(player.getStackInHand(hand)).getResult();
            }

            ItemStack stack = player.getStackInHand(hand);
            if (stack.isEmpty()){
                return TypedActionResult.pass(player.getStackInHand(hand)).getResult();
            }

            if (ItemAccessHelper.isLocked(serverPlayer, stack)){
                player.sendMessage(Text.literal("That item is locked"), true);
                return TypedActionResult.fail(stack).getResult();
            }
            return TypedActionResult.pass(stack).getResult();
        });
    }
    private static void registerItemAttack() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient()){
                return TypedActionResult.pass(player.getStackInHand(hand)).getResult();
            }

            if (!(player instanceof ServerPlayerEntity serverPlayer)) {
                return TypedActionResult.pass(player.getStackInHand(hand)).getResult();
            }

            ItemStack stack = player.getStackInHand(hand);
            if (stack.isEmpty()){
                return TypedActionResult.pass(player.getStackInHand(hand)).getResult();
            }

            if (ItemAccessHelper.isLocked(serverPlayer, stack)){
                player.sendMessage(Text.literal("That item is locked"), true);
                return TypedActionResult.fail(stack).getResult();
            }
            return TypedActionResult.pass(stack).getResult();
        });
    }

    private static void registerUseBlock() {
        UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
            if (world.isClient()){
                return  ActionResult.PASS;
            }

            if (!(player instanceof ServerPlayerEntity serverPlayer)) {
                return ActionResult.PASS;
            }

            ItemStack stack = player.getStackInHand(hand);
            if (!stack.isEmpty() && ItemAccessHelper.isLocked(serverPlayer, stack)){
                serverPlayer.sendMessage(Text.literal("That item is locked."), true);
                return ActionResult.FAIL;
            }

            BlockState targetBlock = world.getBlockState(hit.getBlockPos());
            if (ItemAccessHelper.isBlockInteractionLocked(serverPlayer, targetBlock)) {
                serverPlayer.sendMessage(Text.literal("That block is locked."), true);
                return ActionResult.FAIL;
            }

            return ActionResult.PASS;
        });
    }

    private static void registerArmorSweep() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                checkArmorSlot(player, EquipmentSlot.HEAD);
                checkArmorSlot(player, EquipmentSlot.CHEST);
                checkArmorSlot(player, EquipmentSlot.LEGS);
                checkArmorSlot(player, EquipmentSlot.FEET);
            }
        });
    }

    private static void checkArmorSlot(ServerPlayerEntity player, EquipmentSlot slot) {
        ItemStack equipped = player.getEquippedStack(slot);
        if (equipped.isEmpty()) return;
        if (!ItemAccessHelper.isLocked(player, equipped)) return;

        // Clear the slot using the direct reference — no copy
        player.equipStack(slot, ItemStack.EMPTY);

        // Insert explicitly into main inventory to prevent
        // insertStack from routing it back to the armor slot
        boolean inserted = false;
        for (int i = 0; i < player.getInventory().main.size(); i++) {
            if (player.getInventory().main.get(i).isEmpty()) {
                player.getInventory().main.set(i, equipped);
                player.getInventory().markDirty();
                inserted = true;
                break;
            }
        }

        if (!inserted) {
            player.dropItem(equipped, false);
        }

        player.sendMessage(Text.literal("That item is locked"), true);
    }

    private ItemStageEnforcer() {}
}
