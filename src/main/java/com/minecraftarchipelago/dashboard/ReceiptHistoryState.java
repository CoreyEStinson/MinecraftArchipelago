package com.minecraftarchipelago.dashboard;

import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ReceiptHistoryState extends PersistentState {
    public static final String DATA_ID = "ap_dashboard_receipts";

    public static final Type<ReceiptHistoryState> TYPE = new Type<>(
            ReceiptHistoryState::new,
            ReceiptHistoryState::fromNbt,
            DataFixTypes.LEVEL
    );

    private final List<ReceiptRecord> records = new ArrayList<>();

    public static ReceiptHistoryState get(MinecraftServer server) {
        PersistentStateManager manager =
                server.getOverworld().getPersistentStateManager();
        return manager.getOrCreate(TYPE, DATA_ID);
    }

    public void append(ReceiptRecord record) {
        boolean alreadyRecorded = records.stream()
                .anyMatch(existing -> existing.slotName().equals(record.slotName())
                        && existing.receiptIndex() == record.receiptIndex());

        if (alreadyRecorded) {
            return;
        }

        records.add(record);
        markDirty();
    }

    public List<ReceiptRecord> getNewestFirst(ReceiptKind kind) {
        List<ReceiptRecord> result = new ArrayList<>();

        for (ReceiptRecord record : records) {
            if (record.kind() == kind) {
                result.add(record);
            }
        }

        Collections.reverse(result);
        return List.copyOf(result);
    }

    public List<ReceiptRecord> getNewestFirst(ReceiptKind kind, String slotName) {
        List<ReceiptRecord> result = new ArrayList<>();

        for (ReceiptRecord record : records) {
            if (record.kind() == kind && record.slotName().equals(slotName)) {
                result.add(record);
            }
        }

        Collections.reverse(result);
        return List.copyOf(result);
    }

    public List<ReceiptRecord> getRecentNewestFirst(int limit) {
        List<ReceiptRecord> result = new ArrayList<>(records);
        Collections.reverse(result);

        if (result.size() > limit) {
            result = new ArrayList<>(result.subList(0, limit));
        }

        return List.copyOf(result);
    }

    public List<ReceiptRecord> getRecentNewestFirst(String slotName, int limit) {
        List<ReceiptRecord> result = new ArrayList<>();

        for (ReceiptRecord record : records) {
            if (record.slotName().equals(slotName)) {
                result.add(record);
            }
        }

        Collections.reverse(result);

        if (result.size() > limit) {
            result = new ArrayList<>(result.subList(0, limit));
        }

        return List.copyOf(result);
    }

    private static ReceiptHistoryState fromNbt(
            NbtCompound nbt,
            RegistryWrapper.WrapperLookup registries
    ) {
        ReceiptHistoryState state = new ReceiptHistoryState();

        NbtList savedRecords = nbt.getList("records", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < savedRecords.size(); i++) {
            state.records.add(ReceiptRecord.fromNbt(savedRecords.getCompound(i)));
        }

        return state;
    }

    @Override
    public NbtCompound writeNbt(
            NbtCompound nbt,
            RegistryWrapper.WrapperLookup registries
    ) {
        NbtList savedRecords = new NbtList();

        for (ReceiptRecord record : records) {
            savedRecords.add(record.toNbt());
        }

        nbt.put("records", savedRecords);
        return nbt;
    }
}