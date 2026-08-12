package com.minecraftarchipelago.dashboard;

import com.minecraftarchipelago.victory.VictoryProgress;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public abstract class DashboardTab {
    public static final int COLOR_SCREEN_DIM = 0xAA000000;
    public static final int COLOR_OUTER_DARK = 0xFF141414;
    public static final int COLOR_OUTER_LIGHT = 0xFF8A8A8A;
    public static final int COLOR_INNER_DARK = 0xFF303030;
    public static final int COLOR_PANEL = 0xFF2B2B2B;
    public static final int COLOR_HEADER = 0xFF5D5D5D;
    public static final int COLOR_TAB_BACKGROUND = 0xFF3D3D3D;
    public static final int COLOR_TAB_SELECTED = 0xFF676767;
    public static final int COLOR_SEPARATOR_DARK = 0xFF171717;
    public static final int COLOR_SEPARATOR_LIGHT = 0xFF777777;
    public static final int COLOR_CONTENT_BACKGROUND = 0xFF434343;
    public static final int COLOR_PROGRESS_BACKGROUND = 0xFF101010;
    public static final int COLOR_PROGRESS_FILL = 0xFF4BAA64;
    public static final int COLOR_TITLE = 0xFFFFFFFF;
    public static final int COLOR_TEXT = 0xFFE8E8E8;
    public static final int COLOR_DIM_TEXT = 0xFFA8A8A8;
    public static final int COLOR_CONNECTED = 0xFF55FF55;
    public static final int COLOR_DISCONNECTED = 0xFFFF5555;
    public static final int COLOR_COMPLETE = 0xFF55FFFF;
    public static final int COLOR_DEATHLINK_ENABLED = 0xFF6E3030;
    public static final int COLOR_COLLECTED_SLOT = 0xFF2D7147;

    public abstract void render(
            DrawContext context,
            TextRenderer renderer,
            int x,
            int y,
            int width,
            int height,
            int mouseX,
            int mouseY,
            float delta
    );

    public static void drawBeveledPanel(
            DrawContext context,
            int x,
            int y,
            int width,
            int height,
            int fillColor
    ) {
        context.fill(x - 2, y - 2, x + width + 2, y + height + 2, COLOR_OUTER_DARK);
        context.fill(x - 1, y - 1, x + width + 1, y + height + 1, COLOR_OUTER_LIGHT);
        context.fill(x, y, x + width, y + height, COLOR_INNER_DARK);
        context.fill(x + 2, y + 2, x + width - 2, y + height - 2, fillColor);
    }

    protected void drawScaledText(
            DrawContext context,
            TextRenderer textRenderer,
            String text,
            int x,
            int y,
            int color,
            float scale
    ) {
        context.getMatrices().push();
        context.getMatrices().translate(x, y, 0);
        context.getMatrices().scale(scale, scale, 1.0f);
        context.drawTextWithShadow(textRenderer, text, 0, 0, color);
        context.getMatrices().pop();
    }

    protected void drawProgressBar(
            DrawContext context,
            int x,
            int y,
            int width,
            int current,
            int total
    ) {
        context.fill(x, y, x + width, y + 9, COLOR_OUTER_DARK);
        context.fill(x + 1, y + 1, x + width - 1, y + 8, COLOR_PROGRESS_BACKGROUND);

        if (total <= 0 || current <= 0) {
            return;
        }

        int fillWidth = (int) ((long) current * (width - 2) / total);
        fillWidth = Math.min(fillWidth, width - 2);

        context.fill(x + 1, y + 1, x + 1 + fillWidth, y + 8, COLOR_PROGRESS_FILL);
    }

    protected final void drawVictoryProgressRow(
            DrawContext context, TextRenderer textRenderer, VictoryProgress condition,
            int x, int y, int width, boolean includeProgressBar
    ) {
        boolean complete = condition.met();
        String count = condition.current() + " / " + condition.required();
        context.drawTextWithShadow(textRenderer, complete ? "✓" : "X", x, y, complete ? COLOR_COMPLETE : COLOR_DISCONNECTED);
        context.drawTextWithShadow(textRenderer, condition.label(), x + 14, y, complete ? COLOR_DIM_TEXT : COLOR_TEXT);
        context.drawTextWithShadow(textRenderer, count, x + width - textRenderer.getWidth(count), y,
                complete ? COLOR_COMPLETE : COLOR_DIM_TEXT);

        if (includeProgressBar) {
            drawProgressBar(context, x + 14, y + 11, width - 14, condition.current(), condition.required());
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double horizontalAmount,
            double verticalAmount
    ) {
        return false;
    }
}
