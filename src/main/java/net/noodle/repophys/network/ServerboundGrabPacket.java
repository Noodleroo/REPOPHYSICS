package net.noodle.repophys.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.noodle.repophys.Repophys;

public record ServerboundGrabPacket(BlockPos targetBlockPos, double tx, double ty, double tz, boolean isReleased) implements CustomPacketPayload {

    public static final Type<ServerboundGrabPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Repophys.MODID, "grab_packet"));


    public static final StreamCodec<FriendlyByteBuf, ServerboundGrabPacket> CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeLong(packet.targetBlockPos.asLong());
                buf.writeDouble(packet.tx);
                buf.writeDouble(packet.ty);
                buf.writeDouble(packet.tz);
                buf.writeBoolean(packet.isReleased);
            },
            buf -> new ServerboundGrabPacket(
                    BlockPos.of(buf.readLong()),
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readBoolean()
            )
    );




    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}