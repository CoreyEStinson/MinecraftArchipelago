package com.minecraftarchipelago.hud;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.minecraftarchipelago.APSession;
import com.minecraftarchipelago.MinecraftArchipelago;
import com.minecraftarchipelago.SlotData;
import com.minecraftarchipelago.aplocations.BossKillLocationRegistry;
import com.minecraftarchipelago.aplocations.CheckedLocationsState;
import com.minecraftarchipelago.aplocations.LocationRegistry;
import com.minecraftarchipelago.apstages.state.StageUnlockState;
import com.minecraftarchipelago.victory.VictoryConditionRegistry;
import com.minecraftarchipelago.victory.VictoryProgress;
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
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Set;

public final class APHudRenderer {

    // ── Layout ────────────────────────────────────────────────────────────────
    private static final int PANEL_WIDTH = 165;
    private static final int MARGIN      = 5;
    private static final int PAD         = 6;
    private static final int LINE        = 12;
    private static final int BAR_HEIGHT  = 10;

    // ── Colours ───────────────────────────────────────────────────────────────
    private static final int COL_BG        = 0xCC0A0A14;
    private static final int COL_BORDER    = 0xFF334455;
    private static final int COL_TITLE     = 0xFFFFAA00;
    private static final int COL_WHITE     = 0xFFFFFFFF;
    private static final int COL_DIM       = 0xFFAAAAAA;
    private static final int COL_GREEN     = 0xFF55FF55;
    private static final int COL_RED       = 0xFFFF5555;
    private static final int COL_YELLOW    = 0xFFFFFF55;
    private static final int COL_BAR_BG    = 0xFF222233;
    private static final int COL_BAR_FILL  = 0xFF33AA55;
    private static final int COL_BAR_WIN   = 0xFFFFAA00;

    // ── Drag state ────────────────────────────────────────────────────────────
    private static int     savedX      = -1;
    private static int     savedY      = -1;
    private static boolean isDragging  = false;
    private static int     dragOffsetX = 0;
    private static int     dragOffsetY = 0;
    private static boolean prevMouseBtn = false;

    // ── Registration ──────────────────────────────────────────────────────────

    public static void register() {
        loadPosition();
        ClientTickEvents.END_CLIENT_TICK.register(APHudRenderer::updateState);
        HudRenderCallback.EVENT.register(APHudRenderer::render);
    }

    // ── State update ──────────────────────────────────────────────────────────

