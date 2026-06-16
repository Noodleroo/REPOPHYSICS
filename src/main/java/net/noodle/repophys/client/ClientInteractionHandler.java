package net.noodle.repophys.client;


import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;

@EventBusSubscriber(modid = "repophys", value = Dist.CLIENT)
public class ClientInteractionHandler {

    @SubscribeEvent
    public static void onClientClick(InputEvent.InteractionKeyMappingTriggered event) {
        // We let the custom PhysicsGrabberItem handle interactions natively now!
        // No more broken global mouse overrides needed here.
    }
}