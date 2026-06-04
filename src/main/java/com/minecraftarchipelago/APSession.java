package com.minecraftarchipelago;

import com.minecraftarchipelago.facades.ArchipelagoClientFacade;
import com.minecraftarchipelago.facades.DefaultArchipelagoClientFacade;
import com.minecraftarchipelago.facades.DefaultMinecraftRuntimeFacade;
import com.minecraftarchipelago.facades.MinecraftRuntimeFacade;
import com.minecraftarchipelago.facades.NoOpArchipelagoClientFacade;
import com.minecraftarchipelago.facades.ServerSafeRuntimeFacade;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

public final class APSession {
    public static final APClient CLIENT = isClientEnvironment() ? new APClient() : null;
    private static boolean listenerRegistered = false;
    private static ArchipelagoClientFacade clientFacade = createDefaultClientFacade();
    private static MinecraftRuntimeFacade runtimeFacade = createDefaultRuntimeFacade();

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
        if (listenerRegistered || CLIENT == null) return;
        CLIENT.getEventManager().registerListener(new APEvents());
        listenerRegistered = true;
    }

    public static ArchipelagoClientFacade client() {
        return clientFacade;
    }

    public static MinecraftRuntimeFacade runtime() {
        return runtimeFacade;
    }

    static void setClientFacadeForTests(ArchipelagoClientFacade facade) {
        clientFacade = facade;
    }

    static void setRuntimeFacadeForTests(MinecraftRuntimeFacade facade) {
        runtimeFacade = facade;
    }

    static void resetForTests() {
        listenerRegistered = false;
        slotData = null;
        slotName = "-";
        pendingHost = null;
        pendingPort = null;
        pendingSlot = null;
        pendingPassword = null;
        clientFacade = createDefaultClientFacade();
        runtimeFacade = createDefaultRuntimeFacade();
    }

    private APSession() {}

    private static boolean isClientEnvironment() {
        try {
            return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private static ArchipelagoClientFacade createDefaultClientFacade() {
        return CLIENT != null ? new DefaultArchipelagoClientFacade(CLIENT) : new NoOpArchipelagoClientFacade();
    }

    private static MinecraftRuntimeFacade createDefaultRuntimeFacade() {
        return isClientEnvironment() ? new DefaultMinecraftRuntimeFacade() : new ServerSafeRuntimeFacade();
    }
}
