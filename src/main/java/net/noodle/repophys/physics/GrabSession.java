package net.noodle.repophys.physics;

import dev.ryanhcode.sable.api.physics.constraint.PhysicsConstraintHandle;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.world.phys.Vec3;

public record GrabSession(
        ServerSubLevel sub,
        PhysicsConstraintHandle constraint,
        double distance,
        Vec3 offset
) {}

