package com.minecraftarchipelago.hud;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.minecraftarchipelago.APSession;
import com.minecraftarchipelago.MinecraftArchipelago;
import com.minecraftarchipelago.SlotData;
import com.minecraftarchipelago.aplocations.CheckedLocationsState;
import com.minecraftarchipelago.aplocations.LocationRegistry;
import com.minecraftarchipelago.aplocations.BossKillLocationRegistry;
import com.minecraftarchipelago.apstages.state.StageUnlockState;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

public final class APHudRenderer {

    // Layout constants
    private static final int PANEL_WIDTH = 165;
    private static final int MARGIN = 5;
    private static final int PAD = 6;
    private static final int LINE = 12;
    private static final int BAR_HEIGHT = 10;

    // Colors
    private static final int COL_BG = 0xCC0A0A14;
    private static final int COL_BORDER = 0xFF334455;
    private static final int COL_TITLE = 0xFFFFAA00;
    private static final int COL_WHITE = 0xFFFFFFFF;
    private static final int COL_DIM = 0xFFAAAAAA;
    private static final int COL_GREEN = 0xFF55FF55;
    private static final int COL_RED = 0xFFFF5555;
    private static final int COL_YELLOW = 0xFFFFFF55;
    private static final int COL_BAR_BG = 0xFF222233;
    private static final int COL_BAR_FILL = 0xFF33AA55;
    private static final int COL_BAR_WIN = 0xFFFFAA00;

    // Saved position
    private static int savedX = -1;
    private static int savedY = -1;

    // Drag state
    private static boolean isDragging = false;
    private static int dragOffsetX = 0;
    private static int dragOffsetY = 0;
    private static boolean prevMouseBtn = false;

    public static void register() {
        loadPosition();
        ClientTickEvents.END_CLIENT_TICK.register(APHudRenderer::updateState);
        HudRenderCallback.EVENT.register(APHudRenderer::render);
    }

