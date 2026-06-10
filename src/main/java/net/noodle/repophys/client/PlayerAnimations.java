package net.noodle.repophys.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.noodle.repophys.Repophys;
import net.noodle.repophys.grab.PlayerGrabHandler;

@EventBusSubscriber(modid = Repophys.MODID, value = Dist.CLIENT)
public class PlayerAnimations {

    @SubscribeEvent
    public static void onRenderHand(net.neoforged.neoforge.client.event.RenderHandEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !ClientInputHandler.isCurrentlyHolding) return;

        if (event.getHand() == net.minecraft.world.InteractionHand.MAIN_HAND) {
            com.mojang.blaze3d.vertex.PoseStack poseStack = event.getPoseStack();
            float bobOffset = (float) Math.sin(ClientInputHandler.customHandPoseTicks) * 0.02f;
            poseStack.translate(0.4D, bobOffset + 0.01D, 0.05D);
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(35.0F));
        }
    }

    @SubscribeEvent
    public static void onRenderLevelStage(net.neoforged.neoforge.client.event.RenderLevelStageEvent event) {
        // Only run this outline pass after the terrain or blocks have drawn
        if (event.getStage() != net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        // ✨ BLOCK HIGHLIGHT FIX: Read the block targets from the new handler layout
        BlockPos grabbedPos = PlayerGrabHandler.grabbedBlockPos;
        BlockPos hoveredPos = PlayerGrabHandler.clientHoveredBlockPos;

        // Prioritize drawing the box on what we are holding, otherwise show what we are looking at
        BlockPos activeTargetPos = (grabbedPos != null) ? grabbedPos : hoveredPos;
        if (activeTargetPos == null) return;

        com.mojang.blaze3d.vertex.PoseStack poseStack = event.getPoseStack();
        net.minecraft.client.renderer.MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        com.mojang.blaze3d.vertex.VertexConsumer buffer = bufferSource.getBuffer(net.minecraft.client.renderer.RenderType.lines());

        poseStack.pushPose();

        // Get camera offsets so our line drawing follows screen adjustments perfectly
        double camX = event.getCamera().getPosition().x;
        double camY = event.getCamera().getPosition().y;
        double camZ = event.getCamera().getPosition().z;

        // Translate drawing space relative to the camera location
        double renderX = activeTargetPos.getX() - camX;
        double renderY = activeTargetPos.getY() - camY;
        double renderZ = activeTargetPos.getZ() - camZ;
        poseStack.translate(renderX, renderY, renderZ);

        // Create a perfect 1x1x1 bounding box around the grid coordinates
        AABB blockOutlineBox = new AABB(0, 0, 0, 1.0D, 1.0D, 1.0D).inflate(0.002D);

        // Render the glowing tractor beam box (Red: 0.0, Green: 0.7, Blue: 1.0)
        net.minecraft.client.renderer.LevelRenderer.renderLineBox(
                poseStack, buffer, blockOutlineBox, 0.0f, 0.7f, 1.0f, 1.0f
        );

        poseStack.popPose();
        bufferSource.endBatch(net.minecraft.client.renderer.RenderType.lines());
    }
}
