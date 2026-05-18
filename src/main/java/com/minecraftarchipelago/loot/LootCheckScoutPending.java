package com.minecraftarchipelago.loot;

import com.minecraftarchipelago.item.ArchipelagoCheckItem;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.item.ItemStack;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class LootCheckScoutPending {

    // locationId → pending chest update
    // WeakReference so a chunk unload doesn't cause a memory leak
    private static final Map<Long, PendingUpdate> PENDING = new ConcurrentHashMap<>();

    public record PendingUpdate(
            WeakReference<LootableContainerBlockEntity> chestRef,
            int slot,
            long timestamp
    ) {}

    public static void register(long locationId,
                                LootableContainerBlockEntity chest,
                                int slot) {
        PENDING.put(locationId, new PendingUpdate(
                new WeakReference<>(chest), slot, System.currentTimeMillis()
        ));
    }

    /**
     * Applies the scouted name to the token inside the chest (if still there)
     * and removes the pending entry.
     */
    public static void resolve(long locationId, int checkIndex,
                               String itemName, String playerName) {
        PendingUpdate pending = PENDING.remove(locationId);
        if (pending == null) return;

        // Stale after 5 minutes
        if (System.currentTimeMillis() - pending.timestamp() > 5 * 60 * 1000) return;

        LootableContainerBlockEntity chest = pending.chestRef().get();
        if (chest == null) return; // chunk unloaded

        ItemStack stack = chest.getStack(pending.slot());
        if (!(stack.getItem() instanceof ArchipelagoCheckItem)) return;
        if (ArchipelagoCheckItem.getLocationId(stack) != locationId) return;

        ArchipelagoCheckItem.setCheckData(stack, locationId, checkIndex,
                itemName, playerName);
        chest.setStack(pending.slot(), stack);
    }

    /** Clears all pending entries older than 10 minutes (call periodically). */
    public static void cleanup() {
        long now = System.currentTimeMillis();
        PENDING.entrySet().removeIf(e ->
                now - e.getValue().timestamp() > 10 * 60 * 1000
        );
    }

    private LootCheckScoutPending() {}
}