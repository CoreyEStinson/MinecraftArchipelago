package com.minecraftarchipelago.dashboard.tabs;

import com.minecraftarchipelago.aplocations.CheckedLocationsState;
import com.minecraftarchipelago.aplocations.LocationRegistry;
import com.minecraftarchipelago.apstages.state.StageUnlockState;
import com.minecraftarchipelago.dashboard.DashboardPreferences;
import com.minecraftarchipelago.dashboard.ScrollableDashboardTab;
import com.minecraftarchipelago.dashboard.progress.AdvancementAccessRules;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.*;

public class ProgressTab extends ScrollableDashboardTab {
    private static final int CELL_SIZE = 35;

    private final Map<String, Boolean> expandedCategories = new HashMap<>();
    private final List<CategoryHitBox> categoryHitBoxes = new ArrayList<>();

    private int toggleX;
    private int toggleY;
    private int toggleWidth;
    private int toggleHeight;
    private List<Text> hoveredTooltip;
    private int hoveredTooltipX;
    private int hoveredTooltipY;

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
        MinecraftClient client = MinecraftClient.getInstance();
        MinecraftServer server = client.getServer();

        if (server == null || client.player == null) {
            context.drawTextWithShadow(
                    textRenderer,
                    "Load a singleplayer world to view advancement progress.",
                    x,
                    y,
                    COLOR_DIM_TEXT
            );
            resetScroll();
            return;
        }

        List<AdvancementStatus> advancements = loadAdvancements(server, client);
        int completed = (int) advancements.stream()
                .filter(AdvancementStatus::completed)
                .count();

        context.drawTextWithShadow(
                textRenderer,
                "Advancement Progress",
                x,
                y,
                COLOR_TITLE
        );

        String summary = completed + " / " + advancements.size();
        context.drawTextWithShadow(
                textRenderer,
                summary,
                x + width - textRenderer.getWidth(summary),
                y,
                COLOR_DIM_TEXT
        );

        drawProgressBar(context, x, y + 12, width, completed, advancements.size());

        toggleX = x;
        toggleY = y + 25;
        toggleWidth = 112;
        toggleHeight = 18;

        boolean hideCompleted = DashboardPreferences.get().hideCompletedAdvancements;

        context.fill(
                toggleX,
                toggleY,
                toggleX + toggleWidth,
                toggleY + toggleHeight,
                COLOR_PANEL
        );

        context.fill(
                toggleX + 4,
                toggleY + 4,
                toggleX + 14,
                toggleY + 14,
                COLOR_OUTER_DARK
        );

        if (hideCompleted) {
            context.fill(
                    toggleX + 6,
                    toggleY + 6,
                    toggleX + 12,
                    toggleY + 12,
                    COLOR_COMPLETE
            );
        }

        context.drawTextWithShadow(
                textRenderer,
                "Hide completed",
                toggleX + 19,
                toggleY + 5,
                COLOR_TEXT
        );

        setViewport(x, y + 49, width, height - 49);
        categoryHitBoxes.clear();
        hoveredTooltip = null;

        beginScrollableContent(context);

        int contentY = contentTop();

        for (String category : List.of("story", "nether", "end", "adventure", "husbandry")) {
            List<AdvancementStatus> allCategoryEntries = advancements.stream()
                    .filter(entry -> category.equals(categoryOf(entry.id())))
                    .toList();

            List<AdvancementStatus> categoryEntries = allCategoryEntries.stream()
                    .filter(entry -> !hideCompleted || !entry.completed())
                    .toList();

            if (categoryEntries.isEmpty()) {
                continue;
            }

            int categoryCompleted = (int) allCategoryEntries.stream()
                    .filter(AdvancementStatus::completed)
                    .count();

            contentY = renderCategory(
                    context,
                    textRenderer,
                    category,
                    categoryEntries,
                    categoryCompleted,
                    allCategoryEntries.size(),
                    contentY,
                    x,
                    width,
                    mouseX,
                    mouseY
            );
        }

