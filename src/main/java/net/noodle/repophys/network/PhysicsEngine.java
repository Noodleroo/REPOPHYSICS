package net.noodle.repophys.network;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PhysicsEngine {

    private static final ConcurrentHashMap<UUID, Vec3> PREVIOUS_POSITIONS = new ConcurrentHashMap<>();

    public static void tickGrabPhysics(ServerPlayer player, ServerLevel world, BlockPos targetBlockPos, ModNetworking.GrabData grabData) {
        UUID uuid = player.getUUID();
        Vec3 targetPos = new Vec3(grabData.tx(), grabData.ty(), grabData.tz());

        Vec3 lastPos = PREVIOUS_POSITIONS.get(uuid);
        PREVIOUS_POSITIONS.put(uuid, targetPos);

        // Placeholder for Sable block assemblies manipulation logic!
    }

    public static void executeThrowHandoff(ServerPlayer player, ServerLevel world, BlockPos targetBlockPos, ServerboundGrabPacket packet) {
        UUID uuid = player.getUUID();
        PREVIOUS_POSITIONS.remove(uuid);

        // Placeholder for final block landing placement!
    }

    public static void purgeUserData(UUID uuid) {
        PREVIOUS_POSITIONS.remove(uuid);
    }
}