    private static void updateState(MinecraftClient client) {
        MinecraftServer server = client.getServer();
        if (server == null || client.player == null) return;

        APHudState.connected = APSession.CLIENT.isConnected();
        APHudState.address   = APSession.CLIENT.getConnectedAddress();
        if (APHudState.address == null) APHudState.address = "—";

        CheckedLocationsState locs = CheckedLocationsState.get(server);

        APHudState.locationsChecked = locs.checkedCount();
        APHudState.locationsTotal   = LocationRegistry.size();
        APHudState.goalAchieved     = locs.isGoalAchieved();

        // ── Boss kills ────────────────────────────────────────────────────────
        APHudState.bossKills.clear();
        APHudState.requiredBossDisplayNames.clear();
        APHudState.bossKillsChecked = 0;

        for (var entry : BossKillLocationRegistry.getAll().entrySet()) {
            Identifier entityId  = entry.getKey();
            long       bossLocId = entry.getValue();
            boolean    killed    = locs.isLocationChecked(bossLocId);

            String nameKey    = "entity." + entityId.getNamespace() + "." + entityId.getPath();
            String displayName = Text.translatable(nameKey).getString();

            APHudState.bossKills.put(displayName, killed);
            if (killed) APHudState.bossKillsChecked++;

            // Track which bosses are required win-condition targets
            if (APSession.hasSlotData()
                    && APSession.getSlotData().isBossRequired(entityId.getPath())) {
                APHudState.requiredBossDisplayNames.add(displayName);
            }
        }

        // ── Lootable checks ───────────────────────────────────────────────────
        APHudState.lootableChecksTotal = APSession.hasSlotData()
                ? APSession.getSlotData().getLootableChecks() : 0;
        APHudState.lootableChecksFound = locs.countCheckedInRange(
                SlotData.LOOTABLE_CHECK_BASE_ID,
                SlotData.LOOTABLE_CHECK_BASE_ID + APHudState.lootableChecksTotal
        );

        // ── Advancement counts ────────────────────────────────────────────────
        // Advancements = everything that isn't a boss kill or lootable check
        APHudState.advancementsChecked = APHudState.locationsChecked
                - APHudState.bossKillsChecked
                - APHudState.lootableChecksFound;
        APHudState.advancementsTotal = APHudState.locationsTotal;

        // ── Goal percent (for advancement bar marker) ─────────────────────────
        if (APSession.hasSlotData()) {
            APHudState.goalPercent = APSession.getSlotData().getAdvancementGoalPercent();
        }

        // ── Win conditions (from registry) ────────────────────────────────────
        if (APSession.hasSlotData()) {
            APHudState.activeConditions = new ArrayList<>(
                    VictoryConditionRegistry.getActiveProgress(
                            server, locs, APSession.getSlotData()
                    )
            );
        } else {
            APHudState.activeConditions = new ArrayList<>();
        }

        // ── Equipment ─────────────────────────────────────────────────────────
        ServerPlayerEntity serverPlayer =
                server.getPlayerManager().getPlayer(client.player.getUuid());
        if (serverPlayer != null) {
            Set<Identifier> unlocked = StageUnlockState.get(server)
                    .getUnlocked(serverPlayer.getUuid());
            APHudState.stagesUnlocked = Math.max(0, unlocked.size() - 1);

            final int DARK = 0xFF555555;

            if      (unlocked.contains(id("armor/netherite_armor")))
            { APHudState.armorTier = "Netherite"; APHudState.armorColor = 0xFF550000; }
            else if (unlocked.contains(id("armor/diamond_armor")))
            { APHudState.armorTier = "Diamond";   APHudState.armorColor = 0xFF55FFFF; }
            else if (unlocked.contains(id("armor/iron_armor")))
            { APHudState.armorTier = "Iron";       APHudState.armorColor = 0xFFFFFFFF; }
            else if (unlocked.contains(id("armor/leather_armor")))
            { APHudState.armorTier = "Leather";    APHudState.armorColor = 0xFFFFAA00; }
            else
            { APHudState.armorTier = "None";       APHudState.armorColor = DARK; }

            if      (unlocked.contains(id("tools/netherite_tools")))
            { APHudState.toolTier = "Netherite"; APHudState.toolColor = 0xFF550000; }
            else if (unlocked.contains(id("tools/diamond_tools")))
            { APHudState.toolTier = "Diamond";   APHudState.toolColor = 0xFF55FFFF; }
            else if (unlocked.contains(id("tools/iron_tools")))
            { APHudState.toolTier = "Iron";       APHudState.toolColor = 0xFFFFFFFF; }
            else if (unlocked.contains(id("tools/stone_tools")))
            { APHudState.toolTier = "Stone";      APHudState.toolColor = 0xFFAAAAAA; }
            else
            { APHudState.toolTier = "None";       APHudState.toolColor = DARK; }
        }
    }

    // ── Render ────────────────────────────────────────────────────────────────