        finishScrollableContent(context, contentY);

        if (hoveredTooltip != null) {
            context.drawTooltip(textRenderer, hoveredTooltip, hoveredTooltipX, hoveredTooltipY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return false;
        }

        if (mouseX >= toggleX
                && mouseX < toggleX + toggleWidth
                && mouseY >= toggleY
                && mouseY < toggleY + toggleHeight) {
            DashboardPreferences preferences = DashboardPreferences.get();
            preferences.hideCompletedAdvancements = !preferences.hideCompletedAdvancements;
            preferences.save();
            return true;
        }

        for (CategoryHitBox hitBox : categoryHitBoxes) {
            if (hitBox.contains(mouseX, mouseY)) {
                boolean expanded = expandedCategories.getOrDefault(hitBox.category(), true);
                expandedCategories.put(hitBox.category(), !expanded);
                return true;
            }
        }

        return false;
    }

    private int renderCategory(
            DrawContext context,
            TextRenderer textRenderer,
            String category,
            List<AdvancementStatus> entries,
            int completed,
            int total,
            int y,
            int x,
            int width,
            int mouseX,
            int mouseY
    ) {
        boolean expanded = expandedCategories.getOrDefault(category, true);
        String heading = displayCategory(category);
        String summary = completed + " / " + total;

        context.fill(x, y, x + width, y + 20, COLOR_PANEL);

        context.drawTextWithShadow(
                textRenderer,
                expanded ? "⌄" : "›",
                x + 6,
                y + 6,
                COLOR_TEXT
        );

        context.drawTextWithShadow(
                textRenderer,
                heading,
                x + 18,
                y + 6,
                COLOR_TITLE
        );

        context.drawTextWithShadow(
                textRenderer,
                summary,
                x + width - 8 - textRenderer.getWidth(summary),
                y + 6,
                COLOR_DIM_TEXT
        );

        categoryHitBoxes.add(new CategoryHitBox(category, x, y, width, 20));

        y += 24;

        if (!expanded) {
            return y;
        }

        int columns = Math.max(1, width / CELL_SIZE);

        for (int index = 0; index < entries.size(); index++) {
            AdvancementStatus entry = entries.get(index);
            int cellX = x + (index % columns) * CELL_SIZE;
            int cellY = y + (index / columns) * CELL_SIZE;

            renderAdvancementCell(
                    context,
                    textRenderer,
                    entry,
                    cellX,
                    cellY,
                    mouseX,
                    mouseY
            );
        }

        int rows = (entries.size() + columns - 1) / columns;
        return y + rows * CELL_SIZE + 6;
    }

    private void renderAdvancementCell(
            DrawContext context,
            TextRenderer textRenderer,
            AdvancementStatus entry,
            int x,
            int y,
            int mouseX,
            int mouseY
    ) {
        context.fill(x, y, x + 20, y + 20, COLOR_OUTER_DARK);
        context.fill(
                x + 1,
                y + 1,
                x + 19,
                y + 19,
                entry.completed() ? COLOR_COLLECTED_SLOT : COLOR_PANEL
        );

        if (entry.ready() && !entry.completed()) {
            context.fill(x + 1, y + 1, x + 19, y + 2, COLOR_COMPLETE);
            context.fill(x + 1, y + 18, x + 19, y + 19, COLOR_COMPLETE);
            context.fill(x + 1, y + 1, x + 2, y + 19, COLOR_COMPLETE);
            context.fill(x + 18, y + 1, x + 19, y + 19, COLOR_COMPLETE);
        }

        context.drawItem(entry.icon(), x + 2, y + 2);

        if (entry.completed()) {
            context.fill(x + 2, y + 2, x + 18, y + 18, 0x88000000);
        } else if (!entry.ready()) {
            context.fill(x + 2, y + 2, x + 18, y + 18, 0x99000000);
        }

        if (mouseX >= x
                && mouseX < x + 20
                && mouseY >= y
                && mouseY < y + 20) {
            List<Text> tooltip = new ArrayList<>();
            tooltip.add(entry.title);

            if (DashboardPreferences.get().showAdvancementDescriptions
                    && !entry.description().getString().isBlank()) {
                tooltip.add(entry.description());
            }

            if (entry.completed()) {
                tooltip.add(Text.literal("Completed"));
            } else if (entry.ready()) {
                tooltip.add(Text.literal("Accessible in Logic"));
            } else {
                tooltip.add(Text.literal(
                        "Missing checks: " + String.join(", ", entry.missingRequirements())
                ));
            }

            hoveredTooltip = wrapTooltip(textRenderer, tooltip);
            hoveredTooltipX = mouseX;
            hoveredTooltipY = mouseY;
        }
    }

