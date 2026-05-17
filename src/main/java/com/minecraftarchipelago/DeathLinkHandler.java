package com.minecraftarchipelago;

public final class DeathLinkHandler {

    // True while the player is dying FROM a received Death Link
    // The mixin checks this to avoid re-sending
    private static boolean receivingDeathLink = false;

    public static boolean isReceivingDeathLink() { return receivingDeathLink; }

    public static void setReceivingDeathLink(boolean value) { receivingDeathLink = value; }

    private DeathLinkHandler() {}
}
