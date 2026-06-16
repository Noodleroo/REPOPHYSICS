package net.noodle.repophys.movement;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;

@EventBusSubscriber(modid = "repophys", value = Dist.CLIENT)
public class SlideHandler {

    private static boolean isSliding = false;
    private static int slideTicksLeft = 0;
    private static final int MAX_SLIDE_DURATION = 8; // Lasts 3/4ths of a second

    // Slide momentum vectors
    private static double slideDirX = 0;
    private static double slideDirZ = 0;
    private static double currentSlideSpeed = 0;

    private static final double INITIAL_SPEED = 0.60;  // Starting velocity magnitude
    private static final double DECAY_RATE = 0.035;    // Fixed loss of speed per tick (linear decay)

    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null || mc.level == null || mc.screen != null) {
            return;
        }

        // 1. Trigger slide: Sprinting, presses Shift, on the ground, and moving
        if (!isSliding && player.isSprinting() && event.getInput().shiftKeyDown && player.onGround()) {
            if (player.getDeltaMovement().horizontalDistanceSqr() > 0.01) {
                isSliding = true;
                slideTicksLeft = MAX_SLIDE_DURATION;
                player.setSprinting(false);

                // Lock in the direction the player is looking when they hit shift
                float yaw = player.getYRot();
                slideDirX = -Math.sin(Math.toRadians(yaw));
                slideDirZ = Math.cos(Math.toRadians(yaw));

                // Set initial fast speed
                currentSlideSpeed = INITIAL_SPEED;
            }
        }

        // 2. Active Slide Processing
        if (isSliding) {
            slideTicksLeft--;

            // Force visual crouching stance
            event.getInput().shiftKeyDown = true;

            // Turn off normal WASD movement input values so they don't fight the slide vectors
            event.getInput().forwardImpulse = 0.0F;
            event.getInput().leftImpulse = 0.0F;

            // Decay the slide speed cleanly over time instead of letting friction break it instantly
            currentSlideSpeed -= DECAY_RATE;

            // Forcefully overwrite the player's movement vectors to completely ignore ground friction
            player.setDeltaMovement(new Vec3(
                    slideDirX * currentSlideSpeed,
                    player.getDeltaMovement().y, // Keep gravity functioning normally
                    slideDirZ * currentSlideSpeed
            ));

            // 3. Stop conditions: Timer ends, speed drops too low, or player releases shift
            if (slideTicksLeft <= 0 || !mc.options.keyShift.isDown() || currentSlideSpeed <= 0.1) {
                isSliding = false;
                slideTicksLeft = 0;
            }
        }
    }
}