package net.noodle.repophys.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.noodle.repophys.Repophys;
import net.noodle.repophys.grab.PlayerGrabHandler;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = Repophys.MODID, value = Dist.CLIENT)
public class ClientInputHandler {

    public static boolean isCurrentlyHolding = false;
    public static float customHandPoseTicks = 0.0F;

    /**
     * ✨ CONTINUOUS TICK LOOPER: Wakes up the PlayerGrabHandler
     * so it scans and drags objects every single client-side frame!
     */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.level != null) {
            // Keep the custom hand pose floating bob animation ticking smoothly
            if (isCurrentlyHolding) {
                customHandPoseTicks += 0.05F;
            }

            // Force the grab tick loop to evaluate every frame
            PlayerGrabHandler.executeEntityGrabTick();
        }
    }

    /**
     * ✨ MOUSE INTERCEPTOR PRE-PASS: Intercepts the left-click action
     * BEFORE vanilla processes it so you do not punch/break the blocks!
     */
    @SubscribeEvent
    public static void onMouseInputPre(InputEvent.MouseButton.Pre event) {
        // Button 0 is the standard left-click button!
        if (event.getButton() != 0) return;

        // Action 1 means the button was PRESSED DOWN
        if (event.getAction() == 1) {
            // Only activate if we are looking at a valid targeted block coordinate
            if (PlayerGrabHandler.clientHoveredBlockPos != null) {
                isCurrentlyHolding = true;
                customHandPoseTicks = 0.0F;

                // 🛑 CANCEL VANILLA PUNCH: Stops the game from breaking the block you are trying to grab!
                event.setCanceled(true);
            }
        }
        // Action 0 means the button was RELEASED (let go)
        else if (event.getAction() == 0) {
            if (isCurrentlyHolding) {
                PlayerGrabHandler.snapIgnoreTicks = 20;
                PlayerGrabHandler.forceRelease(false);
                isCurrentlyHolding = false;
                customHandPoseTicks = 0.0F;

                // Cancel vanilla release updates during the handoff phase
                event.setCanceled(true);
            }
        }
    }

    // 💾 Ensure these tracking variables remain at the top of your class file:
    private static int slideTicksLeft = 0;
    private static boolean wasCrouchingLastTick = false;
    private static boolean wasSprintingLastTick = false;
    private static net.minecraft.world.phys.Vec3 slideDirection = net.minecraft.world.phys.Vec3.ZERO;

    @SubscribeEvent
    public static void onMovementInput(net.neoforged.neoforge.client.event.MovementInputUpdateEvent event) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        boolean isCrouchPressed = event.getInput().shiftKeyDown;
        boolean isSprintingNow = mc.player.isSprinting();

        // 1. ✨ CLEAN COLLISION RESOLUTION: Calculate the standing box directly using coordinates!
        // This replicates the player's full 0.6w x 1.8h standing hitbox boundary footprint.
        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();

        net.minecraft.world.phys.AABB simulatedStandingBox = new net.minecraft.world.phys.AABB(
                x - 0.3D, y, z - 0.3D,
                x + 0.3D, y + 1.8D, z + 0.3D
        );

        // Ask the level collision map if our simulated head space intersects a solid block or slab
        boolean isBlockedByCeiling = !mc.level.noCollision(mc.player, simulatedStandingBox);

        // 2. THE ACTIVE SLIDE LOOP
        if (slideTicksLeft > 0) {
            slideTicksLeft--;

            // Lock player into crouched state during active slide frames
            event.getInput().shiftKeyDown = true;

            // Smooth speed decay (8-tick layout structure)
            double currentSlideSpeed = 0.4D + ((double) slideTicksLeft / 30.0D) * 1.9D;

            net.minecraft.world.phys.Vec3 activeSlideVelocity = new net.minecraft.world.phys.Vec3(
                    slideDirection.x * currentSlideSpeed,
                    mc.player.getDeltaMovement().y,
                    slideDirection.z * currentSlideSpeed
            );

            mc.player.setDeltaMovement(activeSlideVelocity);
        }
        // 3. CEILING OVERRIDE: Keep crouching if your head is trapped under a slab block!
        else if (isBlockedByCeiling) {
            event.getInput().shiftKeyDown = true;
        }

        // 4. TRIGGER PASS: Fires when you hit the crouch key while sprinting
        if (mc.player.onGround() && isCrouchPressed && !wasCrouchingLastTick) {
            if (isSprintingNow || wasSprintingLastTick) {
                // Only slide if we aren't already stuck under a roof ceiling
                if (slideTicksLeft <= 0 && !isBlockedByCeiling) {
                    slideTicksLeft = 8;

                    net.minecraft.world.phys.Vec3 lookAngle = mc.player.getLookAngle();
                    slideDirection = new net.minecraft.world.phys.Vec3(lookAngle.x, 0, lookAngle.z).normalize();
                }
            }
        }

        // Save tracking states for the next frame transition
        wasCrouchingLastTick = isCrouchPressed;
        wasSprintingLastTick = isSprintingNow;
    }

}
