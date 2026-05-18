package com.minecraftarchipelago.loot;


import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

import java.util.*;

public class LootCheckAssignmentState extends PersistentState {

    private static final Type<LootCheckAssignmentState> TYPE = new Type<>(
            LootCheckAssignmentState::new,
            LootCheckAssignmentState::fromNbt,
            null
    );

    private final Map<String, Long> posToLocationId = new LinkedHashMap<>();

    private final Map<Long, Integer> locationIdToIndex = new HashMap<>();

    private int nextIndex = 1;

    public LootCheckAssignmentState() {}

    /**
     * Returns the location ID bound to this chest.
     * If unbound and IDs are available, claims the next one.
     * Returns -1L if all IDs are already claimed.
     */
    public long getOrAssign(String posKey, List<Long> availableIds) {
        if (posToLocationId.containsKey(posKey)) {
            return posToLocationId.get(posKey);
        }

        Set<Long> claimed = new HashSet<>(posToLocationId.values());
        for (Long id : availableIds) {
            if (!claimed.contains(id)) {
                posToLocationId.put(posKey, id);
                locationIdToIndex.put(id, nextIndex++);
                markDirty();
                return id;
            }
        }

        return -1;
    }

    public long getLocationId(String posKey) {
        return posToLocationId.getOrDefault(posKey, -1L);
    }

    public int getCheckedIndex(long locationId) {
        return locationIdToIndex.getOrDefault(locationId, -1);
    }

    public boolean isAssigned(String posKey) {
        return posToLocationId.containsKey(posKey);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        NbtList list = new NbtList();
        for (var entry : posToLocationId.entrySet()) {
            NbtCompound rec = new NbtCompound();
            rec.putString("pos", entry.getKey());
            rec.putLong("loc_id", entry.getValue());
            rec.putInt("index", locationIdToIndex.getOrDefault(entry.getValue(), 0));
            list.add(rec);
        }
        nbt.put("assignments", list);
        return nbt;
    }

    public static LootCheckAssignmentState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        LootCheckAssignmentState state = new LootCheckAssignmentState();
        NbtList list = nbt.getList("assignments", NbtCompound.COMPOUND_TYPE);
        int maxIndex = 0;
        for (int i = 0; i < list.size(); i++) {
            NbtCompound rec = list.getCompound(i);
            String pos = rec.getString("pos");
            long id = rec.getLong("loc_id");
            int idx = rec.getInt("index");
            state.posToLocationId.put(pos, id);
            state.locationIdToIndex.put(id, idx);
            if (idx > maxIndex) maxIndex = idx;
        }
        state.nextIndex = maxIndex + 1;
        return state;
    }

    public static LootCheckAssignmentState get(MinecraftServer server) {
        PersistentStateManager mgr =
                server.getWorld(World.OVERWORLD).getPersistentStateManager();
        return mgr.getOrCreate(TYPE, "ap_loot_check_assignments");
    }

    public static String posKey(Identifier dimension, BlockPos pos) {
        return dimension + "@" + pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }
}
