package com.minecraftarchipelago;

public final class APSession {
    public static final APClient CLIENT = new APClient();
    private static boolean listenerRegistered = false;

    public static void ensureListeners() {
        if (listenerRegistered) return;
        CLIENT.getEventManager().registerListener(new APEvents());
        listenerRegistered = true;
    }
}