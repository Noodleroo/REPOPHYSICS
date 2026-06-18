package net.noodle.repophys.physics;

import dev.ryanhcode.sable.api.physics.PhysicsPipeline;
import dev.ryanhcode.sable.api.physics.PhysicsPipelineProvider;
import net.minecraft.server.level.ServerLevel;

import java.util.HashMap;
import java.util.Map;

public class PipelineManager {

    private static final Map<ServerLevel, PhysicsPipeline> PIPELINES = new HashMap<>();

    public static PhysicsPipeline get(ServerLevel level) {
        return PIPELINES.computeIfAbsent(level, lvl -> {
            PhysicsPipeline pipeline = PhysicsPipelineProvider.INSTANCE.createPipeline(lvl);
            // Sable requires a non-null gravity vector
            var gravity = new org.joml.Vector3d(0, -9.81, 0);
            // Drag is usually 0 unless you want air resistance
            double drag = 0.0;
            pipeline.init(gravity, drag);
            return pipeline;
        });
    }

    public static void dispose(ServerLevel level) {
        PhysicsPipeline pipeline = PIPELINES.remove(level);
        if (pipeline != null) {
            pipeline.dispose();
        }
    }
}
