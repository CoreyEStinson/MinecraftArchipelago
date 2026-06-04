package com.minecraftarchipelago.aplocations;

import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class CheckedLocationsState extends PersistentState{

    // Saved to <world>/data/ap_checked_loactions.dat
    public static final String DATA_ID = "ap_checked_locations";

    public static final PersistentState.Type<CheckedLocationsState> TYPE =
            new PersistentState.Type<>(
                    CheckedLocationsState::new,
                    CheckedLocationsState::fromNbt,
                    DataFixTypes.LEVEL
            );

    private final Set<Long> checkedIds = new HashSet<>();
    private boolean goalAchieved = false;
    private int lastReceivedItemIndex = -1;

    public static CheckedLocationsState get(MinecraftServer server){
        ServerWorld overworld = server.getOverworld();
        PersistentStateManager psm = overworld.getPersistentStateManager();
        return  psm.getOrCreate(TYPE, DATA_ID);
    }

    private static CheckedLocationsState fromNbt(
            NbtCompound nbt,
            RegistryWrapper.WrapperLookup registries
    ) {
        CheckedLocationsState state = new CheckedLocationsState();
        long[] saved = nbt.getLongArray("checked");
        for (long id : saved){
            state.checkedIds.add(id);
        }
        state.goalAchieved = nbt.getBoolean("goal_achieved");
        state.lastReceivedItemIndex = nbt.contains("lastReceivedItemIndex")
                ? nbt.getInt("lastReceivedItemIndex")
                : -1;
        return state;
    }

    @Override
    public NbtCompound writeNbt(
            NbtCompound nbt,
            RegistryWrapper.WrapperLookup registries
    ){
        long[] array = checkedIds.stream()
                .mapToLong(Long::longValue)
                .toArray();
        nbt.putLongArray("checked", array);
        nbt.putBoolean("goal_achieved", goalAchieved);
        nbt.putInt("lastReceivedItemIndex", lastReceivedItemIndex);
        return nbt;
    }

    /**
     * Marks a location as checked.
     * Returns true if this is the first time (should send to AP).
     * Returns false if it was already checked (skip sending).
     */
    public boolean checkLocation(long locationId){
        boolean added = checkedIds.add(locationId);
        if (added) markDirty();
        return added;
    }

    public Set<Long> getAllChecked() {
        return Collections.unmodifiableSet(checkedIds);
    }

    public int checkedCount(){
        return checkedIds.size();
    }

    /**
     * Marks the goal as achieved. Returns true if this is the first time
     * (caller should send goalAchieved to AP). Returns false if already done.
     */
    public boolean markGoalAchieved() {
        if (goalAchieved) return false;
        goalAchieved = true;
        markDirty();
        return true;
    }

    public boolean isGoalAchieved() {
        return goalAchieved;
    }

    public boolean isNewItem(int index) {
        if (index <= lastReceivedItemIndex) return false;
        lastReceivedItemIndex = index;
        markDirty();
        return true;
    }

    public boolean isLocationChecked(long locationId) {
        return checkedIds.contains(locationId);
    }

    public int countCheckedInRange(long minIdInclusive, long maxIdExclusive) {
        int count = 0;
        for (long id = minIdInclusive; id < maxIdExclusive; id++) {
            if (checkedIds.contains(id)) count++;
        }
        return count;
    }

    public void resetForTests() {
        checkedIds.clear();
        goalAchieved = false;
        lastReceivedItemIndex = -1;
        markDirty();
    }
}
