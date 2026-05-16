package com.minecraftarchipelago;

public final class APSession {
    public static final APClient CLIENT = new APClient();
    private static boolean listenerRegistered = false;

    // Null until a successful connection + slot data recived
    private static SlotData slotData = null;

    public static String slotName = "-";

    public static SlotData getSlotData(){
        return  slotData;
    }

    public static void setSlotData(SlotData data){
        slotData = data;
    }

    public static void clearSlotData(){
        slotData = null;
    }

    public static boolean hasSlotData(){
        return slotData != null;
    }

    private static String pendingHost = null;
    private static String pendingPort = null;
    private static String pendingSlot = null;
    private static String pendingPassword = null;

    public static void setPendingCredentials(
            String host, String port, String slot, String password
    ) {
        pendingHost = host;
        pendingPort = port;
        pendingSlot = slot;
        pendingPassword = password;
    }

    public static String getPendingHost() { return pendingHost; }
    public static String getPendingPort() { return pendingPort; }
    public static String getPendingSlot() { return pendingSlot; }
    public static String getPendingPassword() { return pendingPassword; }

    public static void ensureListeners() {
        if (listenerRegistered) return;
        CLIENT.getEventManager().registerListener(new APEvents());
        listenerRegistered = true;
    }

    private APSession() {}
}