package com.minecraftarchipelago.dashboard;

import com.minecraftarchipelago.APSession;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.server.MinecraftServer;

import java.util.List;

public abstract class ReceiptHistoryTab extends ScrollableDashboardTab {
    private static final int ROW_HEIGHT = 28;

    private String importedSlot = "";

    protected abstract ReceiptKind receiptKind();

    protected abstract String title();

    protected abstract String unavailableMessage();

    protected abstract String emptyMessage();

    protected abstract String totalText(List<ReceiptRecord> receipts);

    @Override
    public final void render(
            DrawContext context,
            TextRenderer textRenderer,
            int x,
            int y,
            int width,
            int height,
            int mouseX,
            int mouseY,
            float delta
    ) {
        MinecraftServer server = MinecraftClient.getInstance().getServer();
        if (server == null) {
            context.drawTextWithShadow(textRenderer, unavailableMessage(), x, y, COLOR_DIM_TEXT);
            resetScroll();
            return;
        }

        String slotName = APSession.slotName == null ? "" : APSession.slotName;
        if (!slotName.equals(importedSlot)) {
            ReceiptHistoryRecorder.importCurrentSession(server);
            importedSlot = slotName;
        }

        List<ReceiptRecord> receipts = ReceiptHistoryState.get(server).getNewestFirst(receiptKind(), slotName);
        context.drawTextWithShadow(textRenderer, title(), x, y, COLOR_TITLE);

        String total = totalText(receipts);
        context.drawTextWithShadow(textRenderer, total, x + width - textRenderer.getWidth(total), y, COLOR_DIM_TEXT);

        setViewport(x, y + 18, width, height - 18);
        if (receipts.isEmpty()) {
            context.drawTextWithShadow(textRenderer, emptyMessage(), x, contentTop() + 8, COLOR_DIM_TEXT);
            resetScroll();
            return;
        }

        beginScrollableContent(context);
        int rowY = contentTop();
        for (ReceiptRecord receipt : receipts) {
            renderReceiptRow(context, textRenderer, receipt, x, rowY, width);
            rowY += ROW_HEIGHT;
        }
        finishScrollableContent(context, rowY - 2);
    }

    protected void renderReceiptRow(
            DrawContext context,
            TextRenderer textRenderer,
            ReceiptRecord receipt,
            int x,
            int y,
            int width
    ) {
        ReceiptRowRenderer.render(
                context,
                textRenderer,
                receipt,
                x,
                y,
                width,
                receiptNameWidth(textRenderer, receipt, x, width)
        );
    }

    protected int receiptNameWidth(TextRenderer textRenderer, ReceiptRecord receipt, int x, int width) {
        return width - 44;
    }
}
