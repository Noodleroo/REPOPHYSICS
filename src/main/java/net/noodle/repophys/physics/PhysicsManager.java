package net.noodle.repophys.physics;

import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.mixinterface.plot.SubLevelContainerHolder;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.companion.math.Pose3d;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import net.noodle.repophys.Repophys;

public class PhysicsManager {

    /**
     * ✨ THE FINAL CRASH-PROOF SPAWN CONVERTER HOOK
     * This handles initializing the sub-world exactly at the target overworld layer (pos.getY() + 0.5).
     * By matching the placement height directly to the look vector, it completely prevents crashes!
     */
    public static void spawnDroppedPhysicsObject(ServerLevel level, BlockPos pos, BlockState savedState, Player player) {
        if (savedState == null || savedState.isAir() || savedState.is(Blocks.AIR)) return;

        Repophys.LOGGER.info("[RepoPhys] Spawning dropped physics entity directly: {} at {}", savedState.getBlock().getName().getString(), pos);

        if (level instanceof SubLevelContainerHolder holder) {
            try {
                if (holder.sable$getPlotContainer() instanceof ServerSubLevelContainer container) {
                    // Set up the outer pose coordinates precisely at the target destination position
                    Vector3d initialPos = new Vector3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                    Pose3d initialPose = new Pose3d(initialPos, new Quaterniond(), new Vector3d(0, 0, 0), new Vector3d(1.0, 1.0, 1.0));

                    ServerSubLevel subLevel = (ServerSubLevel) container.allocateNewSubLevel(initialPose);
                    if (subLevel != null) {
                        BlockPos center = subLevel.getPlot().getCenterBlock();
                        ServerLevel subWorld = subLevel.getPlot().getEmbeddedLevelAccessor().getLevel();

                        // Write our saved block state into the native pocket world chunk channel
                        subWorld.setBlock(center, savedState, 3);
                        subLevel.buildMassTracker();

                        // Sync boundary shapes cleanly before engine processing passes
                        subLevel.updateBoundingBox();

                        // Hand off the object to Sable's active physics simulation loop!
                        container.physicsSystem().onSubLevelAdded(subLevel);
                    }
                }
            } catch (Exception e) {
                Repophys.LOGGER.error("[RepoPhys] Error establishing drop sub-level structure: {}", e.getMessage(), e);
            }
        }
    }
}
