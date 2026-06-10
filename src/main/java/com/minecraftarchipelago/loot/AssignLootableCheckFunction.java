package com.minecraftarchipelago.loot;

import com.mojang.serialization.MapCodec;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
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

    @Override
    protected ItemStack process(ItemStack stack, LootContext context) {
        return stack;
    }

    public static ItemStack assignLootableCheck(ItemStack stack, MinecraftServer server) {
        return stack;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends ConditionalLootFunction.Builder<Builder> {
        @Override
        protected Builder getThisBuilder() {
            return this;
        }

        @Override
        public LootFunction build() {
            return new AssignLootableCheckFunction(getConditions());
        }
    }
}
