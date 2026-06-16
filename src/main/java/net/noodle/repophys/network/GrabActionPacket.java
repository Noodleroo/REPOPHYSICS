package net.noodle.repophys.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.noodle.repophys.Repophys;

public record GrabActionPacket(boolean isReleasing) implements CustomPacketPayload {
    public static final Type<GrabActionPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Repophys.MODID, "grab_action"));

    public static final StreamCodec<FriendlyByteBuf, GrabActionPacket> CODEC = StreamCodec.of(
            (buf, value) -> buf.writeBoolean(value.isReleasing()),
            buf -> new GrabActionPacket(buf.readBoolean())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}