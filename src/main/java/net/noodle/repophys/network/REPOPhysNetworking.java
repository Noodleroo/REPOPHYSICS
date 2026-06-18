package net.noodle.repophys.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.noodle.repophys.Repophys;


@EventBusSubscriber(modid = Repophys.MODID)
public class REPOPhysNetworking {

    @SubscribeEvent
    public static void registerPackets(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(Repophys.MODID);

        // Registering the packet to be handled on the Server play phase
        registrar.playToServer(
                GrabActionPacket.TYPE,
                GrabActionPacket.CODEC,
                REPOPhysNetworking::handleGrabAction
        );
    }

    private static void handleGrabAction(GrabActionPacket packet, IPayloadContext context) {
        // 1. Switch from the network thread to the main server thread safely
        context.enqueueWork(() -> {
            // 2. Get the player who sent the packet
            ServerPlayer player = (ServerPlayer) context.player();
            if (player == null) return;

            // 3. Extract the data carried by the packet
            boolean isGrabbing = packet.grabbing();

            // 4. Execute your game mechanics based on that data
            if (isGrabbing) {
                // Logic for when the player starts grabbing something
                System.out.println(player.getName().getString() + " started grabbing!");
                // e.g., Set a custom capability, attach a physics joint, etc.
            } else {
                // Logic for when the player releases the button
                System.out.println(player.getName().getString() + " stopped grabbing.");
                // e.g., Detach physics, throw the object, etc.
            }
        });
    }



}
