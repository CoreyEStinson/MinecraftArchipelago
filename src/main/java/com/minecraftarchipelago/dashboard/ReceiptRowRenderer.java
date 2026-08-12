package com.minecraftarchipelago.dashboard;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public final class ReceiptRowRenderer {
    private static final int ROW_HEIGHT = 28;

    public static void render(
            DrawContext context,
            TextRenderer textRenderer,
            ReceiptRecord receipt,
            int x,
            int y,
            int width,
            int nameWidth
    ) {
        context.fill(x, y, x + width, y + ROW_HEIGHT - 2, DashboardTab.COLOR_PANEL);
        context.fill(x + 4, y + 4, x + 24, y + 24, DashboardTab.COLOR_OUTER_DARK);
        context.fill(x + 5, y + 5, x + 23, y + 23, DashboardTab.COLOR_CONTENT_BACKGROUND);
        context.drawItem(resolveIcon(receipt.iconItemId()), x + 6, y + 6);

        context.drawTextWithShadow(textRenderer, trimToWidth(textRenderer, receipt.displayName(), nameWidth), x + 30, y + 5, DashboardTab.COLOR_TEXT);
        String sender = receipt.sender().isBlank() ? "From Archipelago" : "From " + receipt.sender();
        context.drawTextWithShadow(textRenderer, sender, x + 30, y + 16, DashboardTab.COLOR_DIM_TEXT);
    }

    public static String trimToWidth(TextRenderer textRenderer, String text, int maxWidth) {
        return textRenderer.getWidth(text) <= maxWidth ? text : textRenderer.trimToWidth(text, maxWidth - 9) + "...";
    }

    private static ItemStack resolveIcon(String itemId) {
        String resolvedItemId = switch (itemId) {
            case "minecraft:boat" -> "minecraft:oak_boat";
            case "minecraft:turtle_shell" -> "minecraft:turtle_helmet";
            case "minecraft:bed" -> "minecraft:red_bed";
            default -> itemId;
        };
        Identifier id = Identifier.tryParse(resolvedItemId);
        return id == null || !Registries.ITEM.containsId(id)
                ? new ItemStack(Items.TRIPWIRE_HOOK)
                : new ItemStack(Registries.ITEM.get(id));
    }

    private ReceiptRowRenderer() {
    }
}