    private List<Text> wrapTooltip(TextRenderer textRenderer, List<Text> tooltip) {
        int maxWidth = Math.max(120, getViewportWidth() - 16);
        List<Text> wrappedTooltip = new ArrayList<>();

        for (Text line : tooltip) {
            StringBuilder wrappedLine = new StringBuilder();

            for (String word : line.getString().split(" ")) {
                String candidate = wrappedLine.isEmpty()
                        ? word
                        : wrappedLine + " " + word;

                if (!wrappedLine.isEmpty() && textRenderer.getWidth(candidate) > maxWidth) {
                    wrappedTooltip.add(Text.literal(wrappedLine.toString()));
                    wrappedLine.setLength(0);
                }

                if (!wrappedLine.isEmpty()) {
                    wrappedLine.append(' ');
                }
                wrappedLine.append(word);
            }

            wrappedTooltip.add(Text.literal(wrappedLine.toString()));
        }

        return wrappedTooltip;
    }

    private List<AdvancementStatus> loadAdvancements(
            MinecraftServer server,
            MinecraftClient client
    ) {
        Set<Identifier> unlockedStages = StageUnlockState.get(server)
                .getUnlocked(client.player.getUuid());

        CheckedLocationsState checkedLocations = CheckedLocationsState.get(server);
        List<AdvancementStatus> result = new ArrayList<>();

        for (Map.Entry<Identifier, Long> entry : LocationRegistry.getAll().entrySet()) {
            AdvancementEntry advancement = server.getAdvancementLoader().get(entry.getKey());

            if (advancement == null) {
                continue;
            }

            AdvancementDisplay display = advancement.value().display().orElse(null);

            if (display == null) {
                continue;
            }

            boolean completed = checkedLocations.isLocationChecked(entry.getValue());

            AdvancementAccessRules.AccessResult access =
                    AdvancementAccessRules.evaluate(entry.getKey(), unlockedStages);

            result.add(new AdvancementStatus(
                    entry.getKey(),
                    display.getIcon(),
                    display.getTitle(),
                    display.getDescription(),
                    completed,
                    access.ready(),
                    access.missingRequirements()
            ));
        }

        result.sort(Comparator.comparing(entry -> entry.id().toString()));
        return result;
    }

    private String categoryOf(Identifier id) {
        String path = id.getPath();
        int separator = path.indexOf('/');

        return separator < 0 ? "other" : path.substring(0, separator);
    }

    private String displayCategory(String category) {
        return switch (category) {
            case "story" -> "Story";
            case "nether" -> "Nether";
            case "end" -> "The End";
            case "adventure" -> "Adventure";
            case "husbandry" -> "Husbandry";
            default -> category;
        };
    }

    private record AdvancementStatus(
            Identifier id,
            ItemStack icon,
            Text title,
            Text description,
            boolean completed,
            boolean ready,
            List<String> missingRequirements
    ) {
    }

    private record CategoryHitBox(
            String category,
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
