package com.minecraftarchipelago.dashboard;

import net.minecraft.nbt.NbtCompound;

public record ReceiptRecord(
        String slotName,
        int receiptIndex,
        ReceiptKind kind,
        String displayName,
        String iconItemId,
        int quantity,
        String sender
) {
    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putString("slotName", slotName);
        nbt.putInt("receiptIndex", receiptIndex);
        nbt.putString("kind", kind.name());
        nbt.putString("displayName", displayName);
        nbt.putString("iconItemId", iconItemId);
        nbt.putInt("quantity", quantity);
        nbt.putString("sender", sender);
        return nbt;
    }

    public static ReceiptRecord fromNbt(NbtCompound nbt) {
        ReceiptKind kind;

        try {
            kind = ReceiptKind.valueOf(nbt.getString("kind"));
        } catch (IllegalArgumentException ignored) {
            kind = ReceiptKind.GIFT;
        }

        return new ReceiptRecord(
                nbt.getString("slotName"),
                nbt.getInt("receiptIndex"),
                kind,
                nbt.getString("displayName"),
                nbt.getString("iconItemId"),
                nbt.getInt("quantity"),
                nbt.getString("sender")
        );
    }
}