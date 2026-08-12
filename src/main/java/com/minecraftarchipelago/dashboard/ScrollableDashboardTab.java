package com.minecraftarchipelago.dashboard;

import net.minecraft.client.gui.DrawContext;

public abstract class ScrollableDashboardTab extends DashboardTab {
    protected static final int SCROLL_AMOUNT = 18;

    private int scrollOffset;
    private int maxScroll;
    private int viewportX;
    private int viewportY;
    private int viewportWidth;
    private int viewportHeight;

    protected final void setViewport(int x, int y, int width, int height) {
        viewportX = x;
        viewportY = y;
        viewportWidth = width;
        viewportHeight = Math.max(0, height);
    }

    protected final int contentTop() {
        return viewportY - scrollOffset;
    }

    protected final int getViewportWidth() {
        return viewportWidth;
    }

    protected final void beginScrollableContent(DrawContext context) {
        context.enableScissor(viewportX, viewportY, viewportX + viewportWidth, viewportY + viewportHeight);
    }

    protected final void finishScrollableContent(DrawContext context, int contentBottom) {
        context.disableScissor();
        maxScroll = Math.max(0, contentBottom + scrollOffset - viewportY - viewportHeight + 4);
        scrollOffset = Math.clamp(scrollOffset, 0, maxScroll);
        renderScrollbar(context);
    }

    protected final void resetScroll() {
        scrollOffset = 0;
        maxScroll = 0;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!isInViewport(mouseX, mouseY) || maxScroll == 0) {
            return false;
        }

        scrollOffset = Math.clamp(scrollOffset - (int) (verticalAmount * SCROLL_AMOUNT), 0, maxScroll);
        return true;
    }

    private boolean isInViewport(double mouseX, double mouseY) {
        return mouseX >= viewportX
                && mouseX <= viewportX + viewportWidth
                && mouseY >= viewportY
                && mouseY <= viewportY + viewportHeight;
    }

    private void renderScrollbar(DrawContext context) {
        if (maxScroll <= 0) {
            return;
        }

        int trackX = viewportX + viewportWidth + 4;
        int thumbHeight = Math.max(14, viewportHeight * viewportHeight / (viewportHeight + maxScroll));
        int thumbY = viewportY + (viewportHeight - thumbHeight) * scrollOffset / maxScroll;

        context.fill(trackX, viewportY, trackX + 3, viewportY + viewportHeight, COLOR_OUTER_DARK);
        context.fill(trackX, thumbY, trackX + 3, thumbY + thumbHeight, COLOR_OUTER_LIGHT);
    }
}
