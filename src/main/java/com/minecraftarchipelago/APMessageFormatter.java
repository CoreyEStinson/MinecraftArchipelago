package com.minecraftarchipelago;

import io.github.archipelagomw.Print.APPrint;
import io.github.archipelagomw.Print.APPrintColor;
import io.github.archipelagomw.Print.APPrintPart;
import io.github.archipelagomw.flags.NetworkItem;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.awt.Color;
import java.util.Map;

public final class APMessageFormatter {
    // Priority-hint colors
    private static final Map<String, Integer> GRAB_TYPES = Map.of(
            "(priority)", 0xAF99EF,
            "(no priority)", 0x6D8BE8,
            "(avoid)", 0xE9786B
    );

    public static Text build(APClient client, APPrint print) {
        if (print == null || print.parts == null) {
            return Text.empty();
        }

        MutableText message = Text.empty();

        for (APPrintPart part : print.parts) {
            if (part == null || part.text == null || part.text.isEmpty()) {
                continue;
            }

            APPrintColor partColor =
                    part.color == null ? APPrintColor.none : part.color;

            boolean underlined = partColor == APPrintColor.underline;
            boolean bold = partColor == APPrintColor.bold;

            int rgb = GRAB_TYPES.getOrDefault(
                    part.text,
                    getTextColor(client, part, partColor)
            );

            Style style = Style.EMPTY
                    .withColor(rgb)
                    .withUnderline(underlined)
                    .withBold(bold);

            message.append(Text.literal(part.text).setStyle(style));
        }

        return message;
    }

    private static int getTextColor(
            APClient client,
            APPrintPart part,
            APPrintColor partColor
    ) {
        if (partColor == APPrintColor.none && part.type != null) {
            return switch (part.type) {
                case playerID, playerName ->
                        client != null && client.getMyName().equals(part.text)
                                ? 0xEE00EE  // Current player: magenta
                                : 0xFAFAD2; // Other player: pale yellow

                case locationID, locationName ->
                        0x00FF7F; // Spring green

                case entranceName ->
                        0x6495ED; // Cornflower blue

                case itemID, itemName ->
                        getItemColor(part.flags);

                default ->
                        0xFFFFFF;
            };
        }

        // Explicit protocol colors; background colors are ignored.
        if (partColor.name().endsWith("_bg")) {
            return 0xFFFFFF;
        }

        Color color = partColor.color;
        return color.getRGB() & 0xFFFFFF;
    }

    private static int getItemColor(int flags) {
        if (hasFlag(flags, NetworkItem.ADVANCEMENT)) {
            return 0xAF99EF; // Progression: lavender
        }

        if (hasFlag(flags, NetworkItem.TRAP)) {
            return 0xE9786B; // Trap: salmon
        }

        return 0x6D8BE8; // Filler/useful: blue
    }

    private static boolean hasFlag(int flags, int flag) {
        return (flags & flag) == flag;
    }

    private APMessageFormatter() {
    }
}