package net.noodle.repophys.physics;

import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.Pose3d;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.noodle.repophys.network.GrabActionPacket;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ObjectGrabber {

    // Stores active grab sessions per player
    public static final Map<UUID, GrabSession> SESSIONS = new HashMap<>();

    // A grab session stores the SubLevel and the distance from the player
    public record GrabSession(ServerSubLevel sub, double distance) {}

    // Called when the player presses the grab key
    public static void startGrab(ServerPlayer player) {
        if (SESSIONS.containsKey(player.getUUID())) return;

        ServerLevel level = player.serverLevel();
        ServerSubLevel hit = raycast(level, player, 5.0);

        if (hit == null) return;

        // Distance from player eye to SubLevel center
        Vec3 center = hit.boundingBox().toMojang().getCenter();
        double dist = player.getEyePosition().distanceTo(center);

        SESSIONS.put(player.getUUID(), new GrabSession(hit, dist));
    }

    // Called when the player releases the grab key
    public static void stopGrab(ServerPlayer player) {
        SESSIONS.remove(player.getUUID());
    }

    // Main tick loop — moves grabbed objects
    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Pre event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        for (ServerPlayer player : level.players()) {
            GrabSession session = SESSIONS.get(player.getUUID());
            if (session != null) {
                updateGrab(player, session);
            }
        }
    }

    // Moves the SubLevel toward the player's cursor
    private static void updateGrab(ServerPlayer player, GrabSession session) {

        ServerSubLevel sub = session.sub();
        if (sub == null || sub.isRemoved()) {
            SESSIONS.remove(player.getUUID());
            return;
        }

        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 target = eye.add(look.scale(session.distance()));

        // Mutate the SubLevel's pose directly (Sable API)
        Pose3d pose = sub.logicalPose();
        pose.position().set(target.x, target.y, target.z);
    }

    // Raycast against all SubLevels in the world
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
