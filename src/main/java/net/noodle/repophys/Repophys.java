package net.noodle.repophys;

import dev.ryanhcode.sable.SableCommonEvents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.noodle.repophys.items.ModItems;
import net.noodle.repophys.network.GrabActionPacket;
import net.noodle.repophys.physics.ObjectGrabber;
import net.noodle.repophys.util.RepophysConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod("repophys")
public class Repophys {

    public static final String MODID = "repophys";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public Repophys(IEventBus modEventBus, ModContainer modContainer) {
        ModItems.register(modEventBus);



        //modEventBus.addListener(this::registerNetworkPayloads);

        modContainer.registerConfig(net.neoforged.fml.config.ModConfig.Type.CLIENT, RepophysConfig.CLIENT_SPEC, "repophys-client.toml");

        modContainer.registerExtensionPoint(
                net.neoforged.neoforge.client.gui.IConfigScreenFactory.class,
                ConfigurationScreen::new
        );
    }

//    private void registerNetworkPayloads(final RegisterPayloadHandlersEvent event) {
//        final PayloadRegistrar registrar = event.registrar(MODID);
//
//        registrar.playBidirectional(
//                GrabActionPacket.TYPE,
//                GrabActionPacket.CODEC,
//                GrabActionPacket::handle
//        );
//
//        LOGGER.info("[RepoPhys] Bidirectional network payload channel built successfully!");
//    }
}
