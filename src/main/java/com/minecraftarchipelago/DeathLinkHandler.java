package com.minecraftarchipelago;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class DeathLinkHandler {

    // True while the player is dying FROM a received Death Link
    // The mixin checks this to avoid re-sending
    private static boolean receivingDeathLink = false;

    public static boolean isReceivingDeathLink() { return receivingDeathLink; }

    public static void setReceivingDeathLink(boolean value) { receivingDeathLink = value; }

    public static boolean maybeSendDeathLink(ServerPlayerEntity player) {
        if (receivingDeathLink) return false;
        if (!APSession.client().isConnected()) return false;
        if (!APSession.hasSlotData()) return false;
        if (!APSession.getSlotData().isDeathLinkEnabled()) return false;

        String cause = player.getName().getString() + " died in Minecraft";
        APSession.runtime().executeOnClient(() ->
                APSession.client().sendDeathLink(APSession.getPendingSlot(), cause)
        );
        return true;
    }

    public static boolean applyReceivedDeathLink(ServerPlayerEntity player, String source) {
        if (player == null || player.isDead()) return false;

        receivingDeathLink = true;
        try {
            player.damage(player.getDamageSources().outOfWorld(), Float.MAX_VALUE);
        } finally {
            receivingDeathLink = false;
        }

        APSession.runtime().executeOnClient(() -> {
            var localPlayer = APSession.runtime().getCurrentPlayer();
            if (localPlayer != null) {
                localPlayer.sendMessage(
                        Text.literal("[AP] Death Link received. Sent by " + source)
                );
            }
        });
        return true;
    }

    private DeathLinkHandler() {}
}
