package net.noodle.repophys.blocks.debug_block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

// Core Sable Imports verified by the source file
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import dev.ryanhcode.sable.companion.math.BoundingBox3i;

import java.util.List;

public class GrabPhysicsBlock extends Block implements EntityBlock {

    public GrabPhysicsBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GrabPhysicsBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {

            // 1. Define a 1x1x1 bounding box area for the single block position
//            BoundingBox3i singleBlockBounds = new BoundingBox3i(
//                    pos.getX(), pos.getY(), pos.getZ(),
//                    pos.getX(), pos.getY(), pos.getZ()
//            );

            // 2. Wrap our position in a List to satisfy the Iterable requirement
            List<BlockPos> blockList = List.of(pos);

            // 3. Invoke assembleBlocks matching the verified 4-argument signature perfectly
//            Object createdSubLevel = SubLevelAssemblyHelper.assembleBlocks(
//                    serverLevel,
//                    pos,           // Anchor point
//                    blockList,     // Iterable collections
//                    singleBlockBounds // BoundingBox bounds descriptor
//            );

            // 4. Cache the moving sub-level reference in our local BlockEntity data
//            BlockEntity be = level.getBlockEntity(pos);
//            if (be instanceof GrabPhysicsBlockEntity physicsBe) {
//                physicsBe.setActiveSubLevel(createdSubLevel);
//            }

            return InteractionResult.SUCCESS;
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}