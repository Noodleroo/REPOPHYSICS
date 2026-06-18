package net.noodle.repophys;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.noodle.repophys.items.ModItems;
import net.noodle.repophys.util.RepophysConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod("repophys")
public class Repophys {

    public static final String MODID = "repophys";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public Repophys(IEventBus modEventBus, ModContainer modContainer) {
        ModItems.register(modEventBus);




        modContainer.registerConfig(net.neoforged.fml.config.ModConfig.Type.CLIENT, RepophysConfig.CLIENT_SPEC, "repophys-client.toml");

        modContainer.registerExtensionPoint(
                net.neoforged.neoforge.client.gui.IConfigScreenFactory.class,
                ConfigurationScreen::new
        );


    }

}
