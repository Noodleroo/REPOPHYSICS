package net.noodle.repophys.network;

import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PhysicsEngine {

    public static final double THROW_POWER = 1.8D; // Tweak this variable to amplify throw velocity

    // Keeps track of the entity position context historical records
    private static final ConcurrentHashMap<UUID, Vec3> PREVIOUS_POSITIONS = new ConcurrentHashMap<>();

    /**
     * accept client-side positions and register the frame indices directly on the server thread maps.
     */
    public static void tickGrabPhysics(ServerPlayer player, ServerLevel world, Entity serverEntity, ModNetworking.GrabData grabData) {
        serverEntity.setNoGravity(true);
        UUID uuid = player.getUUID();

        double targetX = grabData.tx();
        double targetY = grabData.ty();
        double targetZ = grabData.tz();

        // Dynamically compute vector momentum properties from our positional coordinate delta sequences
        Vec3 lastPos = PREVIOUS_POSITIONS.get(uuid);
        Vec3 computedVelocity = Vec3.ZERO;
        if (lastPos != null) {
            computedVelocity = new Vec3(targetX - lastPos.x, targetY - lastPos.y, targetZ - lastPos.z);
        }
        PREVIOUS_POSITIONS.put(uuid, new Vec3(targetX, targetY, targetZ));

        // Wipe standard pathfinding scripts so entities don't apply manual braking arrays
        if (serverEntity instanceof Mob mob) {
            mob.getNavigation().moveTo(targetX, targetY, targetZ, 0.0D);
        }

        // Hard lock the positions inside the chunk registry framework
        serverEntity.setPos(targetX, targetY, targetZ);
        serverEntity.setDeltaMovement(computedVelocity);

        serverEntity.fallDistance = 0.0F;
        serverEntity.hasImpulse = true;
        serverEntity.hurtMarked = true;

        // Sync coordinate data out to all tracking player view maps
        world.getChunkSource().broadcast(serverEntity, new ClientboundTeleportEntityPacket(serverEntity));
    }

    /**
     * Executes launch operations when a true release packet structure arrives.
     */
    public static void executeThrowHandoff(ServerPlayer player, ServerLevel world, Entity serverEntity, ServerboundGrabPacket packet) {
        serverEntity.setNoGravity(false);
        UUID uuid = player.getUUID();

        // Map flight launch properties directly from the client packet values
        double throwX = packet.tx();
        double throwY = packet.ty();
        double throwZ = packet.tz();

        Vec3 finalizedThrow = new Vec3(throwX * THROW_POWER, throwY * THROW_POWER, throwZ * THROW_POWER);

        // Position fast-forward offsets
        double dropX = serverEntity.getX() + (finalizedThrow.x * 0.1D);
        double dropY = serverEntity.getY() + (finalizedThrow.y * 0.1D);
        double dropZ = serverEntity.getZ() + (finalizedThrow.z * 0.1D);

        serverEntity.setPos(dropX, dropY, dropZ);
        serverEntity.setDeltaMovement(finalizedThrow);

        serverEntity.hasImpulse = true;
        serverEntity.hurtMarked = true;
        serverEntity.fallDistance = 0.0F;

        // Flush coordinate caches
        serverEntity.teleportTo(dropX, dropY, dropZ);

        // Push definitive velocity tracking packets
        ClientboundSetEntityMotionPacket velocityPacket = new ClientboundSetEntityMotionPacket(serverEntity);
        world.getChunkSource().broadcast(serverEntity, velocityPacket);
        player.connection.send(velocityPacket);

        PREVIOUS_POSITIONS.remove(uuid);
    }

    public static void purgeUserData(UUID uuid) {
        PREVIOUS_POSITIONS.remove(uuid);
    }
}
