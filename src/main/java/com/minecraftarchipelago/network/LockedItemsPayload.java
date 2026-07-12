package com.minecraftarchipelago.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.List;

public record LockedItemsPayload(List<Identifier> itemIds) implements CustomPayload {

    public static final Id<LockedItemsPayload> ID =
            new Id<>(Identifier.of("minecraftarchipelago", "locked_items"));

    public static final PacketCodec<RegistryByteBuf, LockedItemsPayload> CODEC =
            PacketCodec.tuple(
                    Identifier.PACKET_CODEC.collect(PacketCodecs.toList()),
                    LockedItemsPayload::itemIds,
                    LockedItemsPayload::new
            );

    @Override
    public Id<LockedItemsPayload> getId() {
        return ID;
    }
}
