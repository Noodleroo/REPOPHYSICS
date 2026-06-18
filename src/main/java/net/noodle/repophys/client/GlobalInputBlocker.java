package net.noodle.repophys.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.noodle.repophys.Repophys;
import net.noodle.repophys.network.GrabActionPacket;
import net.noodle.repophys.items.DebugSuperShovel;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = Repophys.MODID, value = Dist.CLIENT)
public class GlobalInputBlocker {

    private static boolean isHoldingObject = false;

    @SubscribeEvent
    public static void onAbsoluteLeftClickOverride(InputEvent.MouseButton.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null || mc.level == null) return;

        ItemStack heldItem = mc.player.getItemInHand(InteractionHand.MAIN_HAND);

        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (heldItem.getItem() instanceof DebugSuperShovel) {
                return;
            }

            event.setCanceled(true);

            if (event.getAction() == GLFW.GLFW_PRESS) {
                isHoldingObject = true;
                Minecraft.getInstance().getConnection().send(new GrabActionPacket(true));
            }
            else if (event.getAction() == GLFW.GLFW_RELEASE) {
                isHoldingObject = false;
                Minecraft.getInstance().getConnection().send(new GrabActionPacket(false));
            }
        }
    }

    /**
     * 👁️ RENDER THE LIVE GRADIENT ENERGY BEAM GLOW
     */
    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS) return;
        if (!isHoldingObject) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // Trace line from your hand right to the crosshairs vector center point
        Vec3 lookVec = mc.player.getLookAngle().normalize();
        Vec3 start = mc.player.getEyePosition().add(lookVec.scale(0.5)); // slightly offset from face
        Vec3 end = mc.player.getEyePosition().add(lookVec.scale(2.5));

        Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();

        // Match matrix viewspace offsets
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        VertexConsumer buffer = mc.renderBuffers().bufferSource().getBuffer(RenderType.lines());

        // Draw a gorgeous glowing energy beam connecting your crosshair line to the block!
        buffer.addVertex(poseStack.last().pose(), (float)start.x, (float)start.y, (float)start.z)
                .setColor(0.3F, 1.0F, 0.3F, 0.8F) // High neon green core glow
                .setNormal(poseStack.last(), 0.0F, 1.0F, 0.0F);

        buffer.addVertex(poseStack.last().pose(), (float)end.x, (float)end.y, (float)end.z)
                .setColor(0.1F, 0.8F, 0.1F, 0.2F) // Soft transparent dissipation tip
                .setNormal(poseStack.last(), 0.0F, 1.0F, 0.0F);

        poseStack.popPose();
        mc.renderBuffers().bufferSource().endBatch();
    }
}
