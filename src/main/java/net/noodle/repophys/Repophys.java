package net.noodle.repophys;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.noodle.repophys.blocks.ModBlocks;
import net.noodle.repophys.network.ModNetworking;

@Mod("repophys")
public class Repophys {

    public static final String MODID = "repophys";


    public Repophys(IEventBus modEventBus, ModContainer modContainer) {
        // Registers your custom blocks/items
        ModBlocks.register(modEventBus);

        // --- FIXED: Wake up ModNetworking! ---
        ModNetworking.init(modEventBus);


        modContainer.registerConfig(net.neoforged.fml.config.ModConfig.Type.CLIENT, RepophysConfig.CLIENT_SPEC, "repophys-client.toml");
        // Parameter 1: ModContainer
        // Parameter 2: Screen (parent)
        modContainer.registerExtensionPoint(
                net.neoforged.neoforge.client.gui.IConfigScreenFactory.class,
                ConfigurationScreen::new
        );
    }

}