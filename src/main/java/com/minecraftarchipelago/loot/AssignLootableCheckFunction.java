package com.minecraftarchipelago.loot;

import com.minecraftarchipelago.APSession;
import com.minecraftarchipelago.aplocations.LootableCheckState;
import com.minecraftarchipelago.item.ArchipelagoCheckItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;

import java.util.List;

public class AssignLootableCheckFunction extends ConditionalLootFunction {

    public static final MapCodec<AssignLootableCheckFunction> CODEC =
            MapCodec.unit(new AssignLootableCheckFunction(List.of(new LootCondition[0])));

    public static final LootFunctionType<AssignLootableCheckFunction> TYPE =
            new LootFunctionType<>(CODEC);

    private AssignLootableCheckFunction(List<LootCondition> conditions) {
        super(conditions);
    }

    @Override
    public LootFunctionType<AssignLootableCheckFunction> getType() {
        return TYPE;
    }

    // ── Core logic ────────────────────────────────────────────────────────────
    // Called once per ItemStack at loot generation time (when the chest is
    // first opened). If connected, assigns a location ID immediately.
    // If offline or pool exhausted, the inventoryTick fallback handles it later.

    @Override
    protected ItemStack process(ItemStack stack, LootContext context) {
        MinecraftServer server = context.getWorld().getServer();
        return assignLootableCheck(stack, server);
    }

    public static ItemStack assignLootableCheck(ItemStack stack, MinecraftServer server) {
        if (!(stack.getItem() instanceof ArchipelagoCheckItem)) return stack;

        NbtCompound nbt = ArchipelagoCheckItem.getCustomData(stack);
        if (nbt.getBoolean(ArchipelagoCheckItem.NBT_ASSIGNED)) return stack;
        if (nbt.getBoolean(ArchipelagoCheckItem.NBT_SURPLUS))  return stack;

        // If not connected yet, inventoryTick will assign when slot data arrives
        if (!APSession.hasSlotData()) return stack;

        int poolSize = APSession.getSlotData().getLootableChecks();
        if (poolSize == 0) {
            nbt.putBoolean(ArchipelagoCheckItem.NBT_SURPLUS, true);
            ArchipelagoCheckItem.setCustomData(stack, nbt);
            return stack;
        }
        if (server == null) return stack;

        long locationId = LootableCheckState.get(server).assignNext(poolSize);

        if (locationId < 0) {
            nbt.putBoolean(ArchipelagoCheckItem.NBT_SURPLUS, true);
        } else {
            nbt.putBoolean(ArchipelagoCheckItem.NBT_ASSIGNED, true);
            nbt.putLong(ArchipelagoCheckItem.NBT_LOCATION_ID, locationId);

            // Try to apply a cached name immediately if scout already arrived
            ArchipelagoCheckItem.setCustomData(stack, nbt);
            ChestOpenHandler.maybeApplyCachedName(
                    stack, ArchipelagoCheckItem.getCustomData(stack));

            // Request scout for item name + recipient
            ChestOpenHandler.requestScout(locationId);
        }

        return stack;
    }

    // ── Builder ───────────────────────────────────────────────────────────────

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends ConditionalLootFunction.Builder<Builder> {
        @Override
        protected Builder getThisBuilder() { return this; }

        @Override
        public LootFunction build() {
            return new AssignLootableCheckFunction(getConditions());
        }
    }
}
