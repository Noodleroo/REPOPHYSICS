package net.noodle.repophys.network;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.noodle.repophys.Repophys;
import net.noodle.repophys.physics.ObjectGrabber;

import java.util.UUID;


@EventBusSubscriber(modid = Repophys.MODID)
public class REPOPhysNetworking {

    @SubscribeEvent
    public static void registerPackets(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(Repophys.MODID);

        // Registering the packet to be handled on the Server play phase
        registrar.playToServer(
                GrabActionPacket.TYPE,
                GrabActionPacket.CODEC,
                (packet, context) -> {
                    context.enqueueWork(() -> {
                        ServerPlayer player = (ServerPlayer) context.player();
                        if (player == null) return;

                        if (packet.grabbing()) {
                            ObjectGrabber.startGrab(player);
                        } else {
                            ObjectGrabber.stopGrab(player);
                        }
                    });
                }
        );
    }

    private static void handleGrabAction(GrabActionPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                if (payload.grabbing()) {
                    ObjectGrabber.startGrab(player);
                } else {
                    ObjectGrabber.stopGrab(player);
                }
            }
        });
    }
}
