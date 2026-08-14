package com.minecraftarchipelago.dashboard.tabs;

import com.minecraftarchipelago.APSession;
import com.minecraftarchipelago.dashboard.DashboardPreferences;
import com.minecraftarchipelago.dashboard.DashboardTab;
import com.minecraftarchipelago.dashboard.ScrollableDashboardTab;
import com.minecraftarchipelago.dashboard.ReceiptHistoryRecorder;
import com.minecraftarchipelago.dashboard.ReceiptHistoryState;
import com.minecraftarchipelago.dashboard.ReceiptKind;
import com.minecraftarchipelago.dashboard.ReceiptRecord;
import com.minecraftarchipelago.dashboard.ReceiptRowRenderer;
import com.minecraftarchipelago.dashboard.DashboardProgressState;
import com.minecraftarchipelago.victory.VictoryProgress;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.server.MinecraftServer;

import java.util.List;

public class OverviewTab extends ScrollableDashboardTab {
    private static final int RECEIPT_ROW_HEIGHT = 28;

    private String importedSlot = "";

    @Override
    public void render(
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
        setViewport(x, y, width, height);
        beginScrollableContent(context);
        y = contentTop();

        String slotName = APSession.slotName == null || APSession.slotName.isBlank()
                ? "-"
                : APSession.slotName;

        String address = APSession.client().getConnectedAddress();
        if (address == null || address.isBlank()) {
            address = "No active Archipelago connection";
        }

        context.drawTextWithShadow(
                textRenderer,
                "Connected",
                x,
                y,
                COLOR_CONNECTED
        );

        context.drawTextWithShadow(
                textRenderer,
                "Slot: " + slotName + "  |  " + address,
                x + 58,
                y,
                COLOR_DIM_TEXT
        );

        y += 15;

        boolean deathLinkAvailable = APSession.hasSlotData()
                && APSession.getSlotData().isDeathLinkEnabled();

        if (deathLinkAvailable
                && DashboardPreferences.get().showDeathLinkStatusStrip) {
            context.fill(x, y, x + width, y + 22, COLOR_DEATHLINK_ENABLED);
            context.fill(x, y, x + width, y + 1, COLOR_OUTER_LIGHT);

            context.drawTextWithShadow(
                    textRenderer,
                    "DEATHLINK ENABLED",
                    x + 7,
                    y + 4,
                    COLOR_TEXT
            );

            context.drawTextWithShadow(
                    textRenderer,
                    "Deaths are shared with the multiworld",
                    x + 7,
                    y + 13,
                    COLOR_DIM_TEXT
            );

            y += 26;
        }

        context.drawTextWithShadow(
                textRenderer,
                "Overall Checks",
                x,
                y,
                COLOR_TEXT
        );

        String totalText = DashboardProgressState.locationsChecked + " / " + DashboardProgressState.locationsTotal;

        context.drawTextWithShadow(
                textRenderer,
                totalText,
                x + width - textRenderer.getWidth(totalText),
                y,
                COLOR_DIM_TEXT
        );

        drawProgressBar(
                context,
                x,
                y + 11,
                width,
                DashboardProgressState.locationsChecked,
                DashboardProgressState.locationsTotal
        );

        y += 24;

        context.drawTextWithShadow(
                textRenderer,
                "Required Victory Progress",
                x,
                y,
                COLOR_TITLE
        );

        y += 13;

        if (DashboardProgressState.activeConditions.isEmpty()) {
            context.drawTextWithShadow(
                    textRenderer,
                    "Waiting for Archipelago slot data...",
                    x,
                    y,
                    COLOR_DIM_TEXT
            );

            y += 13;
        } else {
            for (VictoryProgress condition : DashboardProgressState.activeConditions) {
                drawVictoryProgressRow(
                        context,
                        textRenderer,
                        condition,
                        x,
                        y,
                        width,
                        false
                );

                y += 13;
            }
        }

        y += 2;

        context.drawTextWithShadow(
                textRenderer,
                "Recent Gifts & Unlocks",
                x,
                y,
                COLOR_TITLE
        );

        y += 14;

        MinecraftServer server = MinecraftClient.getInstance().getServer();

        if (server == null) {
            context.drawTextWithShadow(
                    textRenderer,
                    "Load a singleplayer world to view receipt history.",
                    x,
                    y,
                    COLOR_DIM_TEXT
            );
            finishScrollableContent(context, y + 14);
            return;
        }

        String receiptSlotName = APSession.slotName == null ? "" : APSession.slotName;

        if (!receiptSlotName.equals(importedSlot)) {
            ReceiptHistoryRecorder.importCurrentSession(server);
            importedSlot = receiptSlotName;
        }

        List<ReceiptRecord> receipts = ReceiptHistoryState.get(server)
                .getRecentNewestFirst(
                        receiptSlotName,
                        DashboardPreferences.get().recentActivityCount
                );

        if (receipts.isEmpty()) {
            context.drawTextWithShadow(
                    textRenderer,
                    "No gifts or unlocks received for this slot.",
                    x,
                    y,
                    COLOR_DIM_TEXT
            );
            finishScrollableContent(context, y + 14);
            return;
        }

        for (ReceiptRecord receipt : receipts) {
            ReceiptRowRenderer.render(context, textRenderer, receipt, x, y, width, width - 44);

            y += RECEIPT_ROW_HEIGHT;
        }

        finishScrollableContent(context, y);
    }

}
