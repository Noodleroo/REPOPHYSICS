package net.noodle.repophys.physics;

import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class SubLevelRaycast {

    public static ServerSubLevel raycast(ServerLevel level, Player player, double maxDistance) {

        ServerSubLevelContainer container =
                SubLevelContainer.getContainer(level);

        var subs = container.getAllSubLevels();

        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        Vec3 end = eye.add(look.scale(maxDistance));

        ServerSubLevel closest = null;
        double closestDist = Double.MAX_VALUE;

        for (ServerSubLevel sub : subs) {

            AABB aabb = sub.boundingBox().toMojang();

            Optional<Vec3> hitOpt = aabb.clip(eye, end);

            if (hitOpt.isPresent()) {
                Vec3 hit = hitOpt.get();
                double dist = hit.distanceTo(eye);


            if (dist < closestDist) {
                    closestDist = dist;
                    closest = sub;
                }
            }
        }

        return closest;
    }

}

