package com.minecraftarchipelago.aplocations;

import com.minecraftarchipelago.SlotData;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;

public class LootableCheckState extends PersistentState {

    private static final String SAVE_ID = "ap_lootable_checks";
    private static final String NBT_SLOT = "next_slot";

    private static final Type<LootableCheckState> TYPE = new Type<>(
            LootableCheckState::new,
            LootableCheckState::fromNbt,
            null
    );

    public static LootableCheckState get(MinecraftServer server) {
        PersistentStateManager manager = server.getOverworld()
                .getPersistentStateManager();
        return manager.getOrCreate(TYPE, SAVE_ID);
    }

    /**
     * Index of the next slot to assign (0-based).
     * Increments each time assignNext() succeeds.
     * Persisted across sessions so assignments survive restarts.
     */
    private int nextSlot = 0;

    /**
     * Assigns the next available lootable check location ID and advances
     * the internal pointer.
     * <p>
     * Returns the location ID (e.g. 42500, 42501 …) if a slot is available.
     * Returns -1 if all slots for this game have already been assigned
     * (the item should be marked surplus in this case).
     * <p>
     * Call with APSession.getSlotData().getLootableChecks() as poolSize.
     */
    public long assignNext(int poolSize) {
        if (nextSlot >= poolSize) return -1; // pool exhausted

        long locationId = SlotData.LOOTABLE_CHECK_BASE_ID + nextSlot;
        nextSlot++;
        markDirty();
        return locationId;
    }

    /**
     *  How many slots have been assigned so far
     */
    public int getAssignedCount() {
        return nextSlot;
    }

    /**
     * True if no more slots are available for the given pool size.
     */
    public boolean isExhausted(int poolSize) {
        return nextSlot >= poolSize;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt,
                                RegistryWrapper.WrapperLookup registries) {
        nbt.putInt(NBT_SLOT, nextSlot);
        return nbt;
    }

    public static LootableCheckState fromNbt(NbtCompound nbt,
                                             RegistryWrapper.WrapperLookup registries) {
        LootableCheckState state = new LootableCheckState();
        state.nextSlot = nbt.getInt(NBT_SLOT);
        return state;
    }

    public void resetForTests() {
        nextSlot = 0;
        markDirty();
    }
}
