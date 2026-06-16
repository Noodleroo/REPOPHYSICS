package net.noodle.repophys.items;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.state.BlockState;

public class DebugSuperShovel extends ShovelItem {

    public DebugSuperShovel(Properties properties) {
        // We base it on Diamond Tier properties but make it unbreakable
        super(Tiers.DIAMOND, properties.durability(9999).fireResistant());
    }

    /**
     * ⚡ THE INSTANT DESTROY SPEED OVERRIDE
     * This forces the shovel to mine every single block at lightspeed.
     */
    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        // Returns a massive speed multiplier. 1000f makes everything instant break!
        return 1000.0F;
    }

    /**
     * 💎 THE HARVEST VALIDATOR OVERRIDE
     * This tells the game that this shovel is the "correct tool" for every block type.
     * It ensures you get the actual item drops for rocks, ores, and obsidian.
     */
    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return true;
    }
}
