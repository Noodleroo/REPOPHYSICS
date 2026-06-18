package net.noodle.repophys.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record GrabActionPacket(boolean grabbing) implements CustomPacketPayload {

    public static final Type<GrabActionPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("repophys", "grab_action"));

    public static final StreamCodec<RegistryFriendlyByteBuf, GrabActionPacket> CODEC =
            StreamCodec.of(GrabActionPacket::write, GrabActionPacket::new);

    // Reader constructor
    public GrabActionPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readBoolean());
    }

    // Writer method
    public static void write(RegistryFriendlyByteBuf buf, GrabActionPacket packet) {
        buf.writeBoolean(packet.grabbing());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

