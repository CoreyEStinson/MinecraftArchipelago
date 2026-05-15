package com.minecraftarchipelago;

import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;

import java.lang.reflect.Type;

public final class APConnectionState extends PersistentState {

    public static final String DATA_ID = "ap_connection";

    public static final Type<APConnectionState> TYPE = new Type<>(
        APConnectionState::new,
        APConnectionState::fromNbt,
        DataFixTypes.LEVEL
    );

    private String host = null;
    private String port = null;
    private String slot = null;
    private String password = null;

    public static APConnectionState get(MinecraftServer server) {
        PersistentStateManager psm = server.getOverworld().getPersistentStateManager();
        return psm.getOrCreate(TYPE, DATA_ID);
    }

    private static APConnectionState fromNbt(
            NbtCompound nbt, RegistryWrapper.WrapperLookup registries
    ) {
        APConnectionState s = new APConnectionState();
        if (nbt.contains("host")) s.host = nbt.getString("host");
        if (nbt.contains("port")) s.port = nbt.getString("port");
        if (nbt.contains("slot")) s.slot = nbt.getString("slot");
        if (nbt.contains("password")) s.password = nbt.getString("password");
        return s;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries){
        if (host != null) nbt.putString("host", host);
        if (port != null) nbt.putString("port", port);
        if (slot != null) nbt.putString("slot", slot);
        if (password!= null) nbt.putString("password", password);
        return nbt;
    }

    public void save(String host, String port, String slot, String password){
        this.host = host;
        this.port = port;
        this.slot = slot;
        this.password = password;
        markDirty();
    }

    public void clear(){
        host = port = slot = password = null;
        markDirty();
    }

    public boolean hasSavedConnection(){
        return host != null && port != null && slot != null;
    }

    public String getHost() { return host; }
    public String getPort() { return port; }
    public String getSlot() { return slot; }
    public String getPassword() { return password; }
}