    private static void updateState(MinecraftClient client) {
        MinecraftServer server = client.getServer();
        if (server != null && client.player != null) {
            APHudState.connected = APSession.CLIENT.isConnected();
            APHudState.address = APSession.CLIENT.getConnectedAddress();
            if (APHudState.address == null) APHudState.address = "-";

            CheckedLocationsState locs = CheckedLocationsState.get(server);

            APHudState.locationsChecked = locs.checkedCount();
            APHudState.locationsTotal   = LocationRegistry.size();
            APHudState.goalAchieved     = locs.isGoalAchieved();

            // Boss kill states
            APHudState.bossKills.clear();
            APHudState.bossKillsChecked = 0;
            for (var entry : BossKillLocationRegistry.getAll().entrySet()) {
                Identifier entityId = entry.getKey();
                long bossLocId = entry.getValue();
                boolean killed = locs.isLocationChecked(bossLocId);
                
                String nameKey = "entity." + entityId.getNamespace() + "." + entityId.getPath();
                String displayName = Text.translatable(nameKey).getString();
                
                APHudState.bossKills.put(displayName, killed);
                if (killed) {
                    APHudState.bossKillsChecked++;
                }
            }

            APHudState.lootableChecksTotal = APSession.hasSlotData()
                    ? APSession.getSlotData().getLootableChecks()
                    : 0;

            APHudState.lootableChecksFound = (server != null)
                    ? CheckedLocationsState.get(server).countCheckedInRange(
                    SlotData.LOOTABLE_CHECK_BASE_ID,
                    SlotData.LOOTABLE_CHECK_BASE_ID + APHudState.lootableChecksTotal)
                    : 0;

            // Advancements
            APHudState.advancementsChecked =
                    APHudState.locationsChecked - APHudState.bossKillsChecked - APHudState.lootableChecksFound;
            APHudState.advancementsTotal   =
                    APHudState.locationsTotal;

            ServerPlayerEntity serverPlayer =
                    server.getPlayerManager().getPlayer(client.player.getUuid());
            if (serverPlayer != null) {
                Set<Identifier> unlocked = StageUnlockState.get(server)
                        .getUnlocked(serverPlayer.getUuid());
                int raw = unlocked.size();
                APHudState.stagesUnlocked = Math.max(0, raw - 1);
                
                int darkGray = 0xFF555555;
                if (unlocked.contains(Identifier.of("minecraftarchipelago", "armor/netherite_armor"))) {
                    APHudState.armorTier = "Netherite";
                    APHudState.armorColor = 0xFF550000;
                } else if (unlocked.contains(Identifier.of("minecraftarchipelago", "armor/diamond_armor"))) {
                    APHudState.armorTier = "Diamond";
                    APHudState.armorColor = 0xFF55FFFF;
                } else if (unlocked.contains(Identifier.of("minecraftarchipelago", "armor/iron_armor"))) {
                    APHudState.armorTier = "Iron";
                    APHudState.armorColor = 0xFFFFFFFF;
                } else if (unlocked.contains(Identifier.of("minecraftarchipelago", "armor/leather_armor"))) {
                    APHudState.armorTier = "Leather";
                    APHudState.armorColor = 0xFFFFAA00;
                } else {
                    APHudState.armorTier = "None";
                    APHudState.armorColor = darkGray;
                }
                
                if (unlocked.contains(Identifier.of("minecraftarchipelago", "tools/netherite_tools"))) {
                    APHudState.toolTier = "Netherite";
                    APHudState.toolColor = 0xFF550000;
                } else if (unlocked.contains(Identifier.of("minecraftarchipelago", "tools/diamond_tools"))) {
                    APHudState.toolTier = "Diamond";
                    APHudState.toolColor = 0xFF55FFFF;
                } else if (unlocked.contains(Identifier.of("minecraftarchipelago", "tools/iron_tools"))) {
                    APHudState.toolTier = "Iron";
                    APHudState.toolColor = 0xFFFFFFFF;
                } else if (unlocked.contains(Identifier.of("minecraftarchipelago", "tools/stone_tools"))) {
                    APHudState.toolTier = "Stone";
                    APHudState.toolColor = 0xFFAAAAAA;
                } else {
                    APHudState.toolTier = "None";
                    APHudState.toolColor = darkGray;
                }
            }
        }
    }

