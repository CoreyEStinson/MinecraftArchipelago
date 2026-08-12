package com.minecraftarchipelago.dashboard.tabs;

import com.minecraftarchipelago.APSession;
import com.minecraftarchipelago.SlotData;
import com.minecraftarchipelago.aplocations.CheckedLocationsState;
import com.minecraftarchipelago.collections.CollectedItemsState;
import com.minecraftarchipelago.collections.ItemCollection;
import com.minecraftarchipelago.collections.ItemCollectionRegistry;
import com.minecraftarchipelago.dashboard.DashboardTab;
import com.minecraftarchipelago.dashboard.ScrollableDashboardTab;
import com.minecraftarchipelago.hud.APHudState;
import com.minecraftarchipelago.victory.VictoryProgress;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GoalTab extends ScrollableDashboardTab {
    private static final int ITEM_CELL_WIDTH = 44;
    private static final int ITEM_CELL_HEIGHT = 28;
    private static final int ITEM_COLUMNS = 8;

    private final Map<String, Boolean> expandedCollections = new HashMap<>();
    private final List<CollectionHitBox> collectionHitBoxes = new ArrayList<>();
    private ItemStack hoveredItemTooltip;
    private int hoveredItemTooltipX;
    private int hoveredItemTooltipY;


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
        collectionHitBoxes.clear();
        hoveredItemTooltip = null;

        if (!APSession.client().isConnected() || !APSession.hasSlotData()) {
            context.drawTextWithShadow(
                    textRenderer,
                    "Connect to Archipelago to view required goals",
                    x,
                    y,
                    COLOR_DIM_TEXT
            );
            resetScroll();
            return;
        }

        SlotData slotData = APSession.getSlotData();
        MinecraftServer server = MinecraftClient.getInstance().getServer();
        Set<Identifier> collected = server == null
                ? Set.of()
                : CollectedItemsState.get(server).getEverHeld();

        beginScrollableContent(context);

        int contentY = contentTop();

        context.drawTextWithShadow(
                textRenderer,
                "Required Goals",
                x,
                contentY,
                COLOR_TITLE
        );

        contentY += 18;

        if (!APHudState.activeConditions.isEmpty()
                && APHudState.activeConditions.stream().allMatch(VictoryProgress::met)) {
            context.fill(x, contentY, x + width, contentY + 22, COLOR_COMPLETE);

            context.drawTextWithShadow(
                    textRenderer,
                    "✓ Victory requirements complete",
                    x + 7,
                    contentY + 7,
                    COLOR_OUTER_DARK
            );

            contentY += 30;
        }

        for (VictoryProgress condition : APHudState.activeConditions) {
            if (condition.label().equals("Boss Kills") || isCollection(condition.label())) {
                continue;
            }

            renderProgressGoal(context, textRenderer, x, contentY, width, condition);
            contentY += 25;
        }

        if (slotData.isBossGoalActive()) {
            context.drawTextWithShadow(
                    textRenderer,
                    "Required Bosses",
                    x,
                    contentY,
                    COLOR_TITLE
            );

            contentY += 16;

            for (String bossId : slotData.getRequiredBossKills()) {
                long locationId = SlotData.getBossLocationId(bossId);
                boolean defeated = server != null
                        && CheckedLocationsState.get(server).isLocationChecked(locationId);

                context.drawItem(new ItemStack(iconForBoss(bossId)), x, contentY - 4);

                context.drawTextWithShadow(
                        textRenderer,
                        formatBossName(bossId),
                        x + 20,
                        contentY,
                        defeated ? COLOR_DIM_TEXT : COLOR_TEXT
                );

                context.drawTextWithShadow(
                        textRenderer,
                        defeated ? "✓" : "✗",
                        x + width - 8,
                        contentY,
                        defeated ? COLOR_COMPLETE : COLOR_DISCONNECTED
                );

                contentY += 20;
            }

            contentY += 4;
        }

        if (!slotData.getRequiredItemCollections().isEmpty()) {
            context.drawTextWithShadow(
                    textRenderer,
                    "Required Collections",
                    x,
                    contentY,
                    COLOR_TITLE
            );

            contentY += 16;

            for (String collectionId : slotData.getRequiredItemCollections()) {
                ItemCollection collection = ItemCollectionRegistry.get(collectionId);

                if (collection == null) {
                    continue;
                }

                contentY = renderCollection(
                        context,
                        textRenderer,
                        collection,
                        collected,
                        x,
                        contentY,
                        width,
                        mouseX,
                        mouseY
                );
            }
        }

        finishScrollableContent(context, contentY);

        if (hoveredItemTooltip != null) {
            context.drawItemTooltip(textRenderer, hoveredItemTooltip, hoveredItemTooltipX, hoveredItemTooltipY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return false;
        }

        for (CollectionHitBox hitBox : collectionHitBoxes) {
            if (hitBox.contains(mouseX, mouseY)) {
                boolean expanded = expandedCollections.getOrDefault(hitBox.collectionId(), false);
                expandedCollections.put(hitBox.collectionId(), !expanded);
                return true;
            }
        }

        return false;
    }

    private int renderCollection(
            DrawContext context,
            TextRenderer textRenderer,
            ItemCollection collection,
            Set<Identifier> collected,
            int x,
            int y,
            int width,
            int mouseX,
            int mouseY
    ) {
        int current = collection.countCollected(collected);
        int total = collection.total();
        boolean complete = current >= total;
        boolean expanded = expandedCollections.getOrDefault(collection.id(), false);
        String count = current + " / " + total;

        context.fill(x, y - 3, x + width, y + 15, COLOR_PANEL);

        context.drawTextWithShadow(
                textRenderer,
                expanded ? "⌄" : "›",
                x + 5,
                y,
                complete ? COLOR_DIM_TEXT : COLOR_TEXT
        );

        context.drawTextWithShadow(
                textRenderer,
                collection.displayName(),
                x + 18,
                y,
                complete ? COLOR_DIM_TEXT : COLOR_TEXT
        );

        context.drawTextWithShadow(
                textRenderer,
                count,
                x + width - 8 - textRenderer.getWidth(count),
                y,
                complete ? COLOR_COMPLETE : COLOR_DIM_TEXT
        );

        collectionHitBoxes.add(new CollectionHitBox(
                collection.id(),
                x,
                y - 3,
                width,
                18
        ));

        y += 22;

        if (!expanded) {
            return y;
        }

        int itemIndex = 0;

        for (Identifier itemId : collection.requiredItems()) {
            int column = itemIndex % ITEM_COLUMNS;
            int row = itemIndex / ITEM_COLUMNS;
            int itemX = x + 4 + column * ITEM_CELL_WIDTH;
            int itemY = y + row * ITEM_CELL_HEIGHT;
            boolean itemCollected = collected.contains(itemId);

            renderCollectionItem(
                    context,
                    textRenderer,
                    itemId,
                    itemCollected,
                    itemX,
                    itemY,
                    mouseX,
                    mouseY
            );

            itemIndex++;
        }

        int rowCount = (itemIndex + ITEM_COLUMNS - 1) / ITEM_COLUMNS;
        return y + rowCount * ITEM_CELL_HEIGHT + 4;
    }

    private void renderCollectionItem(
            DrawContext context,
            TextRenderer textRenderer,
            Identifier itemId,
            boolean collected,
            int x,
            int y,
            int mouseX,
            int mouseY
    ) {
        Item item = Registries.ITEM.get(itemId);
        ItemStack stack = new ItemStack(item);

        context.fill(x - 2, y - 2, x + 18, y + 18, COLOR_OUTER_DARK);
        context.fill(
                x - 1,
                y - 1,
                x + 17,
                y + 17,
                collected ? COLOR_COLLECTED_SLOT : COLOR_PANEL
        );
        context.drawItem(stack, x, y);

        if (mouseX >= x
                && mouseX < x + 16
                && mouseY >= y
                && mouseY < y + 16) {
            hoveredItemTooltip = stack;
            hoveredItemTooltipX = mouseX;
            hoveredItemTooltipY = mouseY;
        }
    }

    private void renderProgressGoal(
            DrawContext context,
            TextRenderer textRenderer,
            int x,
            int y,
            int width,
            VictoryProgress condition
    ) {
        boolean complete = condition.met();
        String count = condition.current() + " / " + condition.required();

        context.drawTextWithShadow(
                textRenderer,
                complete ? "✓" : "✗",
                x,
                y,
                complete ? COLOR_COMPLETE : COLOR_DISCONNECTED
        );

        context.drawTextWithShadow(
                textRenderer,
                condition.label(),
                x + 14,
                y,
                complete ? COLOR_DIM_TEXT : COLOR_TEXT
        );

        context.drawTextWithShadow(
                textRenderer,
                count,
                x + width - textRenderer.getWidth(count),
                y,
                complete ? COLOR_COMPLETE : COLOR_DIM_TEXT
        );

        drawProgressBar(
                context,
                x + 14,
                y + 11,
                width - 14,
                condition.current(),
                condition.required()
        );
    }

    private boolean isCollection(String label) {
        for (ItemCollection collection : ItemCollectionRegistry.getAll()) {
            if (collection.displayName().equals(label)) {
                return true;
            }
        }

        return false;
    }

    private Item iconForBoss(String bossId) {
        return switch (bossId) {
            case "ender_dragon" -> Items.ENDER_DRAGON_SPAWN_EGG;
            case "wither" -> Items.WITHER_SPAWN_EGG;
            case "elder_guardian" -> Items.ELDER_GUARDIAN_SPAWN_EGG;
            case "warden" -> Items.WARDEN_SPAWN_EGG;
            default -> Items.BARRIER;
        };
    }

    private String formatBossName(String bossId) {
        return switch (bossId) {
            case "ender_dragon" -> "Ender Dragon";
            case "elder_guardian" -> "Elder Guardian";
            case "wither" -> "Wither";
            case "warden" -> "Warden";
            default -> bossId;
        };
    }

    private record CollectionHitBox(
            String collectionId,
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
