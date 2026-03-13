package com.minecraftarchipelago.apstages.state;

import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;

import java.util.*;

public final class StageUnlockState extends PersistentState
{
    // This becomes: <world>/data/apstages_unlocked.dat
    public static final String DATA_ID = "apstages_unlocked";
    
    // Tells Minecraft how to create and load this state
    public static final PersistentState.Type<StageUnlockState> TYPE =
        new PersistentState.Type<>(
            StageUnlockState::new,        // "new empty state"
            StageUnlockState::fromNbt,    // "load from disk"
            DataFixTypes.LEVEL            // data-fixer category
        );
    
    private final Map<UUID, Set<Identifier>> unlocked = new HashMap<>();
    
    public static StageUnlockState get(MinecraftServer server) {
        ServerWorld overworld = server.getOverworld();
        PersistentStateManager psm = overworld.getPersistentStateManager();
        return psm.getOrCreate(TYPE, DATA_ID);
    }
    
    private static StageUnlockState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        StageUnlockState state = new StageUnlockState();
        
        NbtList players = nbt.getList("players", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < players.size(); i++) {
            NbtCompound entry = players.getCompound(i);
            
            UUID uuid = entry.getUuid("uuid");
            
            NbtList stages = entry.getList("stages", NbtElement.STRING_TYPE);
            Set<Identifier> set = new HashSet<>();
            for (int j = 0; j < stages.size(); j++) {
                Identifier id = Identifier.tryParse(stages.getString(j));
                if (id != null) set.add(id);
            }
            
            state.unlocked.put(uuid, set);
        }
        
        return state;
    }
    
    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        NbtList players = new NbtList();
        
        for (var e : unlocked.entrySet()) {
            NbtCompound entry = new NbtCompound();
            entry.putUuid("uuid", e.getKey());
            
            NbtList stages = new NbtList();
            for (Identifier id : e.getValue()) {
                stages.add(NbtString.of(id.toString()));
            }
            
            entry.put("stages", stages);
            players.add(entry);
        }
        
        nbt.put("players", players);
        return nbt;
    }
    
    public boolean unlock(UUID player, Identifier stage) {
        Set<Identifier> set = unlocked.computeIfAbsent(player, u -> new HashSet<>());
        boolean added = set.add(stage);
        if (added) markDirty(); // tells MC “please save me”
        return added;
    }
    
    public Set<Identifier> getUnlocked(UUID player) {
        return Collections.unmodifiableSet(unlocked.getOrDefault(player, Set.of()));
    }
    
    public boolean reset(UUID player) {
        boolean removed = unlocked.remove(player) != null;
        if (removed) markDirty();
        return removed;
    }
}
