package com.minecraftarchipelago.dashboard.tabs;

import com.minecraftarchipelago.dashboard.ReceiptHistoryTab;
import com.minecraftarchipelago.dashboard.ReceiptKind;
import com.minecraftarchipelago.dashboard.ReceiptRecord;
import net.minecraft.client.font.TextRenderer;

import java.util.List;

public class ItemsTab extends ReceiptHistoryTab {
    @Override
    protected ReceiptKind receiptKind() {
        return ReceiptKind.GIFT;
    }

    @Override
    protected String title() {
        return "Items";
    }

    @Override
    protected String unavailableMessage() {
        return "Load a singleplayer world to view received items.";
    }

    @Override
    protected String emptyMessage() {
        return "No gift items have been received for this slot";
    }

    @Override
    protected String totalText(List<ReceiptRecord> receipts) {
        return receipts.size() + " received";
    }

    @Override
    protected int receiptNameWidth(TextRenderer textRenderer, ReceiptRecord receipt, int x, int width) {
        String quantity = "x" + receipt.quantity();
        int quantityX = x + width - 8 - textRenderer.getWidth(quantity);
        return quantityX - (x + 30) - 8;
    }

    @Override
    protected void renderReceiptRow(
            net.minecraft.client.gui.DrawContext context,
            TextRenderer textRenderer,
            ReceiptRecord receipt,
            int x,
            int y,
            int width
    ) {
        super.renderReceiptRow(context, textRenderer, receipt, x, y, width);
        String quantity = "x" + receipt.quantity();
        context.drawTextWithShadow(textRenderer, quantity, x + width - 8 - textRenderer.getWidth(quantity), y + 5, COLOR_COMPLETE);
    }
}
