package com.minecraftarchipelago.collections;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Records every item type ever placed into the player's inventory this world.
 * Persists across sessions. Used by ItemCollectionChecker as the source of truth.
 */
public class CollectedItemsState extends PersistentState {

    private static final String SAVE_ID = "ap_collected_items";
    private static final String NBT_ITEMS = "items";

    private static final Type<CollectedItemsState> TYPE = new Type<>(
            CollectedItemsState::new,
            CollectedItemsState::fromNbt,
            null
    );

    public static CollectedItemsState get(MinecraftServer server) {
        PersistentStateManager mgr = server.getOverworld().getPersistentStateManager();
        return mgr.getOrCreate(TYPE, SAVE_ID);
    }

    private final Set<Identifier> everHeld = new HashSet<>();

    /**
     * Marks an item type as ever-held
     * Return true only if this item was not seen before (triggers victory check)
     */
    public boolean markCollected(Identifier itemId) {
        boolean added = everHeld.add(itemId);
        if (added) markDirty();
        return added;
    }

    public boolean hasCollected(Identifier itemId) {
        return everHeld.contains(itemId);
    }

    public Set<Identifier> getEverHeld() {
        return Collections.unmodifiableSet(everHeld);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        NbtList list = new NbtList();
        for (Identifier id : everHeld) {
            list.add(NbtString.of(id.toString()));
        }
        nbt.put(NBT_ITEMS, list);
        return nbt;
    }

    public static CollectedItemsState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        CollectedItemsState state = new CollectedItemsState();
        NbtList list = nbt.getList(NBT_ITEMS, NbtElement.STRING_TYPE);
        for (int i = 0; i < list.size(); i++) {
            Identifier id = Identifier.tryParse(list.getString(i));
            if (id != null) state.everHeld.add(id);
        }
        return state;
    }
}
