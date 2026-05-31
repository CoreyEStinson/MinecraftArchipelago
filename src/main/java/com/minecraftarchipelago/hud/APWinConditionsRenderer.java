package com.minecraftarchipelago.hud;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.minecraftarchipelago.APSession;
import com.minecraftarchipelago.MinecraftArchipelago;
import com.minecraftarchipelago.SlotData;
import com.minecraftarchipelago.aplocations.CheckedLocationsState;
import com.minecraftarchipelago.aplocations.VictoryCondition;
import com.minecraftarchipelago.victory.VictoryConditionRegistry;
import com.minecraftarchipelago.victory.VictoryProgress;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.MinecraftServer;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class APWinConditionsRenderer {

    // --- Layout ---
    private static final int PANEL_WIDTH = 185;
    private static final int MARGIN      = 5;
    private static final int PAD         = 6;
    private static final int LINE        = 12;
    private static final int BAR_HEIGHT  = 10;

    // --- Colors ---
    private static final int COL_BG       = 0xCC0A0A14;
    private static final int COL_BORDER   = 0xFF334455;
    private static final int COL_TITLE    = 0xFFFFAA00;
    private static final int COL_WHITE    = 0xFFFFFFFF;
    private static final int COL_DIM      = 0xFFAAAAAA;
    private static final int COL_FAINT    = 0xFF888888;
    private static final int COL_GREEN    = 0xFF55FF55;
    private static final int COL_RED      = 0xFFFF5555;
    private static final int COL_YELLOW   = 0xFFFFFF55;
    private static final int COL_BAR_BG   = 0xFF222233;
    private static final int COL_BAR_FILL = 0xFF33AA55;
    private static final int COL_BAR_WIN  = 0xFFFFAA00;

    // --- Drag State ---
    private static int     savedX       = -1;
    private static int     savedY       = -1;
    private static boolean isDragging   = false;
    private static int     dragOffsetX  = 0;
    private static int     dragOffsetY  = 0;
    private static boolean prevMouseBtn = false;

    // Registration
    public static void register() {
        loadPosition();
        ClientTickEvents.END_CLIENT_TICK.register(APWinConditionsRenderer::updateState);
        HudRenderCallback.EVENT.register(APWinConditionsRenderer::render);
    }

    // --- State Update ---

    private static void updateState(MinecraftClient client) {
        MinecraftServer server = client.getServer();
        if (server == null || client.player == null) {
            APHudState.activeConditions = new ArrayList<>();
            APHudState.collectionRemainingItems = new LinkedHashMap<>();
            return;
        }

        if (!APSession.hasSlotData()) {
            APHudState.activeConditions = new ArrayList<>();
            APHudState.collectionRemainingItems = new LinkedHashMap<>();
            return;
        }

        SlotData slotData = APSession.getSlotData();
        CheckedLocationsState locs = CheckedLocationsState.get(server);

        // Populate the active conditions list from the registry
        APHudState.activeConditions = new ArrayList<>(
                VictoryConditionRegistry.getActiveProgress(server, locs, slotData)
        );

        // Expanded item details - only computed when panel is open and expanded
        if (APHudState.winConditionsVisible && APHudState.expandedView) {
            APHudState.collectionRemainingItems = new LinkedHashMap<>(
                    VictoryConditionRegistry.getRemainingDetails(server, locs, slotData)
            );
        } else if (!APHudState.expandedView) {
            APHudState.collectionRemainingItems = new LinkedHashMap<>();
        }
    }

    // --- Render ---

    private static void render(DrawContext ctx, RenderTickCounter delta) {
        if (!APHudState.winConditionsVisible) return;

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

        long    handle  = client.getWindow().getHandle();
        boolean altHeld =
                GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_LEFT_ALT)  == GLFW.GLFW_PRESS ||
                        GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_RIGHT_ALT) == GLFW.GLFW_PRESS;

        int borderColor = isDragging ? 0xFFFFAA00 : altHeld ? 0xFF4488AA : COL_BORDER;
        ctx.fill(x - 1, y - 1, x + PANEL_WIDTH + 1, y + height + 1, borderColor);
        ctx.fill(x,     y,     x + PANEL_WIDTH,     y + height,     COL_BG);

        int cx   = x + PAD;
        int cy   = y + PAD;
        int barX = cx + 2;
        int barW = PANEL_WIDTH - PAD * 2 - 4;

        // ── Title ─────────────────────────────────────────────────────────────
        ctx.drawTextWithShadow(tr, "\u26A1 Win Conditions", cx, cy, COL_TITLE);
        cy += LINE + 2;
        cy = separator(ctx, cx, cy, x);

        // ── Empty / disconnected states ───────────────────────────────────────
        if (!APSession.CLIENT.isConnected()) {
            ctx.drawTextWithShadow(tr, "Not connected to Archipelago.", cx, cy, COL_DIM);
            cy += LINE;
            ctx.drawTextWithShadow(tr, "/archipelago join to connect.", cx + 4, cy, COL_FAINT);
            return;
        }

        if (APHudState.activeConditions.isEmpty()) {
            ctx.drawTextWithShadow(tr, "No active win conditions.", cx, cy, COL_DIM);
            return;
        }

        // ── Condition rows ────────────────────────────────────────────────────
        for (VictoryProgress cond : APHudState.activeConditions) {
            String icon      = cond.met() ? "\u2713" : "\u2717"; // ✓ or ✗
            int    iconColor = cond.met() ? COL_GREEN : COL_RED;
            String counts    = cond.current() + "/" + cond.required();

            // Label row: icon | name (left) | X/required (right)
            ctx.drawTextWithShadow(tr, icon, cx + 2, cy, iconColor);
            ctx.drawTextWithShadow(tr,
                    truncate(tr, cond.label(), PANEL_WIDTH - PAD * 2 - 40),
                    cx + 14, cy,
                    cond.met() ? COL_DIM : COL_WHITE);
            ctx.drawTextWithShadow(tr, counts,
                    x + PANEL_WIDTH - PAD - tr.getWidth(counts), cy, COL_DIM);
            cy += LINE;

            // Progress bar — fills current/total, goal marker at required/total
            int condFill = cond.total() > 0 ? (int)(cond.barFraction() * barW) : 0;
            int condCol  = cond.met() ? COL_BAR_WIN : COL_BAR_FILL;
            ctx.fill(barX, cy, barX + barW, cy + BAR_HEIGHT, COL_BAR_BG);
            if (condFill > 0)
                ctx.fill(barX, cy, barX + condFill, cy + BAR_HEIGHT, condCol);

            // Goal marker only when required < total (not all-or-nothing)
            if (cond.total() > 0 && cond.goalFraction() < 1.0f) {
                int gx = barX + (int)(cond.goalFraction() * barW);
                gx = Math.max(barX, Math.min(gx, barX + barW - 1));
                ctx.fill(gx, cy - 1, gx + 1, cy + BAR_HEIGHT + 1, COL_YELLOW);
            }
            cy += BAR_HEIGHT + 4;

            // Expanded item list — only for incomplete conditions that have detail data
            if (APHudState.expandedView && !cond.met()) {
                List<String> remaining =
                        APHudState.collectionRemainingItems.get(cond.label());
                if (remaining != null && !remaining.isEmpty()) {
                    ctx.drawTextWithShadow(tr,
                            "Missing (" + remaining.size() + "):",
                            cx + 4, cy, COL_DIM);
                    cy += LINE;
                    for (String itemName : remaining) {
                        ctx.drawTextWithShadow(tr,
                                " ◦ " + truncate(tr, itemName, PANEL_WIDTH - PAD * 2 - 14),
                                cx + 4, cy, COL_FAINT);
                        cy += LINE;
                    }
                }
            }
        }

        // ── Footer ────────────────────────────────────────────────────────────
        cy = separator(ctx, cx, cy + 2, x);

        long metCount = APHudState.activeConditions.stream()
                .filter(VictoryProgress::met).count();
        String summary = metCount + "/" + APHudState.activeConditions.size() + " complete";
        int summaryColor = (metCount == APHudState.activeConditions.size())
                ? COL_YELLOW : COL_DIM;
        ctx.drawTextWithShadow(tr, summary, cx, cy + 2, summaryColor);

        // Hint for detail toggle aligned to right edge
        String hint = APHudState.expandedView ? "[V] collapse" : "[V] details";
        ctx.drawTextWithShadow(tr, hint,
                x + PANEL_WIDTH - PAD - tr.getWidth(hint), cy + 2, COL_FAINT);
    }

    private static int computeHeight() {
        int h = PAD;
        // Title + separator
        h += LINE + 2 + 4;

        // Disconnected or no conditions: two message lines + padding
        if (!APSession.CLIENT.isConnected() || APHudState.activeConditions.isEmpty()) {
            h += LINE * 2 + PAD;
            return h;
        }

        for (VictoryProgress cond : APHudState.activeConditions) {
            h += LINE;            // label row
            h += BAR_HEIGHT + 4;  // bar row

            if (APHudState.expandedView && !cond.met()) {
                List<String> remaining =
                        APHudState.collectionRemainingItems.get(cond.label());
                if (remaining != null && !remaining.isEmpty()) {
                    h += LINE;                    // "Missing (N):" header
                    h += remaining.size() * LINE; // one line per item
                }
            }
        }

        // Footer: separator + summary line
        h += 6 + LINE;

        return h + PAD;
    }

    // --- Helpers ---

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

    // --- Position Persistence ---

    private static void loadPosition() {
        Path path = FabricLoader.getInstance().getConfigDir()
                .resolve("minecraftarchipelago_wincond.json");
        if (!Files.exists(path)) return;
        try {
            JsonObject obj = new Gson().fromJson(Files.readString(path), JsonObject.class);
            if (obj.has("x")) savedX = obj.get("x").getAsInt();
            if (obj.has("y")) savedY = obj.get("y").getAsInt();
        } catch (Exception e) {
            MinecraftArchipelago.LOGGER.warn(
                    "[AP Win Cond HUD] Could not load config: {}", e.getMessage());
        }
    }

    private static void saveConfig() {
        Path path = FabricLoader.getInstance().getConfigDir()
                .resolve("minecraftarchipelago_wincond.json");
        try {
            JsonObject obj = new JsonObject();
            if (savedX >= 0) obj.addProperty("x", savedX);
            if (savedY >= 0) obj.addProperty("y", savedY);
            Files.writeString(path, new Gson().toJson(obj));
        } catch (Exception e) {
            MinecraftArchipelago.LOGGER.warn(
                    "[AP Win Cond HUD] Could not save config: {}", e.getMessage());
        }
    }

    private static int[] resolvedPosition(int sw, int sh, int h) {
        // Default: top-left corner (main HUD is top-right, so they don't overlap by default)
        int x = (savedX >= 0) ? savedX : MARGIN;
        int y = (savedY >= 0) ? savedY : MARGIN;
        return new int[]{
                Math.max(0, Math.min(x, sw - PANEL_WIDTH)),
                Math.max(0, Math.min(y, sh - h))
        };
    }

    private static int[] handleDrag(MinecraftClient client, int px, int py, int h) {
        if (client.mouse.isCursorLocked()) { prevMouseBtn = false; return new int[]{px, py}; }

        long    handle    = client.getWindow().getHandle();
        boolean mouseDown = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT)
                == GLFW.GLFW_PRESS;
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

    private APWinConditionsRenderer() {}
}
