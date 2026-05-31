package com.minecraftarchipelago;

import io.github.archipelagomw.flags.NetworkItem;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts Archipelago PrintJSON messages to colored Minecraft Text,
 * matching the color scheme used by the Archipelago text client:
 * <p>
 *   Player names   → YELLOW
 *   Items          → AQUA  (progression/unknown — BLUE if useful, RED if trap)
 *   Location names → GREEN
 *   Hints          → GOLD
 *   Join/Part      → GRAY
 *   [AP] prefix    → GOLD
 */
public class APMessageFormatter {

    // --- Patterns ---
    private static final Pattern PAT_FOUND = Pattern.compile(
            "^(.+?) found (.+) \\(([^)]+)\\)(.*)$");

    private static final Pattern PAT_SENT = Pattern.compile(
            "^(.+?) sent (.+?) to (.+?) \\(([^)]+)\\)(.*)$");

    // "Hint: ..."
    private static final Pattern PAT_HINT = Pattern.compile(
            "^Hint: (.+)$", Pattern.DOTALL);

    // "PlayerName (GameName) has joined/left/finished..."
    private static final Pattern PAT_STATUS = Pattern.compile(
            "^(.+?) \\(.+?\\) has (?:joined|left|finished).*$");

    // "[Tag]: message" — server/admin/command output
    private static final Pattern PAT_SERVER = Pattern.compile(
            "^(\\[.+?\\]: )(.*)$", Pattern.DOTALL);

    // "PlayerName: chat message" — checked last to avoid catching join/part
    private static final Pattern PAT_CHAT = Pattern.compile(
            "^(\\S[^:]*): (.+)$", Pattern.DOTALL);

    // --- Public Entry Point ---

    /**
     * Builds a colored Minecraft Text from the plain AP message string.
     */
    public static Text build(String plain, int itemFlags) {
        if (plain == null || plain.isBlank()) return Text.empty();
        Formatting itemColor = itemColor(itemFlags);

        MutableText out = Text.empty();
        out.append(Text.literal("[AP] ").formatted(Formatting.GOLD));

        Matcher sent = PAT_SENT.matcher(plain);
        if (sent.matches()) {
            add(out, sent.group(1), Formatting.YELLOW);
            add(out, " sent ",      Formatting.WHITE);
            add(out, sent.group(2), itemColor);
            add(out, " to ",        Formatting.WHITE);
            add(out, sent.group(3), Formatting.YELLOW);
            add(out, " (",          Formatting.GRAY);
            add(out, sent.group(4), Formatting.GREEN);
            add(out, ")",           Formatting.GRAY);
            tail(out, sent.group(5));
            return out;
        }

        Matcher found = PAT_FOUND.matcher(plain);
        if (found.matches()) {
            add(out, found.group(1), Formatting.YELLOW);
            add(out, " found ",      Formatting.WHITE);
            add(out, found.group(2), itemColor);
            add(out, " (",           Formatting.GRAY);
            add(out, found.group(3), Formatting.GREEN);
            add(out, ")",            Formatting.GRAY);
            tail(out, found.group(4));
            return out;
        }

        Matcher hint = PAT_HINT.matcher(plain);
        if (hint.matches()) {
            add(out, "Hint: ",      Formatting.GOLD);
            add(out, hint.group(1), Formatting.WHITE);
            return out;
        }

        Matcher status = PAT_STATUS.matcher(plain);
        if (status.matches()) {
            String name = status.group(1);
            add(out, name,                              Formatting.YELLOW);
            add(out, plain.substring(plain.indexOf(name) + name.length()), Formatting.GRAY);
            return out;
        }

        Matcher server = PAT_SERVER.matcher(plain);
        if (server.matches()) {
            add(out, server.group(1), Formatting.GRAY);
            add(out, server.group(2), Formatting.WHITE);
            return out;
        }

        Matcher chat = PAT_CHAT.matcher(plain);
        if (chat.matches()) {
            add(out, chat.group(1), Formatting.YELLOW);
            add(out, ": ",          Formatting.GRAY);
            add(out, chat.group(2), Formatting.WHITE);
            return out;
        }

        add(out, plain, Formatting.WHITE);
        return out;
    }

    public static Text build(String plain) {
        return build(plain, -1);
    }

    // --- Helpers ---

    /**
     * Maps NetworkItem flags to the matching AP text client color.
     * <p>
     *   TRAP        → RED   (traps stand out as dangerous)
     *   USEFUL      → BLUE  (important but not progression-critical)
     *   ADVANCEMENT → AQUA  (progression items — the most notable)
     *   filler/none → WHITE (everything else is quiet)
     *   unknown (-1)→ AQUA  (no info, default to progression color)
     */
    private static Formatting itemColor(int flags) {
        if (flags < 0)                                    return Formatting.AQUA;
        if ((flags & NetworkItem.TRAP) != 0)              return Formatting.RED;
        if ((flags & NetworkItem.USEFUL) != 0)            return Formatting.BLUE;
        if ((flags & NetworkItem.ADVANCEMENT) != 0)       return Formatting.AQUA;
        return Formatting.WHITE; // filler
    }

    private static void add(MutableText out, String text, Formatting fmt) {
        if (text != null && !text.isEmpty()) {
            out.append(Text.literal(text).formatted(fmt));
        }
    }

    private static void tail(MutableText out, String text) {
        if (text != null && !text.isBlank()) {
            add(out, text, Formatting.GRAY);
        }
    }

    private APMessageFormatter() {}
}
