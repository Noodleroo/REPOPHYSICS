package net.noodle.repophys.content;

import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.mixinterface.plot.SubLevelContainerHolder;
import dev.ryanhcode.sable.companion.math.Pose3d;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.noodle.repophys.physics.ObjectGrabber; // Our revised backend mapping
import org.joml.Vector3d;
import org.joml.Quaterniond;

import java.util.UUID;

public class RepoStaffItem extends Item {

    public RepoStaffItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW; // Gives a holding/aiming animation while pulling
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000; // Allows holding it indefinitely
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            UUID uuid = player.getUUID();

            // Raycast and look for existing Sable sub-levels
            ServerSubLevel targetedSubLevel = findTargetSubLevel((ServerLevel) level, player);

            if (targetedSubLevel != null) {
                ObjectGrabber.ACTIVE_GRABS.put(uuid, targetedSubLevel);
                player.startUsingItem(hand);
                player.sendSystemMessage(Component.literal("§a[RepoPhys] Tractor beam locked!"));
                return InteractionResultHolder.consume(stack);
            }
        }

        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!level.isClientSide() && entity instanceof Player player) {
            UUID uuid = player.getUUID();

            if (ObjectGrabber.ACTIVE_GRABS.containsKey(uuid)) {
                ServerSubLevel subLevel = ObjectGrabber.ACTIVE_GRABS.get(uuid);

                if (subLevel != null && !subLevel.isRemoved()) {
                    Vec3 lookAngle = player.getLookAngle().normalize();

                    // Calculate Fling Vector
                    double launchStrength = 45.0;
                    Vector3d launchVector = new Vector3d(lookAngle.x * launchStrength, lookAngle.y * launchStrength, lookAngle.z * launchStrength);

                    // Queue the throw impulse
                    ObjectGrabber.PENDING_LAUNCHES.put(subLevel.getUniqueId(), launchVector);
                    player.sendSystemMessage(Component.literal("§c[RepoPhys] Object launched!"));
                }

                ObjectGrabber.ACTIVE_GRABS.remove(uuid);
            }
        }
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int count) {
        // Keeps the tether updated while the item is active
        if (!level.isClientSide() && entity instanceof Player player) {
            UUID uuid = player.getUUID();
            if (!ObjectGrabber.ACTIVE_GRABS.containsKey(uuid)) {
                player.stopUsingItem();
            }
        }
    }

    private ServerSubLevel findTargetSubLevel(ServerLevel level, Player player) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle().normalize();
        double reachDistance = 10.0;
        Vec3 targetVec = eyePos.add(lookVec.scale(reachDistance));

        if (level instanceof SubLevelContainerHolder holder && holder.sable$getPlotContainer() instanceof ServerSubLevelContainer container) {
            double closestDistance = Double.MAX_VALUE;
            ServerSubLevel targetedSubLevel = null;

            for (ServerSubLevel subLevel : container.getAllSubLevels()) {
                if (subLevel.isRemoved()) continue;
                var sableBounds = subLevel.boundingBox();
                AABB overworldBox = new AABB(
                        sableBounds.minX(), sableBounds.minY(), sableBounds.minZ(),
                        sableBounds.maxX(), sableBounds.maxY(), sableBounds.maxY()
                ).inflate(0.5);

                var hitResult = overworldBox.clip(eyePos, targetVec);
                if (hitResult.isPresent()) {
                    double distance = eyePos.distanceToSqr(hitResult.get());
                    if (distance < closestDistance) {
                        closestDistance = distance;
                        targetedSubLevel = subLevel;
                    }
                }
            }
            return targetedSubLevel;
        }
        return null;
    }
}