package net.noodle.repophys.movement;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.noodle.repophys.Repophys;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = Repophys.MODID, value = Dist.CLIENT)
public class PlayerRollHandler {

    // ==========================================
    //       🛠️ USER CONFIGURATION AREA 🛠️
    // ==========================================
    public static final int TOTAL_ROLL_DURATION = 12;  // How long the roll lasts (in game ticks)
    public static final int ROLL_COOLDOWN_TICKS = 30;  // Delay before you can roll again (20 ticks = 1 second)

    public static final float LAUNCH_FORCE = 0.75f;    // Forward speed boost multiplier
    public static final float UPWARD_HOP = 0.1f;       // Small vertical pop up so you don't stick to slabs

    // Camera Rotation Settings
    public static final float MAX_SCREEN_ROLL = 360.0f; // Total degrees to spin sideways (360 = full barrel roll)
    public static final float MAX_FORWARD_TILT = 35.0f; // Maximum degrees to tilt head forward/down mid-roll
    // ==========================================

    public static int rollTicks = 0;
    public static int rollCooldown = 0;

    public static boolean isRolling() {
        return rollTicks > 0;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (rollTicks > 0) rollTicks--;
        if (rollCooldown > 0) rollCooldown--;
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        if (event.getKey() == GLFW.GLFW_KEY_Q && event.getAction() == GLFW.GLFW_PRESS) {
            if (rollTicks == 0 && rollCooldown == 0 && mc.player.zza > 0) {

                rollTicks = TOTAL_ROLL_DURATION;
                rollCooldown = ROLL_COOLDOWN_TICKS;

                float lookAngle = mc.player.getYRot() * ((float)Math.PI / 180F);
                double motionX = -Math.sin(lookAngle) * LAUNCH_FORCE;
                double motionZ = Math.cos(lookAngle) * LAUNCH_FORCE;

                Vec3 currentVelocity = mc.player.getDeltaMovement();
                mc.player.setDeltaMovement(motionX, UPWARD_HOP, motionZ);
            }
        }
    }

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !isRolling()) return;

        // 1. Calculate precise frame progress (0.0 to 1.0)
        float exactTicksRemaining = (float) ((float) rollTicks - event.getPartialTick());
        if (exactTicksRemaining < 0) exactTicksRemaining = 0;

        float progress = 1.0f - (exactTicksRemaining / (float) TOTAL_ROLL_DURATION);

        // 2. Apply Sideways Spin (Roll Matrix)
        //float activeRoll = -(progress * MAX_SCREEN_ROLL);
        event.setRoll(0.0f);
        float forwardRollAngle = progress * 360.0f;


        // 3. Apply Forward Head-Tilt (Pitch Matrix)
        // Math.sin(progress * Math.PI) creates a smooth arc: starts at 0,
        // peaks at 1.0 exactly halfway through the roll, and returns to 0 at the end.
        //float forwardTiltModifier = (float) Math.sin(progress * Math.PI) * MAX_FORWARD_TILT;

        // Add the forward tilt on top of the player's existing look angle
        event.setPitch(mc.player.getXRot() + forwardRollAngle);
    }

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        if (player == null || rollTicks <= 0) return;

        Minecraft mc = Minecraft.getInstance();

        // 1. Calculate precise frame progress (0.0 to 1.0)
        float exactTicksRemaining = (float) rollTicks - event.getPartialTick();
        if (exactTicksRemaining < 0) exactTicksRemaining = 0;

        float progress = 1.0f - (exactTicksRemaining / (float) TOTAL_ROLL_DURATION);

        // 2. Compute full 360 rotation angle
        float renderRollAngle = progress * 360.0f;

        PoseStack poseStack = event.getPoseStack();

        // 3. Translate pivot to center of torso
        float pivotHeight = player.getDimensions(player.getPose()).height() / 2.0f;
        poseStack.translate(0.0D, pivotHeight, 0.0D);

        // 4. Match the entity's raw body yaw rotation so the forward flip
        // follows the direction their feet are pointing, not universal North/South!
        float bodyYaw = player.yBodyRot;
        poseStack.mulPose(Axis.YP.rotationDegrees(-bodyYaw));

        // 5. Execute the forward somersault on the X-axis
        poseStack.mulPose(Axis.XP.rotationDegrees(renderRollAngle));

        // 6. Restore their look orientation matrix layers
        poseStack.mulPose(Axis.YP.rotationDegrees(bodyYaw));

        // 7. Translate pivot point back down to ground level
        poseStack.translate(0.0D, -pivotHeight, 0.0D);
    }
}