package com.minecraftarchipelago.item;

import net.minecraft.client.gui.tooltip.TooltipState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class ArchipelagoCheckItem extends Item {

    // Keys stored inside CUSTOM_DATA
    public static final String KEY_LOCATION_ID = "ap_location_id";
    public static final String KEY_CHECK_INDEX = "ap_check_index";
    public static final String KEY_ITEM_NAME = "ap_item_name";
    public static final String KEY_PLAYER_NAME = "ap_player_name";

    public ArchipelagoCheckItem(Settings settings) {
        super(settings);
    }

    @Override
    public Text getName(ItemStack stack) {
        NbtCompound data = getCheckData(stack);

        // If data has been written (chest was opened and scouted)
        // show what the check contains and who it is for.
        // Display this regardless of current connection state
        // Once scouted the name is stored on the item
        if (data != null
                && data.contains(KEY_ITEM_NAME)
                && !data.getString(KEY_ITEM_NAME).isEmpty()) {

            String itemName = data.getString(KEY_ITEM_NAME);
            String playerName = data.getString(KEY_PLAYER_NAME);

            return Text.empty()
                    .append(Text.literal(itemName).formatted(Formatting.AQUA)
                    .append(Text.literal(" (" + playerName + ")").formatted(Formatting.GRAY)));
        }

        // No data yet, chest not opened or not connected to AP when opened
        return Text.literal("★ AP Token").formatted(Formatting.GOLD);
    }

    // Tooltip
    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        NbtCompound data = getCheckData(stack);

        if (data != null && data.contains(KEY_CHECK_INDEX)) {
            tooltip.add(
                    Text.literal("Loot Check #" + data.getInt(KEY_CHECK_INDEX))
                            .formatted(Formatting.DARK_AQUA)
            );
        }

        tooltip.add(
                Text.literal("Pick up to send an Archipelago check")
                        .formatted(Formatting.DARK_GRAY, Formatting.ITALIC)
        );
    }

    // Helpers
    public static NbtCompound getCheckData(ItemStack stack) {
        NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
        return component != null ? component.copyNbt() : null;
    }

    /**
     * Writes all check fields to the item stack.
     * Called when a chest is first opened and the location is scouted.
     *
     * @param locationId  AP location ID (e.g. 42213)
     * @param checkIndex  1-based display number (e.g. 3 → "Loot Check #3")
     * @param apItemName  Name of the AP item waiting at this location
     * @param apPlayerName Slot name of the player who receives it
     */
    public static void setCheckData(ItemStack stack,
                                    long locationId, int checkIndex,
                                    String apItemName, String apPlayerName) {
        NbtCompound data = new NbtCompound();
        data.putLong(KEY_LOCATION_ID, locationId);
        data.putInt(KEY_CHECK_INDEX, checkIndex);
        data.putString(KEY_ITEM_NAME, apItemName);
        data.putString(KEY_PLAYER_NAME, apPlayerName);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(data));
    }

    /**
     * Writes only the location ID and check index, leaving item/player name blank.
     * Used when a chest is opened while not connected to AP
     * the name is filled in when scouting succeeds later.
     */
    public static void setCheckDataPartial(ItemStack stack,
                                           long locationId, int checkIndex) {
        setCheckData(stack, locationId, checkIndex, "", "");
    }

    /** Returns the location ID stored on this token, or -1L if not set. */
    public static long getLocationId(ItemStack stack) {
        NbtCompound data = getCheckData(stack);
        return (data != null && data.contains(KEY_LOCATION_ID))
                ? data.getLong(KEY_LOCATION_ID)
                : -1L;
    }

    /** Returns true if this token has been assigned a location (chest was opened). */
    public static boolean isAssigned(ItemStack stack) {
        return getLocationId(stack) != -1L;
    }

    /** Returns true if this token has a fully scouted name (ready to display). */
    public static boolean isScouted(ItemStack stack) {
        NbtCompound data = getCheckData(stack);
        return data != null
                && data.contains(KEY_ITEM_NAME)
                && !data.getString(KEY_ITEM_NAME).isEmpty();
    }

}
