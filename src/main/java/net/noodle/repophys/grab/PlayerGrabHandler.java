package net.noodle.repophys.grab;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.noodle.repophys.RepophysConfig;
import net.noodle.repophys.client.ClientInputHandler;
import net.noodle.repophys.network.ServerboundGrabPacket;

public class PlayerGrabHandler {
    public static BlockPos grabbedBlockPos = null;
    public static BlockPos clientHoveredBlockPos = null;

    public static int snapIgnoreTicks = 0;
    private static int loopTickHeartbeat = 0;

    public static void executeEntityGrabTick() {
        Minecraft mc = Minecraft.getInstance();
        if (snapIgnoreTicks > 0) snapIgnoreTicks--;

        if (mc.player == null || mc.level == null) return;
        double reachDistance = RepophysConfig.CLIENT.reachDistance.get();

        if (RepophysConfig.CLIENT.enableActionbarHeartbeat.get()) {
            loopTickHeartbeat++;
            if (loopTickHeartbeat >= 100) {
                mc.player.displayClientMessage(Component.literal("§7[System] Block Grab Tick Active..."), true);
                loopTickHeartbeat = 0;
            }
        }

        // 1. STATE A: ACTIVE DRAGGING
        if (ClientInputHandler.isCurrentlyHolding) {
            if (grabbedBlockPos == null && clientHoveredBlockPos != null) {
                grabbedBlockPos = clientHoveredBlockPos;
            }

            if (grabbedBlockPos != null) {
                double holdDistance = RepophysConfig.CLIENT.holdDistance.get();
                Vec3 lookVec = mc.player.getViewVector(1.0F);

                Vec3 playerEyeVec = dev.ryanhcode.sable.companion.SableCompanion.INSTANCE.getEyePositionInterpolated(mc.player, 1.0F);
                Vec3 targetFloatingVec = playerEyeVec.add(
                        lookVec.x * holdDistance,
                        lookVec.y * holdDistance,
                        lookVec.z * holdDistance
                );

                PacketDistributor.sendToServer(
                        new ServerboundGrabPacket(grabbedBlockPos, targetFloatingVec.x, targetFloatingVec.y, targetFloatingVec.z, false)
                );
            }
            return;
        }

        // 2. STATE B: RELEASE HANDOFF INTERCEPTOR
        else if (grabbedBlockPos != null) {
            Vec3 currentLook = mc.player.getViewVector(1.0F);
            Vec3 throwVelocity = mc.player.getDeltaMovement().add(currentLook.scale(1.5D));

            // ✨ FIXED: Passes grabbedBlockPos cleanly instead of grabbedEntity.getId()
            PacketDistributor.sendToServer(new ServerboundGrabPacket(
                    grabbedBlockPos,
                    throwVelocity.x,
                    throwVelocity.y,
                    throwVelocity.z,
                    true
            ));

            grabbedBlockPos = null;
            clientHoveredBlockPos = null;
            snapIgnoreTicks = 15;
            return;
        }

        if (ClientInputHandler.isCurrentlyHolding || grabbedBlockPos != null) return;

        // 3. STATE C: SCANNING MODE
        BlockHitResult hitResult = performBlockRaycast(mc, reachDistance);
        if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
            clientHoveredBlockPos = hitResult.getBlockPos();
        } else {
            clientHoveredBlockPos = null;
        }
    }

    public static void forceRelease(boolean isSnap) {
        if (grabbedBlockPos != null) {
            PacketDistributor.sendToServer(new ServerboundGrabPacket(grabbedBlockPos, 0, 0, 0, true));
            grabbedBlockPos = null;
            clientHoveredBlockPos = null;
        }
    }

    private static BlockHitResult performBlockRaycast(Minecraft mc, double reach) {
        if (mc.getCameraEntity() == null) return null;
        Vec3 startVec = mc.getCameraEntity().getEyePosition(1.0F);
        Vec3 lookVec = mc.getCameraEntity().getViewVector(1.0F);
        Vec3 endVec = startVec.add(lookVec.x * reach, lookVec.y * reach, lookVec.z * reach);

        return mc.level.clip(new ClipContext(
                startVec, endVec, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, mc.getCameraEntity()
        ));
    }
}
