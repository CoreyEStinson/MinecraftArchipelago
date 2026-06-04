package com.minecraftarchipelago.loot;

import com.minecraftarchipelago.item.ArchipelagoCheckItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.nbt.NbtCompound;

import java.util.List;

public class SetLootSourceFunction extends ConditionalLootFunction {
    public static final MapCodec<SetLootSourceFunction> CODEC =
            MapCodec.unit(new SetLootSourceFunction(List.of(new LootCondition[0]), "", ""));

    public static final LootFunctionType<SetLootSourceFunction> TYPE = new LootFunctionType<>(CODEC);

    private final String sourceId;
    private final String sourceName;

    private SetLootSourceFunction(List<LootCondition> conditions, String sourceId, String sourceName) {
        super(conditions);
        this.sourceId = sourceId;
        this.sourceName = sourceName;
    }

    @Override
    public LootFunctionType<SetLootSourceFunction> getType() {
        return TYPE;
    }

    @Override
    protected ItemStack process(ItemStack stack, LootContext context) {
        if (stack.getItem() instanceof ArchipelagoCheckItem) {
            NbtCompound nbt = ArchipelagoCheckItem.getCustomData(stack);
            if (!sourceId.isEmpty()) nbt.putString(ArchipelagoCheckItem.NBT_LOOT_SOURCE, sourceId);
            if (!sourceName.isEmpty()) nbt.putString(ArchipelagoCheckItem.NBT_LOOT_SOURCE_NAME, sourceName);
            ArchipelagoCheckItem.setCustomData(stack, nbt);
        }
        return stack;
    }

    public static Builder builder(String sourceId, String sourceName) {
        return new Builder(sourceId, sourceName);
    }

    public static class Builder extends ConditionalLootFunction.Builder<Builder> {
        private final String sourceId;
        private final String sourceName;

        private Builder(String sourceId, String sourceName) {
            this.sourceId = sourceId;
            this.sourceName = sourceName;
        }

        @Override
        protected Builder getThisBuilder() {
            return this;
        }

        @Override
        public LootFunction build() {
            return new SetLootSourceFunction(getConditions(), sourceId, sourceName);
        }
    }
}
