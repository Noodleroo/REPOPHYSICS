package net.noodle.repophys.valuables;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import java.util.HashMap;
import java.util.Map;

public class JunkValuables {

    // A fast server dictionary matching world blocks to custom valuable configurations
    private static final Map<Block, ValuableProfile> REGISTRY = new HashMap<>();

    public record ValuableProfile(String id, int maxHealth, double pullFactor, double maxTensionBreak) {}

    static {
        // Register your static items into the dictionary mapping database
        register(Blocks.OAK_PLANKS, new ValuableProfile("wooden_crate", 40, 0.35, 3.5));
        register(Blocks.STONE, new ValuableProfile("heavy_rubble", 150, 0.15, 2.2)); // Heavy metal speed dampening pull
        register(Blocks.GLASS, new ValuableProfile("fragile_urn", 5, 0.50, 4.0));    // High responsiveness but shatters easily
    }

    public static void register(Block block, ValuableProfile profile) {
        REGISTRY.put(block, profile);
    }

    public static ValuableProfile getProfile(Block block) {
        if (REGISTRY.containsKey(block)) {
            return REGISTRY.get(block);
        }
        return  new ValuableProfile("unknown_block", 30, 0.30, 3.0);
    }
}