    private static void render(DrawContext ctx, RenderTickCounter delta) {
        if (!APHudState.visible) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        TextRenderer tr = client.textRenderer;
        int sw = client.getWindow().getScaledWidth();
        int sh = client.getWindow().getScaledHeight();
        int height = computeHeight();

        int[] pos = resolvedPosition(sw, sh, height);
        pos = handleDrag(client, pos[0], pos[1], height);
        int x = pos[0];
        int y = pos[1];

        long    handle = client.getWindow().getHandle();
        boolean altHeld =
                GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_LEFT_ALT)  == GLFW.GLFW_PRESS ||
                        GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_RIGHT_ALT) == GLFW.GLFW_PRESS;

        int borderColor = isDragging ? 0xFFFFAA00
                : altHeld    ? 0xFF4488AA
                : COL_BORDER;

        ctx.fill(x - 1, y - 1, x + PANEL_WIDTH + 1, y + height + 1, borderColor);
        ctx.fill(x,     y,     x + PANEL_WIDTH,     y + height,     COL_BG);

        int cx = x + PAD;
        int cy = y + PAD;
        int barX = cx + 2;
        int barW = PANEL_WIDTH - PAD * 2 - 4;

        // ── Title ─────────────────────────────────────────────────────────────
        ctx.drawTextWithShadow(tr, "⚡ Archipelago", cx, cy, COL_TITLE);
        cy += LINE + 2;
        cy = separator(ctx, cx, cy, x);

        // ── Connection ────────────────────────────────────────────────────────
        if (!APHudState.connected) {
            ctx.drawTextWithShadow(tr, "○ Disconnected", cx, cy, COL_RED);
            return;
        }
        ctx.drawTextWithShadow(tr, "● Connected", cx, cy += 2, COL_GREEN);
        cy += LINE;
        ctx.drawTextWithShadow(tr,
                truncate(tr, APHudState.address, PANEL_WIDTH - PAD * 2), cx + 4, cy, COL_DIM);
        cy += LINE;
        ctx.drawTextWithShadow(tr, "Slot: " + APSession.slotName, cx + 4, cy, COL_WHITE);
        cy += LINE;
        cy = separator(ctx, cx, cy + 2, x);

        // ── Advancements ──────────────────────────────────────────────────────
        ctx.drawTextWithShadow(tr, "Advancements", cx, cy += 2, COL_TITLE);
        cy += LINE;

        ctx.drawTextWithShadow(tr,
                APHudState.advancementsChecked + " / " + APHudState.advancementsTotal,
                cx + 4, cy, COL_WHITE);
        cy += LINE;

        // Bar — fills based on advancements checked vs total
        int advFill = APHudState.advancementsTotal > 0
                ? (int)(APHudState.progressFraction() * barW) : 0;
        ctx.fill(barX, cy, barX + barW, cy + BAR_HEIGHT, COL_BAR_BG);
        if (advFill > 0)
            ctx.fill(barX, cy, barX + advFill, cy + BAR_HEIGHT, COL_BAR_FILL);
        cy += BAR_HEIGHT + 4;
        cy = separator(ctx, cx, cy + 2, x);

        // ── Boss Kills ────────────────────────────────────────────────────────
        ctx.drawTextWithShadow(tr, "Boss Kills", cx, cy += 2, COL_TITLE);
        cy += LINE;

        for (var entry : APHudState.bossKills.entrySet()) {
            boolean required = APHudState.requiredBossDisplayNames.contains(entry.getKey());
            drawBossLine(ctx, tr, cx, cy, entry.getKey(), entry.getValue(), required);
            cy += LINE;
        }

        int bossColor = APHudState.bossKillsChecked == APHudState.BOSS_KILLS_TOTAL
                ? COL_YELLOW : COL_DIM;
        ctx.drawTextWithShadow(tr,
                APHudState.bossKillsChecked + " / " + APHudState.BOSS_KILLS_TOTAL + " killed",
                cx + 4, cy, bossColor);
        cy += LINE;

        // ── Lootable Checks (only if any exist in this game) ─────────────────
        if (APHudState.lootableChecksTotal > 0) {
            cy = separator(ctx, cx, cy + 2, x);
            ctx.drawTextWithShadow(tr, "Lootable Checks", cx, cy += 2, COL_TITLE);
            cy += LINE;

            ctx.drawTextWithShadow(tr,
                    APHudState.lootableChecksFound + " / " + APHudState.lootableChecksTotal,
                    cx + 4, cy, COL_WHITE);
            cy += LINE;

            int lootFill = APHudState.lootableChecksTotal > 0
                    ? (int)((float) APHudState.lootableChecksFound / APHudState.lootableChecksTotal * barW)
                    : 0;
            int lootCol = APHudState.lootableChecksFound >= APHudState.lootableChecksTotal
                    ? COL_BAR_WIN : COL_BAR_FILL;
            ctx.fill(barX, cy, barX + barW, cy + BAR_HEIGHT, COL_BAR_BG);
            if (lootFill > 0) ctx.fill(barX, cy, barX + lootFill, cy + BAR_HEIGHT, lootCol);
            cy += BAR_HEIGHT + 4;
        }

        // ── Win Conditions (only when active conditions exist) ────────────────
        if (!APHudState.activeConditions.isEmpty()) {
            cy = separator(ctx, cx, cy + 2, x);
            ctx.drawTextWithShadow(tr, "Win Conditions", cx, cy += 2, COL_TITLE);
            cy += LINE;

            for (VictoryProgress cond : APHudState.activeConditions) {
                String icon      = cond.met() ? "✓" : "✗";
                int    iconColor = cond.met() ? COL_GREEN : COL_RED;
                String counts    = cond.current() + "/" + cond.required();

                // Label + icon on left, counts on right
                ctx.drawTextWithShadow(tr, icon, cx + 2, cy, iconColor);
                ctx.drawTextWithShadow(tr, cond.label(), cx + 14, cy,
                        cond.met() ? COL_DIM : COL_WHITE);
                ctx.drawTextWithShadow(tr, counts,
                        x + PANEL_WIDTH - PAD - tr.getWidth(counts), cy, COL_DIM);
                cy += LINE;

                // Bar fills to current/required
                int condFill = cond.required() > 0
                        ? (int)(cond.barFraction() * barW) : 0;
                int condCol  = cond.met() ? COL_BAR_WIN : COL_BAR_FILL;
                ctx.fill(barX, cy, barX + barW, cy + BAR_HEIGHT, COL_BAR_BG);
                if (condFill > 0)
                    ctx.fill(barX, cy, barX + condFill, cy + BAR_HEIGHT, condCol);

                cy += BAR_HEIGHT + 4;
            }
        }

        // ── Equipment ─────────────────────────────────────────────────────────
        cy = separator(ctx, cx, cy + 2, x);
        ctx.drawTextWithShadow(tr, "Armor:", cx, cy += 2, COL_TITLE);
        cy += LINE;
        ctx.drawTextWithShadow(tr, APHudState.armorTier, cx + 4, cy, APHudState.armorColor);
        cy += LINE;
        ctx.drawTextWithShadow(tr, "Tools:", cx, cy, COL_TITLE);
        cy += LINE;
        ctx.drawTextWithShadow(tr, APHudState.toolTier, cx + 4, cy, APHudState.toolColor);
    }

    // ── Height calculation ────────────────────────────────────────────────────

    private static int computeHeight() {
        int h = PAD;

        // Title + separator
        h += LINE + 2 + 4;

        // Disconnected — only one line
        if (!APHudState.connected) return h + LINE + PAD;

        // Connection (3 lines + separator)
        h += 2 + LINE * 3 + 6;

        // Advancements (header + count + bar + separator)
        h += 2 + LINE + LINE + BAR_HEIGHT + 4 + 6;

        // Boss kills (header + 4 boss lines + counter, NO separator — separator added by next block)
        h += 2 + LINE + LINE * APHudState.BOSS_KILLS_TOTAL + LINE;

        // Lootable checks — only if any exist
        if (APHudState.lootableChecksTotal > 0) {
            h += 6;                           // separator
            h += 2 + LINE + LINE + BAR_HEIGHT + 4; // header + count + bar
        }

        // Win conditions — only if any active
        if (!APHudState.activeConditions.isEmpty()) {
            h += 6;           // separator
            h += 2 + LINE;    // header
            for (VictoryProgress ignored : APHudState.activeConditions) {
                h += LINE;            // label row
                h += BAR_HEIGHT + 4;  // bar row
            }
        }

        // Equipment (separator + armor + tools = separator + 4 lines)
        h += 6 + 2 + LINE + LINE + LINE + LINE;

        return h + PAD;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Finds an active win condition by label. Returns null if not active. */
    @Nullable
    private static VictoryProgress findCondition(String label) {
        for (VictoryProgress p : APHudState.activeConditions) {
            if (p.label().equals(label)) return p;
        }
        return null;
    }

    private static void drawBossLine(DrawContext ctx, TextRenderer tr,
                                     int cx, int cy, String name,
                                     boolean killed, boolean required) {
        String icon;
        int    color;
        if (killed) {
            icon  = "✓";
            color = required ? COL_GREEN : 0xFF228B22; // bright green if required, dim if not
        } else if (required) {
            icon  = "✦";   // required but not yet killed
            color = COL_YELLOW;
        } else {
            icon  = "○";
            color = COL_DIM;
        }
        ctx.drawTextWithShadow(tr, icon + " " + name, cx + 4, cy, color);
    }

    private static int separator(DrawContext ctx, int cx, int cy, int panelX) {
        ctx.fill(cx, cy, panelX + PANEL_WIDTH - PAD, cy + 1, COL_BORDER);
        return cy + 4;
    }

    private static String truncate(TextRenderer tr, String text, int maxWidth) {
        if (tr.getWidth(text) <= maxWidth) return text;
        while (!text.isEmpty() && tr.getWidth(text + "…") > maxWidth)
            text = text.substring(0, text.length() - 1);
        return text + "…";
    }

    private static Identifier id(String path) {
        return Identifier.of("minecraftarchipelago", path);
    }

    // ── Position persistence ──────────────────────────────────────────────────

    private static void loadPosition() {
        Path path = FabricLoader.getInstance().getConfigDir()
                .resolve("minecraftarchipelago_hud.json");
        if (!Files.exists(path)) return;
        try {
            JsonObject obj = new Gson().fromJson(Files.readString(path), JsonObject.class);
            if (obj.has("x"))       savedX              = obj.get("x").getAsInt();
            if (obj.has("y"))       savedY              = obj.get("y").getAsInt();
            if (obj.has("visible")) APHudState.visible  = obj.get("visible").getAsBoolean();
        } catch (Exception e) {
            MinecraftArchipelago.LOGGER.warn("[AP HUD] Could not load config: {}", e.getMessage());
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

    private static int[] resolvedPosition(int sw, int sh, int h) {
        int x = (savedX >= 0) ? savedX : sw - PANEL_WIDTH - MARGIN;
        int y = (savedY >= 0) ? savedY : MARGIN;
        return new int[]{
                Math.max(0, Math.min(x, sw - PANEL_WIDTH)),
                Math.max(0, Math.min(y, sh - h))
        };
    }

    private static int[] handleDrag(MinecraftClient client, int px, int py, int h) {
        if (client.currentScreen != null) { prevMouseBtn = false; return new int[]{px, py}; }

        long    handle    = client.getWindow().getHandle();
        boolean mouseDown = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean altHeld   = GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_LEFT_ALT)  == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_RIGHT_ALT) == GLFW.GLFW_PRESS;
        int sw = client.getWindow().getScaledWidth();
        int sh = client.getWindow().getScaledHeight();
        int mx = (int)(client.mouse.getX() * sw / client.getWindow().getWidth());
        int my = (int)(client.mouse.getY() * sh / client.getWindow().getHeight());

        if (mouseDown && !prevMouseBtn && altHeld)
            if (mx >= px && mx <= px + PANEL_WIDTH && my >= py && my <= py + h) {
                isDragging = true; dragOffsetX = mx - px; dragOffsetY = my - py;
            }

        if (isDragging && mouseDown) {
            px = Math.max(0, Math.min(mx - dragOffsetX, sw - PANEL_WIDTH));
            py = Math.max(0, Math.min(my - dragOffsetY, sh - h));
            savedX = px; savedY = py;
        }

        if (!mouseDown && prevMouseBtn && isDragging) { isDragging = false; saveConfig(); }
        prevMouseBtn = mouseDown;
        return new int[]{px, py};
    }

    private APHudRenderer() {}
}

