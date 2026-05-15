package com.minecraftarchipelago;

public final class DeathLinkHandler {

    // True while the player is dying FROM a received Death Link
    // The mixin checks this to avoid re-sending
    private static boolean recievingDeathLink = false;

    public static boolean isRecievingDeathLink() { return recievingDeathLink; }

    public static void setRecievingDeathLink(boolean value) { recievingDeathLink = value; }

    private DeathLinkHandler() {}
}
