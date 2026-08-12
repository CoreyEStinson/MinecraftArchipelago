package com.minecraftarchipelago.dashboard;

import com.minecraftarchipelago.APSession;
import com.minecraftarchipelago.apitems.APGiveItemRegistry;
import com.minecraftarchipelago.apitems.APItemRegistry;
import io.github.archipelagomw.parts.NetworkItem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

import java.util.List;

public final class ReceiptHistoryRecorder {
    public static void record(
            MinecraftServer server,
            long apItemId,
            int receiptIndex,
            String displayName,
            String sender
    ) {
        APGiveItemRegistry.GiveEntry gift = APGiveItemRegistry.getEntry(apItemId);
        ReceiptKind kind = gift == null ? ReceiptKind.UNLOCK : ReceiptKind.GIFT;

        String slotName = APSession.slotName == null ? "" : APSession.slotName;
        String safeName = displayName == null || displayName.isBlank()
                ? "Unknown item"
                : displayName;
        String safeSender = sender == null ? "" : sender;

        ReceiptHistoryState.get(server).append(new ReceiptRecord(
                slotName,
                receiptIndex,
                kind,
                safeName,
                gift == null ? getUnlockIcon(apItemId) : gift.itemId().toString(),
                gift == null ? 1 : gift.count(),
                safeSender
        ));
    }

    public static void importCurrentSession(MinecraftServer server) {
        if (APSession.CLIENT == null) {
            return;
        }

        List<NetworkItem> receivedItems =
                APSession.CLIENT.getItemManager().getReceivedItems();

        for (int index = 0; index < receivedItems.size(); index++) {
            NetworkItem item = receivedItems.get(index);

            record(
                    server,
                    item.itemID,
                    index + 1,
                    item.itemName,
                    item.playerName
            );
        }
    }

    private static String getUnlockIcon(long apItemId) {
        Identifier stageId = APItemRegistry.getStageId(apItemId);

        if (stageId != null) {
            String path = stageId.getPath();

            String groupedItemIcon = switch (path) {
                case "items/boat" -> "minecraft:oak_boat";
                case "items/turtle_shell" -> "minecraft:turtle_helmet";
                case "blocks/bed" -> "minecraft:red_bed";
                default -> null;
            };

            if (groupedItemIcon != null) {
                return groupedItemIcon;
            }

            if (path.startsWith("items/")) {
                return "minecraft:" + path.substring("items/".length());
            }

            if (path.startsWith("blocks/")) {
                return "minecraft:" + path.substring("blocks/".length());
            }

            if (path.startsWith("gamerules/")) {
                return "minecraft:command_block";
            }
        }

        if (APItemRegistry.isProgressive(apItemId)) {
            return apItemId == 43000L
                    ? "minecraft:iron_pickaxe"
                    : "minecraft:iron_chestplate";
        }

        return "minecraft:barrier";
    }

    private ReceiptHistoryRecorder() {
    }
}
