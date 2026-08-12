package com.minecraftarchipelago.dashboard.tabs;

import com.minecraftarchipelago.dashboard.DashboardPreferences;
import com.minecraftarchipelago.dashboard.DashboardTab;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public class SettingsTab extends DashboardTab {
    private static final int ROW_HEIGHT = 26;

    private final List<SettingHitBox> hitBoxes = new ArrayList<>();

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
        DashboardPreferences preferences = DashboardPreferences.get();
        hitBoxes.clear();

        context.drawTextWithShadow(
                textRenderer,
                "Display Preferences",
                x,
                y,
                COLOR_TITLE
        );

        y += 18;

        y = renderToggleRow(
                context,
                textRenderer,
                "Hide completed advancements",
                "Only show advancement checks that are unfinished.",
                preferences.hideCompletedAdvancements,
                Setting.HIDE_COMPLETED,
                x,
                y,
                width
        );

        y = renderToggleRow(
                context,
                textRenderer,
                "Show advancement descriptions",
                "Include the vanilla description in advancement tooltips.",
                preferences.showAdvancementDescriptions,
                Setting.SHOW_DESCRIPTIONS,
                x,
                y,
                width
        );

        y += 7;

        context.drawTextWithShadow(
                textRenderer,
                "Overview Preferences",
                x,
                y,
                COLOR_TITLE
        );

        y += 18;

        y = renderToggleRow(
                context,
                textRenderer,
                "Show DeathLink status strip",
                "Display the prominent DeathLink status on Overview.",
                preferences.showDeathLinkStatusStrip,
                Setting.SHOW_DEATHLINK_STRIP,
                x,
                y,
                width
        );

        renderChoiceRow(
                context,
                textRenderer,
                "Recent activity rows",
                "Overview shows the newest " + preferences.recentActivityCount + " gifts or unlocks.",
                Integer.toString(preferences.recentActivityCount),
                Setting.RECENT_ACTIVITY_COUNT,
                x,
                y,
                width
        );
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return false;
        }

        for (SettingHitBox hitBox : hitBoxes) {
            if (!hitBox.contains(mouseX, mouseY)) {
                continue;
            }

            DashboardPreferences preferences = DashboardPreferences.get();

            switch (hitBox.setting()) {
                case HIDE_COMPLETED ->
                        preferences.hideCompletedAdvancements = !preferences.hideCompletedAdvancements;
                case SHOW_DESCRIPTIONS ->
                        preferences.showAdvancementDescriptions = !preferences.showAdvancementDescriptions;
                case SHOW_DEATHLINK_STRIP ->
                        preferences.showDeathLinkStatusStrip = !preferences.showDeathLinkStatusStrip;
                case RECENT_ACTIVITY_COUNT ->
                        preferences.recentActivityCount =
                                preferences.recentActivityCount == 5 ? 3 : 5;
            }

            preferences.save();
            return true;
        }

        return false;
    }

    private int renderToggleRow(
            DrawContext context,
            TextRenderer textRenderer,
            String title,
            String description,
            boolean enabled,
            Setting setting,
            int x,
            int y,
            int width
    ) {
        context.fill(x, y, x + width, y + ROW_HEIGHT - 2, COLOR_PANEL);

        context.fill(x + 5, y + 5, x + 17, y + 17, COLOR_OUTER_DARK);
        context.fill(x + 6, y + 6, x + 16, y + 16, COLOR_CONTENT_BACKGROUND);

        if (enabled) {
            context.fill(x + 8, y + 8, x + 14, y + 14, COLOR_COMPLETE);
        }

        context.drawTextWithShadow(
                textRenderer,
                title,
                x + 24,
                y + 4,
                COLOR_TEXT
        );

        context.drawTextWithShadow(
                textRenderer,
                description,
                x + 24,
                y + 14,
                COLOR_DIM_TEXT
        );

        hitBoxes.add(new SettingHitBox(setting, x, y, width, ROW_HEIGHT - 2));
        return y + ROW_HEIGHT;
    }

    private void renderChoiceRow(
            DrawContext context,
            TextRenderer textRenderer,
            String title,
            String description,
            String value,
            Setting setting,
            int x,
            int y,
            int width
    ) {
        context.fill(x, y, x + width, y + ROW_HEIGHT - 2, COLOR_PANEL);

        context.drawTextWithShadow(
                textRenderer,
                title,
                x + 7,
                y + 4,
                COLOR_TEXT
        );

        context.drawTextWithShadow(
                textRenderer,
                description,
                x + 7,
                y + 14,
                COLOR_DIM_TEXT
        );

        int valueX = x + width - 10 - textRenderer.getWidth(value);

        context.drawTextWithShadow(
                textRenderer,
                value,
                valueX,
                y + 9,
                COLOR_COMPLETE
        );

        hitBoxes.add(new SettingHitBox(setting, x, y, width, ROW_HEIGHT - 2));
    }

    private enum Setting {
        HIDE_COMPLETED,
        SHOW_DESCRIPTIONS,
        SHOW_DEATHLINK_STRIP,
        RECENT_ACTIVITY_COUNT
    }

    private record SettingHitBox(
            Setting setting,
            int x,
            int y,
            int width,
            int height
    ) {
        private boolean contains(double mouseX, double mouseY) {
            return mouseX >= x
                    && mouseX < x + width
                    && mouseY >= y
                    && mouseY < y + height;
        }
    }
}