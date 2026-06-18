package net.noodle.repophys.content;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.noodle.repophys.physics.ObjectGrabber;

public class RepoStaffItem extends Item {

    public RepoStaffItem(Properties props) {
        super(props);
    }

    /**
     * Called when the player right-clicks (use item).
     * We use this to START a grab session via the ObjectGrabber.
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.consume(itemstack);
        }

        ServerPlayer serverPlayer = (ServerPlayer) player;

        // Try to start a grab. ObjectGrabber handles its own internal raycasting.
        ObjectGrabber.startGrab(serverPlayer);

        // Check if a grab was actually successful
        if (ObjectGrabber.SESSIONS.containsKey(serverPlayer.getUUID())) {
            serverPlayer.startUsingItem(hand);
            return InteractionResultHolder.success(itemstack);
        }

        return InteractionResultHolder.fail(itemstack);
    }

    /**
     * Called every tick while the player is holding right-click.
     * Handled globally by ObjectGrabber's onLevelTick, so we keep this empty.
     */
    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        // No action needed here
    }

    /**
     * Called when the player releases right-click or the use duration expires.
     * We use this to safely break the grab.
     */
    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (level.isClientSide) return;

        if (entity instanceof ServerPlayer serverPlayer) {
            ObjectGrabber.stopGrab(serverPlayer);
        }
    }

    /**
     * Set a high use duration so the player can hold down right-click continuously.
     */
    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000; // 1 hour in real-time ticks
    }
}