package net.noodle.repophys.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.noodle.repophys.Repophys;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.noodle.repophys.network.ServerboundGrabPacket;
import net.noodle.repophys.network.ModNetworking;

public record ServerboundGrabPacket(int entityId, double tx, double ty, double tz, boolean isReleased) implements CustomPacketPayload {

    public static final Type<ServerboundGrabPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Repophys.MODID, "grab_packet"));

    public static final StreamCodec<FriendlyByteBuf, ServerboundGrabPacket> CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeInt(packet.entityId);
                buf.writeDouble(packet.tx);
                buf.writeDouble(packet.ty);
                buf.writeDouble(packet.tz);
                buf.writeBoolean(packet.isReleased); // Using a boolean cuts down payload overhead
            },
            buf -> new ServerboundGrabPacket(buf.readInt(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readBoolean())
    );



    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}