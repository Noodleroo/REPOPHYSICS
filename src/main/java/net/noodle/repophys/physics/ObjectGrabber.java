package net.noodle.repophys.physics;

import dev.ryanhcode.sable.api.physics.force.ForceGroups;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.noodle.repophys.Repophys;
import org.joml.Vector3d;

import java.util.*;

@EventBusSubscriber(modid = Repophys.MODID)
public class ObjectGrabber {
    public static final HashMap<UUID, ServerSubLevel> ACTIVE_GRABS = new HashMap<>();
    public static final HashMap<UUID, Vector3d> PENDING_LAUNCHES = new HashMap<>();

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Pre event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        List<UUID> toRemove = new ArrayList<>();

        // Process Continuous Tractor Beam Leash Forces
        for (Map.Entry<UUID, ServerSubLevel> entry : ACTIVE_GRABS.entrySet()) {
            UUID playerUuid = entry.getKey();
            ServerSubLevel subLevel = entry.getValue();

            if (subLevel != null && !subLevel.isRemoved()) {
                Player player = serverLevel.getServer().getPlayerList().getPlayer(playerUuid);

                // Break tether if player switches items, drops it, or logs out
                if (player == null || player.level() != serverLevel || !player.isUsingItem()) {
                    toRemove.add(playerUuid);
                    continue;
                }

                Vec3 lookAngle = player.getLookAngle().normalize();
                Vec3 holdTarget = player.getEyePosition().add(lookAngle.scale(4.5));
                Vector3d objPos = subLevel.logicalPose().position();

                double diffX = holdTarget.x - objPos.x;
                double diffY = ((holdTarget.y - 0.5) - objPos.y);
                double diffZ = holdTarget.z - objPos.z;

                double separationDistance = holdTarget.distanceTo(new Vec3(objPos.x, objPos.y, objPos.z));
                if (separationDistance > 12.0) { // Max break distance
                    toRemove.add(playerUuid);
                    player.stopUsingItem();
                    player.sendSystemMessage(Component.literal("§c[RepoPhys] Tractor beam snapped!"));
                    continue;
                }

                // Force wake up state
                subLevel.setLastNetworkedStopped(false);

                double mass = 1.0;
                if (subLevel.getMassTracker() != null) {
                    mass = Math.max(1.0, subLevel.getMassTracker().getMass());
                }

                var forceGroup = subLevel.getOrCreateQueuedForceGroup(ForceGroups.LIFT.get());

                // Attractor Vector
                double springStrength = 500.0 * mass;
                Vector3d forceVector = new Vector3d(diffX * springStrength, diffY * springStrength, diffZ * springStrength);

                // Linear Dampening (Prevents rubber-banding/orbiting)
                Vector3d currentVel = subLevel.latestLinearVelocity;
                if (currentVel != null) {
                    forceVector.sub(new Vector3d(currentVel).mul(30.0 * mass));
                }

                forceGroup.recordPointForce(new Vector3d(0, 0, 0), forceVector);
            }
        }

        // Process Toss/Fling Forces
        if (!PENDING_LAUNCHES.isEmpty()) {
            var container = SubLevelContainer.getContainer(serverLevel);
            if (container instanceof ServerSubLevelContainer serverContainer) {
                var iterator = PENDING_LAUNCHES.entrySet().iterator();
                while (iterator.hasNext()) {
                    var launchEntry = iterator.next();
                    UUID subLevelUuid = launchEntry.getKey();

                    ServerSubLevel targetSubLevel = (ServerSubLevel) serverContainer.getSubLevel(subLevelUuid);
                    if (targetSubLevel != null && !targetSubLevel.isRemoved()) {

                        double mass = 1.0;
                        if (targetSubLevel.getMassTracker() != null) {
                            mass = Math.max(1.0, targetSubLevel.getMassTracker().getMass());
                        }

                        targetSubLevel.setLastNetworkedStopped(false);
                        var forceGroup = targetSubLevel.getOrCreateQueuedForceGroup(ForceGroups.LIFT.get());

                        // Scale the throw vector relative to mass so mass doesn't slow down the throw
                        Vector3d launchForce = new Vector3d(launchEntry.getValue()).mul(3500.0 * mass);
                        forceGroup.recordPointForce(new Vector3d(0, 0, 0), launchForce);
                    }
                    iterator.remove();
                }
            }
        }

        for (UUID uuid : toRemove) {
            ACTIVE_GRABS.remove(uuid);
        }
    }
}