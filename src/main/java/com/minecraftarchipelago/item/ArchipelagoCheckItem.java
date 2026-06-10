package com.minecraftarchipelago.item;

import com.minecraftarchipelago.APSession;
import com.minecraftarchipelago.SlotData;
import com.minecraftarchipelago.aplocations.CheckedLocationsState;
import com.minecraftarchipelago.aplocations.LootableCheckState;
import com.minecraftarchipelago.aplocations.VictoryCondition;
import com.minecraftarchipelago.loot.ChestOpenHandler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

public class ArchipelagoCheckItem extends Item {

    public static final String NBT_ASSIGNED = "ap_assigned";
    public static final String NBT_LOCATION_ID = "ap_location_id";
    public static final String NBT_CLAIMED = "ap_claimed";
    public static final String NBT_SURPLUS = "ap_surplus";
    public static final String NBT_AP_ITEM_NAME = "ap_item_display_name";
    public static final String NBT_AP_PLAYER_NAME = "ap_player_display_name";
    public static final String NBT_LOOT_SOURCE = "ap_loot_source";
    public static final String NBT_LOOT_SOURCE_NAME = "ap_loot_source_name";

    public ArchipelagoCheckItem(Settings settings) {
        super(settings);
    }

    @Override
    public Text getName(ItemStack stack) {
        NbtCompound nbt = getCustomData(stack);

        if (nbt.getBoolean(NBT_SURPLUS)) {
            return Text.literal("âœ¦ Archipelago Loot âœ¦")
                    .formatted(Formatting.DARK_GRAY, Formatting.STRIKETHROUGH);
        }

        if (nbt.getBoolean(NBT_ASSIGNED)) {
            if (nbt.contains(NBT_AP_ITEM_NAME)) {
                return Text.empty()
                        .append(Text.literal("⚡ ").formatted(Formatting.YELLOW))
                        .append(Text.literal(nbt.getString(NBT_AP_ITEM_NAME))
                                .formatted(Formatting.WHITE))
                        .append(Text.literal(" → ").formatted(Formatting.GRAY))
                        .append(Text.literal(nbt.getString(NBT_AP_PLAYER_NAME))
                                .formatted(Formatting.AQUA));
            }

            return Text.empty()
                    .append(Text.literal("✦ ").formatted(Formatting.GOLD))
                    .append(Text.literal("Archipelago Loot")
                            .formatted(Formatting.GOLD, Formatting.BOLD))
                    .append(Text.literal(" #" + checkNumber(nbt))
                            .formatted(Formatting.YELLOW))
                    .append(Text.literal(" ✦").formatted(Formatting.GOLD));
        }

        return Text.literal("✦ Archipelago Loot ✦")
                .formatted(Formatting.GOLD, Formatting.BOLD);
    }

    // Enchantment glint
    @Override
    public boolean hasGlint(ItemStack stack) {
        NbtCompound nbt = getCustomData(stack);
        // No glint once claimed or surplus
        return !nbt.getBoolean(NBT_CLAIMED) && !nbt.getBoolean(NBT_SURPLUS);
    }

