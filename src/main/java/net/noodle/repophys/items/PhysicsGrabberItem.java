package net.noodle.repophys.items;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.noodle.repophys.physics.PhysicsManager;

public class PhysicsGrabberItem extends Item {

    private static final int COOLDOWNS_TICKS = 10; // 0.5 second activation buffer window

    public PhysicsGrabberItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            // Apply a nice cooldown bar effect directly over the tool icon on the hotbar
            if (player.getCooldowns().isOnCooldown(this)) {
                return InteractionResult.FAIL;
            }

            // Convert the block directly into an active, dropping world object
            //PhysicsManager.convertBlockToPhysicsObject(serverLevel, pos);

            player.getCooldowns().addCooldown(this, COOLDOWNS_TICKS);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}
