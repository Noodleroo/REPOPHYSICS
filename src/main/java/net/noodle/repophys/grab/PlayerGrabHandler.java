package net.noodle.repophys.grab;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.noodle.repophys.RepophysConfig;
import net.noodle.repophys.client.ClientInputHandler;
import net.noodle.repophys.network.ServerboundGrabPacket;

public class PlayerGrabHandler {
    public static Entity grabbedEntity = null;
    public static int clientHoveredEntityId = -1;
    public static int lastSnappedEntityId = -1;
    private static int networkThrottleTicks = 0;

    // PERSISTENT CONTEXT TRACKERS
    public static int lastLoggedEntityId = -1;
    public static int snapIgnoreTicks = 0;
    private static int loopTickHeartbeat = 0;

    public static void executeEntityGrabTick() {
        Minecraft mc = Minecraft.getInstance();
        if (snapIgnoreTicks > 0) {
            snapIgnoreTicks--;
        }

        // 1. Context Safety Gate
        if (mc.player == null || mc.level == null) return;
        double reachDistance = RepophysConfig.CLIENT.reachDistance.get();

        // Heartbeat logger
        if (RepophysConfig.CLIENT.enableActionbarHeartbeat.get()) {
            loopTickHeartbeat++;
            if (loopTickHeartbeat >= 100) {
                mc.player.displayClientMessage(Component.literal("§7[System] Grab Tick Engine Active..."), true);
                loopTickHeartbeat = 0;
            }
        }

        // 2. STATE A: ACTIVE DRAGGING MODE
        if (ClientInputHandler.isCurrentlyHolding) {
            // First-frame promotion: convert hover target to held entity
            if (grabbedEntity == null && clientHoveredEntityId != -1) {
                Entity target = mc.level.getEntity(clientHoveredEntityId);
                if (target != null && target.isAlive()) {
                    grabbedEntity = target;
                    networkThrottleTicks = 0;
                }
            }

            if (grabbedEntity != null) {
                if (!grabbedEntity.isAlive()) {
                    forceRelease(false);
                    return;
                }

                double holdDistance = RepophysConfig.CLIENT.holdDistance.get();
                Vec3 lookVec = mc.player.getViewVector(1.0F);

                // High-precision camera tracking
                Vec3 playerEyeVec = dev.ryanhcode.sable.companion.SableCompanion.INSTANCE.getEyePositionInterpolated(mc.player, 1.0F);
                Vec3 targetPosVec = playerEyeVec.add(
                        lookVec.x * holdDistance,
                        lookVec.y * holdDistance,
                        lookVec.z * holdDistance
                );

                // Force client model positioning instantly to prevent local screen stutter
                grabbedEntity.setNoGravity(true);
                grabbedEntity.setPos(targetPosVec.x, targetPosVec.y, targetPosVec.z);
                grabbedEntity.setDeltaMovement(Vec3.ZERO);

                // Continuously send the precise coordinate positions over to the server maps
                PacketDistributor.sendToServer(
                        new ServerboundGrabPacket(grabbedEntity.getId(), targetPosVec.x, targetPosVec.y, targetPosVec.z, false)
                );
            }
            return;
        }

        // 3. STATE B: THE RELEASE PASS INTERCEPTOR
        else if (grabbedEntity != null) {
            Vec3 currentLook = mc.player.getViewVector(1.0F);

            // Calculate a clean handoff velocity based on camera look vectors
            Vec3 throwVelocity = mc.player.getDeltaMovement().add(currentLook.scale(1.5D));

            // Pass the custom throw vector to the server via the tx, ty, tz arguments
            PacketDistributor.sendToServer(new ServerboundGrabPacket(
                    grabbedEntity.getId(),
                    throwVelocity.x,
                    throwVelocity.y,
                    throwVelocity.z,
                    true
            ));

            // Clean fields immediately
            clientHoveredEntityId = -1;
            lastLoggedEntityId = -1;
            grabbedEntity = null;
            networkThrottleTicks = 0;
            snapIgnoreTicks = 15;
            return;
        }

        if (ClientInputHandler.isCurrentlyHolding || grabbedEntity != null) {
            return;
        }

        // 4. STATE C: IDLE SCANNING MODE
        Entity pointedEntity = performSableAwareRaycast(mc, reachDistance);

        if (pointedEntity != null) {
            PlayerGrabHandler.clientHoveredEntityId = pointedEntity.getId();
            if (PlayerGrabHandler.clientHoveredEntityId != lastLoggedEntityId) {
                if (RepophysConfig.CLIENT.enableChatLogging.get()) {
                    mc.player.displayClientMessage(Component.literal("§b[Tractor Beam]§a Locked onto ID: " + PlayerGrabHandler.clientHoveredEntityId), false);
                }
                lastLoggedEntityId = PlayerGrabHandler.clientHoveredEntityId;
            }
        } else {
            PlayerGrabHandler.clientHoveredEntityId = -1;
            if (lastLoggedEntityId != -1) {
                if (RepophysConfig.CLIENT.enableChatLogging.get()) {
                    mc.player.displayClientMessage(Component.literal("§b[Tractor Beam]§c Target Lost"), false);
                }
                lastLoggedEntityId = -1;
            }
        }
    }

    public static void forceRelease(boolean isSnap) {
        if (grabbedEntity != null) {
            PacketDistributor.sendToServer(new ServerboundGrabPacket(grabbedEntity.getId(), 0, 0, 0, true));
            clientHoveredEntityId = -1;
            lastLoggedEntityId = -1;
            grabbedEntity = null;
        }
        networkThrottleTicks = 0;
    }

    private static Entity performSableAwareRaycast(Minecraft mc, double reach) {
        Entity cameraEntity = mc.getCameraEntity();
        if (cameraEntity == null) return null;

        Vec3 eyePos = cameraEntity.getEyePosition(1.0F);
        Vec3 lookVec = cameraEntity.getViewVector(1.0F);
        Vec3 reachVec = eyePos.add(lookVec.x * reach, lookVec.y * reach, lookVec.z * reach);
        AABB searchBox = cameraEntity.getBoundingBox().expandTowards(lookVec.scale(reach)).inflate(1.0D, 1.0D, 1.0D);

        net.minecraft.world.phys.EntityHitResult hitResult = net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(
                cameraEntity, eyePos, reachVec, searchBox, entity -> {
                    if (entity.isSpectator() || !entity.isAlive()) return false;
                    if (ClientInputHandler.isCurrentlyHolding) return false; // Ignore if holding
                    if (grabbedEntity != null && entity.getId() == grabbedEntity.getId()) return false;
                    if (snapIgnoreTicks > 0 && entity.getId() == lastLoggedEntityId) return false;
                    return true;
                }, reach * reach
        );

        if (hitResult == null) return null;
        return hitResult.getEntity();
    }
}