    // Tooltip
    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context,
                              List<Text> tooltip, TooltipType type) {
        NbtCompound nbt = getCustomData(stack);

        if (nbt.getBoolean(NBT_SURPLUS)) {
            tooltip.add(Text.literal("All lootable checks have been found.")
                    .formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
            appendSourceTooltip(nbt, tooltip);
            return;
        }

        if (nbt.getBoolean(NBT_ASSIGNED)) {
            tooltip.add(Text.literal("Lootable Check #" + checkNumber(nbt))
                    .formatted(Formatting.YELLOW));
            tooltip.add(Text.literal(""));

            if (nbt.contains(NBT_AP_PLAYER_NAME)) {
                tooltip.add(Text.literal("For: ")
                        .formatted(Formatting.GRAY)
                        .append(Text.literal(nbt.getString(NBT_AP_PLAYER_NAME))
                                .formatted(Formatting.AQUA)));
            } else {
                tooltip.add(Text.literal("Scouting destination...")
                        .formatted(Formatting.GRAY, Formatting.ITALIC));
            }

            appendSourceTooltip(nbt, tooltip);
            tooltip.add(Text.literal(""));
            tooltip.add(Text.empty()
                    .append(Text.literal("Right-click").formatted(Formatting.YELLOW))
                    .append(Text.literal(" to send and reveal.").formatted(Formatting.GRAY)));
            return;
        }

        tooltip.add(Text.literal("A location waiting to be claimed...")
                .formatted(Formatting.GRAY, Formatting.ITALIC));
        appendSourceTooltip(nbt, tooltip);
        tooltip.add(Text.literal(""));
        tooltip.add(Text.empty()
                .append(Text.literal("Right-click").formatted(Formatting.YELLOW))
                .append(Text.literal(" to reveal and claim.").formatted(Formatting.GRAY)));
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity,
                              int slot, boolean selected) {
        if (world.isClient()) return;
        if (!(entity instanceof ServerPlayerEntity)) return;

        NbtCompound nbt = getCustomData(stack);
        if (nbt.getBoolean(NBT_ASSIGNED) && !nbt.contains(NBT_AP_ITEM_NAME)) {
            ChestOpenHandler.maybeApplyCachedName(stack, nbt);
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (world.isClient()) return TypedActionResult.success(stack);
        if (!(user instanceof ServerPlayerEntity player)) return TypedActionResult.pass(stack);

        NbtCompound nbt = getCustomData(stack);

        if (nbt.getBoolean(NBT_SURPLUS)) {
            player.sendMessage(
                    Text.literal("All lootable checks have already been found.")
                            .formatted(Formatting.DARK_GRAY),
                    true
            );
            stack.decrement(1);
            return TypedActionResult.consume(stack);
        }

        if (!nbt.getBoolean(NBT_ASSIGNED)) {
            if (!APSession.hasSlotData()) {
                player.sendMessage(
                        Text.empty()
                                .append(Text.literal("⚡ ").formatted(Formatting.YELLOW))
                                .append(Text.literal("Connect to Archipelago ").formatted(Formatting.WHITE))
                                .append(Text.literal("first to claim this item.").formatted(Formatting.GRAY)),
                        true
                );
                return TypedActionResult.pass(stack);
            }

            MinecraftServer server = player.getServer();
            if (server == null) return TypedActionResult.fail(stack);

            int poolSize = APSession.getSlotData().getLootableChecks();
            if (poolSize == 0) {
                nbt.putBoolean(NBT_SURPLUS, true);
                setCustomData(stack, nbt);
            } else {
                long locationId = LootableCheckState.get(server).assignNext(poolSize);
                if (locationId < 0) {
                    nbt.putBoolean(NBT_SURPLUS, true);
                    setCustomData(stack, nbt);
                } else {
                    nbt.putBoolean(NBT_ASSIGNED, true);
                    nbt.putLong(NBT_LOCATION_ID, locationId);
                    setCustomData(stack, nbt);
                    nbt = getCustomData(stack);
                }
            }

            if (nbt.getBoolean(NBT_SURPLUS)) {
                player.sendMessage(
                        Text.literal("All lootable checks have already been found.")
                                .formatted(Formatting.DARK_GRAY),
                        true
                );
                stack.decrement(1);
                return TypedActionResult.consume(stack);
            }
        }

        long locationId = nbt.getLong(NBT_LOCATION_ID);
        int n = checkNumber(nbt);

        MinecraftServer server = player.getServer();
        if (server == null) return TypedActionResult.fail(stack);

        CheckedLocationsState checkedState = CheckedLocationsState.get(server);
        boolean wasNew = checkedState.checkLocation(locationId);

        if (wasNew) {
            APSession.runtime().executeOnClient(() -> {
                if (APSession.client().isConnected()) {
                    APSession.client().checkLocation(locationId);
                }
            });
            VictoryCondition.checkAndAward(server);
        }

        player.playSoundToPlayer(
                SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0f, 1.0f
        );

        player.sendMessage(
                Text.empty()
                        .append(Text.literal("⚡ ").formatted(Formatting.YELLOW))
                        .append(Text.literal("Lootable Check #" + n).formatted(Formatting.GOLD, Formatting.BOLD))
                        .append(Text.literal(" sent to Archipelago!").formatted(Formatting.YELLOW)),
                false
        );

        stack.decrement(1);
        return TypedActionResult.consume(stack);
    }

    private static int checkNumber(NbtCompound nbt) {
        return (int) (nbt.getLong(NBT_LOCATION_ID) - SlotData.LOOTABLE_CHECK_BASE_ID) + 1;
    }

    private static void appendSourceTooltip(NbtCompound nbt, List<Text> tooltip) {
        if (!nbt.contains(NBT_LOOT_SOURCE_NAME)) return;

        tooltip.add(Text.empty()
                .append(Text.literal("Found from: ").formatted(Formatting.GRAY))
                .append(Text.literal(nbt.getString(NBT_LOOT_SOURCE_NAME)).formatted(Formatting.YELLOW)));
    }

    public static NbtCompound getCustomData(ItemStack stack) {
        NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
        return component != null ? component.copyNbt() : new NbtCompound();
    }

    public static void setCustomData(ItemStack stack, NbtCompound nbt) {
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    }
}