    private static void render(DrawContext ctx, RenderTickCounter delta) {
        if (!APHudState.visible) return;
        if (MinecraftClient.getInstance().world == null) return;

        MinecraftClient client = MinecraftClient.getInstance();

        TextRenderer tr = client.textRenderer;
        int sw = client.getWindow().getScaledWidth();
        int sh = client.getWindow().getScaledHeight();
        int height = computeHeight();

        // Resolve saved/default position, then appy drag for this frame
        int[] pos = resolvedPosition(sw, sh, height);
        pos = handleDrag(client, pos[0], pos[1], height);
        int x = pos[0];
        int y = pos[1];

        // Detect Alt for visual hints
        long handle = client.getWindow().getHandle();
        boolean altHeld =
                GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_LEFT_ALT) == GLFW.GLFW_PRESS ||
                GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_RIGHT_ALT) == GLFW.GLFW_PRESS;

        // Border changes color to signal drag-ready or dragging
        int borderColor = isDragging ? 0xFFFFAA00       // gold while dragging
                        : altHeld    ? 0xFF4488AA       // blue when Alt held
                        : COL_BORDER;                   // normal

        ctx.fill(x - 1, y - 1, x + PANEL_WIDTH + 1, y + height + 1, borderColor);
        ctx.fill(x, y, x + PANEL_WIDTH, y + height, COL_BG);

        int cx = x + PAD;
        int cy = y + PAD;

        // --- Title ---
        ctx.drawTextWithShadow(tr, "⚡ Archipelago", cx, cy, COL_TITLE);
        cy += LINE + 2;
        cy = separator(ctx, cx, cy, x);

        // --- Connection ---
        if (APHudState.connected) {
            ctx.drawTextWithShadow(tr, "● Connected", cx, cy += 2, COL_GREEN);
            cy += LINE;
            ctx.drawTextWithShadow(tr,
                    truncate(tr, APHudState.address, PANEL_WIDTH - PAD * 2),
                    cx + 4, cy, COL_DIM);
            cy += LINE;
            ctx.drawTextWithShadow(tr, "Slot: " + APSession.slotName, cx + 4, cy, COL_WHITE);
        } else {
            ctx.drawTextWithShadow(tr, "○ Disconnected", cx, cy, COL_RED);
            return;
        }
        cy += LINE;
        cy = separator(ctx, cx, cy + 2, x);

        // ── Advancements ──────────────────────────────────────────────────────
        ctx.drawTextWithShadow(tr, "Advancements", cx, cy += 2, COL_TITLE);
        cy += LINE;

        // Count
        String advCount = APHudState.advancementsChecked + " / " + APHudState.advancementsTotal;
        ctx.drawTextWithShadow(tr, advCount, cx + 4, cy, COL_WHITE);
        cy += LINE;

        // Progress bar
        int barX = cx + 2;
        int barW = PANEL_WIDTH - PAD * 2 - 4;
        int fillW = (int)(APHudState.progressFraction() * barW);
        int barCol = APHudState.goalAchieved ? COL_BAR_WIN : COL_BAR_FILL;
        ctx.fill(barX, cy, barX + barW, cy + BAR_HEIGHT, COL_BAR_BG);
        if (fillW > 0) ctx.fill(barX, cy, barX + fillW, cy + BAR_HEIGHT, barCol);
        // Goal marker (based on full location pool)
        if (APHudState.advancementsTotal > 0) {
            int totalBossKills = APHudState.bossKills.size();
            int goalX = barX + (int)(
                    (float)(APHudState.locationsRequired() - totalBossKills)
                            / APHudState.advancementsTotal * barW
            );
            goalX = Math.max(barX, Math.min(goalX, barX + barW - 1));
            ctx.fill(goalX, cy - 1, goalX + 1, cy + BAR_HEIGHT + 1, COL_YELLOW);
        }
        cy += BAR_HEIGHT + 4;

        // Goal text — references total locations (all types)
        if (APHudState.goalAchieved) {
            ctx.drawTextWithShadow(tr, "✓ Goal complete!", cx + 4, cy, COL_YELLOW);
            cy += LINE;
        } else {
            int req       = APHudState.locationsRequired();
            int remaining = APHudState.locationsRemaining() + APHudState.bossKillsChecked + APHudState.lootableChecksFound;
            ctx.drawTextWithShadow(tr,
                    "Goal: " + APHudState.goalPercent + "% → " + req + " total",
                    cx + 4, cy, COL_DIM);
            cy += LINE;
            int remCol = remaining <= 10 ? COL_YELLOW : COL_WHITE;
            ctx.drawTextWithShadow(tr, remaining + " more needed", cx + 4, cy, remCol);
            cy += LINE;
        }
        cy = separator(ctx, cx, cy + 2, x);

        // ── Boss Kills ─────────────────────────────────────────────────────────
        ctx.drawTextWithShadow(tr, "Boss Kills", cx, cy += 2, COL_TITLE);
        cy += LINE;

        for (var entry : APHudState.bossKills.entrySet()) {
            drawBossLine(ctx, tr, cx, cy, entry.getKey(), entry.getValue());
            cy += LINE;
        }

        int totalBoss = APHudState.bossKills.size();
        int bossColor = (totalBoss > 0 && APHudState.bossKillsChecked == totalBoss)
                ? COL_YELLOW : COL_DIM;
        ctx.drawTextWithShadow(tr,
                APHudState.bossKillsChecked + " / " + totalBoss + " killed",
                cx + 4, cy, bossColor);
        cy += LINE;
        cy = separator(ctx, cx, cy + 2, x);

        // ── Lootable Checks ───────────────────────────────────────────────────────
        ctx.drawTextWithShadow(tr, "Lootable Checks", cx, cy += 2, COL_TITLE);
        cy += LINE;

        String lootCount = APHudState.lootableChecksFound
                + " / " + APHudState.lootableChecksTotal;
        ctx.drawTextWithShadow(tr, lootCount, cx + 4, cy, COL_WHITE);
        cy += LINE;

        int lootBarW  = PANEL_WIDTH - PAD * 2 - 4;
        int lootFillW = APHudState.lootableChecksTotal > 0
                ? (int)((float) APHudState.lootableChecksFound
                / APHudState.lootableChecksTotal * lootBarW)
                : 0;
        int lootColor = (APHudState.lootableChecksTotal > 0
                && APHudState.lootableChecksFound >= APHudState.lootableChecksTotal)
                ? COL_BAR_WIN : COL_BAR_FILL;
        ctx.fill(cx + 2, cy, cx + 2 + lootBarW, cy + BAR_HEIGHT, COL_BAR_BG);
        if (lootFillW > 0) ctx.fill(cx + 2, cy, cx + 2 + lootFillW, cy + BAR_HEIGHT, lootColor);
        cy += BAR_HEIGHT + 4;
        cy = separator(ctx, cx, cy + 2, x);


        // ── Equipment ──────────────────────────────────────────────────────────
        ctx.drawTextWithShadow(tr, "Armor Tier Unlocked:", cx, cy += 2, COL_TITLE);
        cy += LINE;
        ctx.drawTextWithShadow(tr, APHudState.armorTier, cx + 4, cy, APHudState.armorColor);
        cy += LINE;
        
        ctx.drawTextWithShadow(tr, "Tool Tier Unlocked:", cx, cy, COL_TITLE);
        cy += LINE;
        ctx.drawTextWithShadow(tr, APHudState.toolTier, cx + 4, cy, APHudState.toolColor);
    }

    // Helpers

    // Draws a horizontal rule a returns the update y cursor
    private static int separator(DrawContext ctx, int cx, int cy, int panelX) {
        ctx.fill(cx, cy, panelX + PANEL_WIDTH - PAD, cy + 1, COL_BORDER);
        return cy + 4;
    }

    // Truncates a string so it fits within maxWidth pixels
    private static String truncate(TextRenderer tr, String text, int maxWidth) {
        if (tr.getWidth(text) <= maxWidth) return text;
        while (!text.isEmpty() && tr.getWidth(text + "…") > maxWidth) {
            text = text.substring(0, text.length() - 1);
        }
        return text + "…";
    }

    // Computes panel height based on current state
    private static int computeHeight() {
        int h = PAD; // Top padding

        // Title
        h += LINE + 2;
        h += 4; // separator

        // Connection
        if (!APHudState.connected) {
            return h + LINE + PAD;
        }

        h += 2 + LINE * 3; // Connected text, address, slot
        h += 6; // separator

        // Advancements
        h += 2 + LINE; // Header
        h += LINE; // Count
        h += BAR_HEIGHT + 4; // Bar
        h += APHudState.goalAchieved ? LINE : (LINE * 2); // Goal text
        h += 6; // separator

        // Boss Kills
        h += 2 + LINE; // Header
        h += LINE * APHudState.bossKills.size(); // Entries
        h += LINE; // Kill counter
        h += 6; // separator

        // Lootable Checks
        h += 2 + LINE; // Header
        h += LINE; // Count
        h += BAR_HEIGHT + 4; // Bar
        h += 6; // separator

        // Equipment
        h += 2 + LINE; // Armor header
        h += LINE;     // Armor tier
        h += LINE;     // Tool header

        return h + LINE + PAD; // Tool tier + bottom padding
    }

    // Position persistence
    private static void loadPosition() {
        Path path = FabricLoader.getInstance().getConfigDir()
                .resolve("minecraftarchipelago_hud.json");
        if(!Files.exists(path)) return;
        try {
            JsonObject obj = new Gson().fromJson(Files.readString(path), JsonObject.class);
            if (obj.has("x")) savedX = obj.get("x").getAsInt();
            if (obj.has("y")) savedY = obj.get("y").getAsInt();
            if (obj.has("visible")) APHudState.visible = obj.get("visible").getAsBoolean();
        } catch (Exception e) {
            MinecraftArchipelago.LOGGER.warn("[AP HUD] Could not load position: {}", e.getMessage());
        }
    }

    public static void saveConfig() {
        Path path = FabricLoader.getInstance().getConfigDir()
                .resolve("minecraftarchipelago_hud.json");
        try {
            JsonObject obj = new JsonObject();
            if (savedX >= 0) obj.addProperty("x", savedX);
            if (savedY >= 0) obj.addProperty("y", savedY);
            obj.addProperty("visible", APHudState.visible);
            Files.writeString(path, new Gson().toJson(obj));
        } catch (Exception e) {
            MinecraftArchipelago.LOGGER.warn("[AP HUD] Could not save config: {}", e.getMessage());
        }
    }

    // Position resolution
    /**
     * Returns [x, y] for the panel this frame.
     * Falls back to top-right if no saved position.
     * Clamps to screen so window resizes can't push it off-screen.
     */
    private static int[] resolvedPosition(int sw, int sh, int panelHeight) {
        int x = (savedX >= 0) ? savedX : sw - PANEL_WIDTH - MARGIN;
        int y = (savedY >= 0) ? savedY : MARGIN;
        x = Math.max(0, Math.min(x, sw - PANEL_WIDTH));
        y = Math.max(0, Math.min(y, sh - panelHeight));
        return new int[]{x, y};
    }

    private static void drawBossLine(DrawContext ctx, TextRenderer tr,
                                     int cx, int cy, String name, boolean killed) {
        String icon  = killed ? "✓" : "○";
        int color = killed ? COL_GREEN : COL_DIM;
        ctx.drawTextWithShadow(tr, icon + " " + name, cx + 4, cy, color);
    }

    // Drag handler
    /**
     * Call at the start of render() each frame.
     * Alt + left-click-drag repositions the panel.
     * Saves to disk when drag ends.
     * Returns updated [x, y] for this frame.
     */
    private static int[] handleDrag(MinecraftClient client, int px, int py, int panelHeight) {

        long handle = client.getWindow().getHandle();

        boolean mouseDown = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT)
                == GLFW.GLFW_PRESS;
        boolean altHeld =
                GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_LEFT_ALT) == GLFW.GLFW_PRESS ||
                GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_RIGHT_ALT) == GLFW.GLFW_PRESS;

        int sw = client.getWindow().getScaledWidth();
        int sh = client.getWindow().getScaledHeight();
        int mx = (int)(client.mouse.getX() * sw / client.getWindow().getWidth());
        int my = (int)(client.mouse.getY() * sh / client.getWindow().getHeight());

        // Start drag: Alt held, mouse just pressed, cursor is inside panel
        if (mouseDown && !prevMouseBtn && altHeld) {
            if (mx >= px && mx <= px + PANEL_WIDTH &&
                my >= py && my <= py + panelHeight) {
                isDragging = true;
                dragOffsetX = mx - px;
                dragOffsetY = my - py;
            }
        }

        // Update position while dragging
        if (isDragging && mouseDown) {
            px = Math.max(0, Math.min(mx - dragOffsetX, sw - PANEL_WIDTH));
            py = Math.max(0, Math.min(my - dragOffsetY, sh - panelHeight));
            savedX = px;
            savedY = py;
        }

        // Mouse released - end drag and persist position
        if (!mouseDown && prevMouseBtn && isDragging) {
            isDragging = false;
            saveConfig();
        }

        prevMouseBtn = mouseDown;
        return new int[]{px, py};
    }

    private APHudRenderer() {}
}
