package net.noodle.repophys.physics;

import dev.ryanhcode.sable.api.physics.PhysicsPipeline;
import dev.ryanhcode.sable.api.physics.constraint.ConstraintJointAxis;
import dev.ryanhcode.sable.api.physics.constraint.FreeConstraintConfiguration;
import dev.ryanhcode.sable.api.physics.constraint.PhysicsConstraintHandle;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.Pose3d;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.common.EventBusSubscriber;
import net.noodle.repophys.Repophys;
import org.joml.Quaterniond;
import org.joml.Vector3d;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

// 1. Registered to the GAME bus so onLevelTick automatically fires
@EventBusSubscriber(modid = Repophys.MODID)
public class ObjectGrabber {

    public static final Map<UUID, GrabSession> SESSIONS = new HashMap<>();

    // Added offset vector to prevent the object from center-snapping violently upon pickup
    public record GrabSession(ServerSubLevel sub, PhysicsConstraintHandle handle, double distance, Vec3 offset) {}

    public static void startGrab(ServerPlayer player) {
        // Already grabbing something?
        if (SESSIONS.containsKey(player.getUUID())) return;

        ServerLevel level = player.serverLevel();
        ServerSubLevel hit = raycast(level, player, 25.0);
        if (hit == null) return;

        // Get Sable physics pipeline
        ServerSubLevelContainer container = SubLevelContainer.getContainer(level);
        SubLevelPhysicsSystem physicsSystem = container.physicsSystem();
        PhysicsPipeline pipeline = physicsSystem.getPipeline();

        // Compute initial grab anchor
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 hitPos = new Vec3(
                hit.logicalPose().position().x(),
                hit.logicalPose().position().y(),
                hit.logicalPose().position().z()
        );

        double distance = eye.distanceTo(hitPos);

        // Where the player is "holding" the object
        Vec3 targetPoint = eye.add(look.scale(distance));
        Vec3 offset = hitPos.subtract(targetPoint);

        // Create a free constraint (like Simulated)
        FreeConstraintConfiguration config =
                new FreeConstraintConfiguration(
                        JOMLConversion.ZERO,        // parent anchor
                        new Vector3d(),             // child anchor (we update this every physics tick)
                        new Quaterniond()           // orientation (unused for now)
                );

        PhysicsConstraintHandle handle =
                pipeline.addConstraint(null, hit, config);

        // Store session
        GrabSession session = new GrabSession(hit, handle, distance, offset);
        SESSIONS.put(player.getUUID(), session);
    }


    public static void stopGrab(ServerPlayer player) {
        SESSIONS.remove(player.getUUID());
    }

    public static void physicsTick(SubLevelPhysicsSystem physicsSystem) {
        ServerLevel level = physicsSystem.getLevel();

        for (UUID uuid : SESSIONS.keySet()) {
            ServerPlayer player = (ServerPlayer) level.getPlayerByUUID(uuid);
            if (player == null) continue;

            GrabSession session = SESSIONS.get(uuid);
            if (session == null) continue;

            ServerSubLevel sub = session.sub();
            PhysicsConstraintHandle constraint = session.handle();

            if (sub.isRemoved() || !constraint.isValid()) {
                SESSIONS.remove(uuid);
                continue;
            }

            // Compute target
            Vec3 eye = player.getEyePosition();
            Vec3 look = player.getLookAngle();

            Vec3 baseTarget = eye.add(look.scale(session.distance()));
            Vec3 finalTarget = baseTarget.add(session.offset());

            // Convert to JOML
            Vector3d goal = new Vector3d(finalTarget.x, finalTarget.y, finalTarget.z);

            // Apply motors (pull object toward target)
            float stiffness = 200f;
            float damping = 20f;

            constraint.setMotor(ConstraintJointAxis.LINEAR_X, goal.x, stiffness, damping, false, 0);
            constraint.setMotor(ConstraintJointAxis.LINEAR_Y, goal.y, stiffness, damping, false, 0);
            constraint.setMotor(ConstraintJointAxis.LINEAR_Z, goal.z, stiffness, damping, false, 0);
        }
    }

    private static void updateGrab(ServerPlayer player, GrabSession session) {
        ServerSubLevel sub = session.sub();
        if (sub == null || sub.isRemoved()) {
            SESSIONS.remove(player.getUUID());
            return;
        }

        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();

        // Target calculation including the initial grip offset
        Vec3 baseTarget = eye.add(look.scale(session.distance()));
        Vec3 finalTarget = baseTarget.add(session.offset());

        // Update Sable SubLevel matrix position smoothly
        Pose3d pose = sub.logicalPose();
        pose.position().set(finalTarget.x, finalTarget.y, finalTarget.z);
    }

    private static ServerSubLevel raycast(ServerLevel level, ServerPlayer player, double maxDistance) {
        SubLevelContainer container = SubLevelContainer.getContainer(level);
        if (container == null) return null;

        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        Vec3 end = eye.add(look.scale(maxDistance));

        ServerSubLevel closest = null;
        double closestDist = Double.MAX_VALUE;

        for (SubLevel sub : container.getAllSubLevels()) {
            AABB aabb = sub.boundingBox().toMojang();
            Optional<Vec3> hitOpt = aabb.clip(eye, end);

            if (hitOpt.isPresent()) {
                Vec3 hit = hitOpt.get();
                double dist = hit.distanceTo(eye);

                if (dist < closestDist) {
                    closestDist = dist;
                    closest = (ServerSubLevel) sub;
                }
            }
        }
        return closest;
    }
}