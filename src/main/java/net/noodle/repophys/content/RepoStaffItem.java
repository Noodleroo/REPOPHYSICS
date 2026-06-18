package net.noodle.repophys.content;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import net.noodle.repophys.physics.ObjectGrabber;
import net.noodle.repophys.physics.SubLevelRaycast;

public class RepoStaffItem extends Item {

    public RepoStaffItem(Properties props) {
        super(props);
    }

    /**
     * Called when the player right-clicks (use item).
     * We use this to START a grab.
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {

        if (level.isClientSide) {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }

        ServerLevel serverLevel = (ServerLevel) level;
        ServerPlayer serverPlayer = (ServerPlayer) player;

        // Raycast for a SubLevel
        var sub = SubLevelRaycast.raycast(serverLevel, serverPlayer, 12.0);

        if (sub != null) {
            // Start grabbing
            ObjectGrabber.SESSIONS.put(
                    serverPlayer.getUUID(),
                    new ObjectGrabber.GrabSession(sub, 4.0) // hold distance
            );

            serverPlayer.startUsingItem(hand);
            return InteractionResultHolder.success(player.getItemInHand(hand));
        }

        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    /**
     * Called every tick while the player is holding right-click.
     * We use this to keep the grab active.
     */
    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        // Nothing needed here — SubLevelGrabber handles movement each tick
    }

    /**
     * Called when the player releases right-click.
     * We use this to STOP grabbing.
     */
    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {

        if (level.isClientSide) return;

        ObjectGrabber.SESSIONS.remove(entity.getUUID());
    }

    /**
     * Optional: allow long use duration so holding right-click works smoothly.
     */
    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000; // same as bows
    }
}
