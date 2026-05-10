package com.minecraftarchipelago;

public final class APSession {
    public static final APClient CLIENT = new APClient();
    private static boolean listenerRegistered = false;

    // Null until a successful connection + slot data recived
    private static SlotData slotData = null;

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

    public static void ensureListeners() {
        if (listenerRegistered) return;
        CLIENT.getEventManager().registerListener(new APEvents());
        listenerRegistered = true;
    }

    private APSession() {}
}