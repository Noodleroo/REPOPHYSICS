package net.noodle.repophys.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.noodle.repophys.Repophys;
import net.noodle.repophys.RepophysConfig;

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
    public static void onRenderFrame(net.neoforged.neoforge.client.event.RenderFrameEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        net.minecraft.world.entity.Entity grabbed = net.noodle.repophys.grab.PlayerGrabHandler.grabbedEntity;
        if (grabbed == null) return;

        net.minecraft.world.entity.Entity targetEntity = mc.level.getEntity(grabbed.getId());
        if (targetEntity == null) return;

        // 🛑 WE DO NOT OVERRIDE targetEntity.setPos() HERE ANYMORE!
        // Leaving this empty of position overrides allows the server's
        // incoming velocity packets (and dampening math) to control the movement.
    }

    @SubscribeEvent
    public static void onRenderTickPre(net.neoforged.neoforge.client.event.RenderFrameEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        net.minecraft.world.entity.Entity grabbed = net.noodle.repophys.grab.PlayerGrabHandler.grabbedEntity;
        if (grabbed == null) return;

        net.minecraft.world.entity.Entity targetEntity = mc.level.getEntity(grabbed.getId());
        if (targetEntity == null) return;

        // Get the real fractional partial tick for the frame rendering pass
        float partialTick = mc.getTimer().getGameTimeDeltaPartialTick(false);

        // Calculate the crosshair destination vector immediately
        double holdDistance = RepophysConfig.CLIENT.holdDistance.get();
        Vec3 lookVec = mc.player.getViewVector(partialTick);
        Vec3 targetDest = mc.player.getEyePosition(partialTick).add(
                lookVec.x * holdDistance,
                lookVec.y * holdDistance,
                lookVec.z * holdDistance
        );

        double lerpWeight = RepophysConfig.CLIENT.tractorBeamSmoothness.get();

        // If it's set to 1.0, instantly snap it. Otherwise, blend it smoothly.
        double actualX = net.minecraft.util.Mth.lerp((float) lerpWeight, (float) targetEntity.getX(), (float) targetDest.x);
        double actualY = net.minecraft.util.Mth.lerp((float) lerpWeight, (float) targetEntity.getY(), (float) targetDest.y);
        double actualZ = net.minecraft.util.Mth.lerp((float) lerpWeight, (float) targetEntity.getZ(), (float) targetDest.z);

        // Overwrite the position arrays BEFORE entity rendering processing begins
        targetEntity.setPos(actualX, actualY, actualZ);

        // Match old positions to kill trailing interpolation ghosting completely
        targetEntity.xOld = actualX;
        targetEntity.yOld = actualY;
        targetEntity.zOld = actualZ;
    }

    @SubscribeEvent
    public static void onRenderLevelStage(net.neoforged.neoforge.client.event.RenderLevelStageEvent event) {
        if (event.getStage() != net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        int targetId = net.noodle.repophys.grab.PlayerGrabHandler.clientHoveredEntityId;
        net.minecraft.world.entity.Entity grabbed = net.noodle.repophys.grab.PlayerGrabHandler.grabbedEntity;
        int activeHighlightId = (grabbed != null) ? grabbed.getId() : targetId;

        if (activeHighlightId == -1) return;
        net.minecraft.world.entity.Entity targetEntity = mc.level.getEntity(activeHighlightId);
        if (targetEntity == null) return;

        com.mojang.blaze3d.vertex.PoseStack poseStack = event.getPoseStack();
        net.minecraft.client.renderer.MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        com.mojang.blaze3d.vertex.VertexConsumer buffer = bufferSource.getBuffer(net.minecraft.client.renderer.RenderType.lines());

        poseStack.pushPose();
        double camX = event.getCamera().getPosition().x;
        double camY = event.getCamera().getPosition().y;
        double camZ = event.getCamera().getPosition().z;

        float partialTick = event.getPartialTick().getGameTimeDeltaTicks();

        // Let Minecraft naturally interpolate between the high-frequency server updates
        double actualX = net.minecraft.util.Mth.lerp(partialTick, targetEntity.xOld, targetEntity.getX());
        double actualY = net.minecraft.util.Mth.lerp(partialTick, targetEntity.yOld, targetEntity.getY());
        double actualZ = net.minecraft.util.Mth.lerp(partialTick, targetEntity.zOld, targetEntity.getZ());

        double renderX = actualX - camX;
        double renderY = actualY - camY;
        double renderZ = actualZ - camZ;

        poseStack.translate(renderX, renderY, renderZ);

        net.minecraft.world.phys.AABB localBox = targetEntity.getBoundingBox().move(-targetEntity.getX(), -targetEntity.getY(), -targetEntity.getZ());

        net.minecraft.client.renderer.LevelRenderer.renderLineBox(
                poseStack, buffer, localBox, 0.0f, 0.7f, 1.0f, 1.0f
        );
        poseStack.popPose();
        bufferSource.endBatch(net.minecraft.client.renderer.RenderType.lines());
    }
}
