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

    // NBT keys
    public static final String NBT_ASSIGNED = "ap_assigned";
    public static final String NBT_LOCATION_ID = "ap_location_id";
    public static final String NBT_CLAIMED = "ap_claimed";
    public static final String NBT_SURPLUS = "ap_surplus";
    public static final String NBT_AP_ITEM_NAME   = "ap_item_display_name";
    public static final String NBT_AP_PLAYER_NAME = "ap_player_display_name";

    public ArchipelagoCheckItem(Settings settings) {
        super(settings);
    }

    // Name
    @Override
    public Text getName(ItemStack stack) {
        NbtCompound nbt = getCustomData(stack);

        if (nbt.getBoolean(NBT_SURPLUS)) {
            return Text.literal("✦ Archipelago Loot ✦")
                    .formatted(Formatting.DARK_GRAY, Formatting.STRIKETHROUGH);
        }

        if (nbt.getBoolean(NBT_ASSIGNED)) {
            // Scout result has arrived — show the item and recipient
            if (nbt.contains(NBT_AP_ITEM_NAME)) {
                return Text.empty()
                        .append(Text.literal("⚡ ").formatted(Formatting.YELLOW))
                        .append(Text.literal(nbt.getString(NBT_AP_ITEM_NAME))
                                .formatted(Formatting.WHITE))
                        .append(Text.literal(" → ").formatted(Formatting.GRAY))
                        .append(Text.literal(nbt.getString(NBT_AP_PLAYER_NAME))
                                .formatted(Formatting.AQUA));
            }
            // Scout pending — show check number
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

            tooltip.add(Text.literal(""));
            tooltip.add(Text.empty()
                    .append(Text.literal("Right-click").formatted(Formatting.YELLOW))
                    .append(Text.literal(" to send and reveal.").formatted(Formatting.GRAY)));
            return;
        }

        // Unassigned — sitting in a chest or just picked up before connecting
        tooltip.add(Text.literal("A location waiting to be claimed...")
                .formatted(Formatting.GRAY, Formatting.ITALIC));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.empty()
                .append(Text.literal("Right-click").formatted(Formatting.YELLOW))
                .append(Text.literal(" to reveal and claim.")
                        .formatted(Formatting.GRAY)));
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity,
                              int slot, boolean selected) {
        if (world.isClient()) return;
        if (!(entity instanceof ServerPlayerEntity player)) return;

        NbtCompound nbt = getCustomData(stack);

        // If assigned but no name yet — check if cache has arrived
        if (nbt.getBoolean(NBT_ASSIGNED) && !nbt.contains(NBT_AP_ITEM_NAME)) {
            ChestOpenHandler.maybeApplyCachedName(stack, nbt);
            return;
        }

        // Already fully processed
        if (nbt.getBoolean(NBT_ASSIGNED) || nbt.getBoolean(NBT_SURPLUS)) return;

        // Fallback assignment - for items picked up while offline or before
        // ChestOpenHandler had slot data (chest was opened before AP connection)
        if (!APSession.hasSlotData()) return;

        int poolSize = APSession.getSlotData().getLootableChecks();
        if (poolSize == 0) {
            nbt.putBoolean(NBT_SURPLUS, true);
            setCustomData(stack, nbt);
            return;
        }

        MinecraftServer server = player.getServer();
        if (server == null) return;

        long locationId = LootableCheckState.get(server).assignNext(poolSize);

        if (locationId < 0) {
            nbt.putBoolean(NBT_SURPLUS, true);
            setCustomData(stack, nbt);
            return;
        }

        int n = (int)(locationId - SlotData.LOOTABLE_CHECK_BASE_ID) + 1;
        nbt.putBoolean(NBT_ASSIGNED, true);
        nbt.putLong(NBT_LOCATION_ID, locationId);
        setCustomData(stack, nbt);

        // Try cache immediately and request scout
        ChestOpenHandler.maybeApplyCachedName(stack, getCustomData(stack));
        ChestOpenHandler.requestScout(locationId);

        player.sendMessage(
                Text.empty()
                        .append(Text.literal("⚡ ").formatted(Formatting.YELLOW))
                        .append(Text.literal("Archipelago Loot #" + n)
                                .formatted(Formatting.GOLD, Formatting.BOLD))
                        .append(Text.literal(" found! Right-click to claim.")
                                .formatted(Formatting.YELLOW)),
                true
        );
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (world.isClient()) return TypedActionResult.success(stack);

        if (!(user instanceof ServerPlayerEntity player))
            return TypedActionResult.pass(stack);

        NbtCompound nbt = getCustomData(stack);

        // Surplus
        if (nbt.getBoolean(NBT_SURPLUS)) {
            player.sendMessage(
                    Text.literal("All lootable checks have already been found.")
                            .formatted(Formatting.DARK_GRAY),
                    true
            );
            stack.decrement(1);
            return TypedActionResult.consume(stack);
        }

        // Not yet assigned
        if (!nbt.getBoolean(NBT_ASSIGNED)) {
            player.sendMessage(
                    Text.empty()
                            .append(Text.literal("⚡ ").formatted(Formatting.YELLOW))
                            .append(Text.literal("Connect to Archipelago ")
                                    .formatted(Formatting.WHITE))
                            .append(Text.literal("first to claim this item.")
                                    .formatted(Formatting.GRAY)),
                    true
            );
            return TypedActionResult.pass(stack);
        }

        // Claim
        // ── Claim ─────────────────────────────────────────────────────────────
        long locationId = nbt.getLong(NBT_LOCATION_ID);
        int n = checkNumber(nbt);

        MinecraftServer server = player.getServer();
        if (server == null) return TypedActionResult.fail(stack);

        CheckedLocationsState checkedState = CheckedLocationsState.get(server);
        boolean wasNew = checkedState.checkLocation(locationId);

        if (wasNew) {
            // Dispatch to client thread for the AP call
            APSession.runtime().executeOnClient(() -> {
                if (APSession.client().isConnected()) {
                    APSession.client().checkLocation(locationId);
                }
                // If offline: already stored in CheckedLocationsState and will be
                // resent by APEvents.onConnected when the player reconnects.
            });
            VictoryCondition.checkAndAward(server);
        }

        // Sound and chat confirmations
        player.playSoundToPlayer(
                SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0f, 1.0f
        );

        player.sendMessage(
                Text.empty()
                        .append(Text.literal("⚡ ").formatted(Formatting.YELLOW))
                        .append(Text.literal("Lootable Check #" + n)
                                .formatted(Formatting.GOLD, Formatting.BOLD))
                        .append(Text.literal(" sent to Archipelago!")
                                .formatted(Formatting.YELLOW)),
                false
        );

        // Consume the item
        stack.decrement(1);
        return TypedActionResult.consume(stack);
    }

    private static int checkNumber(NbtCompound nbt) {
        return (int)(nbt.getLong(NBT_LOCATION_ID) - SlotData.LOOTABLE_CHECK_BASE_ID) + 1;
    }

    // Returns the item's custom NBT data, or an empty compound if none exists.
    public static NbtCompound getCustomData(ItemStack stack) {
        NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
        return component != null ? component.copyNbt() : new NbtCompound();
    }

    // Writes a modified NBT compound back to the item's custom data component.
    public static void setCustomData(ItemStack stack, NbtCompound nbt) {
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    }

}
