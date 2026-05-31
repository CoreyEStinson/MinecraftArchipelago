package com.minecraftarchipelago.victory;

import com.minecraftarchipelago.SlotData;
import com.minecraftarchipelago.aplocations.CheckedLocationsState;
import com.minecraftarchipelago.collections.CollectedItemsState;
import com.minecraftarchipelago.collections.ItemCollection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Set;

/**
 * One instance per collection, registered at startup via ItemCollectionRegistry.
 * isEnabled() checks slot data for this specific collection's ID.
 */
public class ItemCollectionChecker implements VictoryConditionChecker{

    private final ItemCollection collection;

    public ItemCollectionChecker(ItemCollection collection) {
        this.collection = collection;
    }

    @Override
    public String getId() {
        return "collection_"+collection.id();
    }

    @Override
    public boolean isEnabled(SlotData slotData) {
        return slotData.isCollectionRequired(collection.id());
    }

    @Override
    public boolean isMet(MinecraftServer server, CheckedLocationsState state, SlotData slotData) {
        Set<Identifier> everHeld = CollectedItemsState.get(server).getEverHeld();
        return collection.isComplete(everHeld);
    }

    @Override
    public VictoryProgress getProgress(MinecraftServer server, CheckedLocationsState state, SlotData slotData) {
        Set<Identifier> everHeld = CollectedItemsState.get(server).getEverHeld();
        int collected = collection.countCollected(everHeld);
        int total = collection.total();
        return new VictoryProgress(
                collection.displayName(),
                collected,
                total,
                total,
                collected >= total
        );
    }

    /**
     * Returns sorted names of still needed items
     */
    @Override
    public List<String> getRemainingDetails(MinecraftServer server,
                                            CheckedLocationsState checkedState,
                                            SlotData slotData) {
        Set<Identifier> everHeld = CollectedItemsState.get(server).getEverHeld();
        return collection.getUncollectedNames(everHeld);
    }
}
