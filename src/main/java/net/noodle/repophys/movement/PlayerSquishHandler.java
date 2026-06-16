package net.noodle.repophys.movement;

import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.noodle.repophys.Repophys;
import virtuoel.pehkui.api.ScaleTypes;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = Repophys.MODID)
public class PlayerSquishHandler {

    // --- BASE CROUCH VALUES ---
    private static final float CROUCH_EYE_TARGET = 0.6f;
    private static final float CROUCH_MODEL_TARGET = 0.6f;

    // --- REPO TIGHT SPACE VALUES ---
    private static final float EXTREME_EYE_TARGET = 0.2f;
    private static final float EXTREME_MODEL_TARGET = 0.25f;

    public static final Set<UUID> TRAPPED_PLAYERS = new HashSet<>();

    /**
     * ✨ NEW CLIENT INTERCEPT: Forces the game's movement input handler
     * to believe shift is still held down if a ceiling is detected.
     */
    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        Player player = event.getEntity();
        if (player == null || player.level() == null) return;

        // Construct a clean full-height check bounding box above the player's current position
        AABB ceilingCheckArea = player.getBoundingBox().expandTowards(0.0D, 0.8D, 0.0D);
        boolean hasCeilingAbove = !player.level().noCollision(player, ceilingCheckArea);

        // If there's a block overhead and we are on the ground, hijack the input vector
        if (hasCeilingAbove && player.onGround()) {
            event.getInput().shiftKeyDown = true;

            // Apply a minor walking speed penalty to simulate standard crawling under blocks
            event.getInput().forwardImpulse *= 0.5F;
            event.getInput().leftImpulse *= 0.5F;
        }
    }

    /**
     * Your original sizing tick handler, now perfectly paired with the input listener
     */
    @SubscribeEvent
    public static void processSquishLogic(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player == null) return;

        AABB ceilingCheckArea = player.getBoundingBox().expandTowards(0.0D, 0.8D, 0.0D);
        boolean hasCeilingAbove = !player.level().noCollision(player, ceilingCheckArea);
        boolean isCurrentlyShrunk = ScaleTypes.EYE_HEIGHT.getScaleData(player).getTargetScale() == EXTREME_EYE_TARGET;

        boolean isRolling = false;
        try {
            isRolling = PlayerRollHandler.isRolling();
        } catch (NoClassDefFoundError | NoSuchMethodError ignored) {
        }

        // Check if the player is crouching, trapped, or rolling
        if (player.isCrouching() || hasCeilingAbove || (hasCeilingAbove && isCurrentlyShrunk) || isRolling) {
            float currentTargetEye = CROUCH_EYE_TARGET;
            float currentTargetModel = CROUCH_MODEL_TARGET;

            if (!player.isCrouching()) {
                player.setShiftKeyDown(true);
                player.setPose(Pose.CROUCHING);
                player.refreshDimensions();
            }

            if (hasCeilingAbove) {
                currentTargetEye = EXTREME_EYE_TARGET;
                currentTargetModel = EXTREME_MODEL_TARGET;
                TRAPPED_PLAYERS.add(player.getUUID());
            } else {
                TRAPPED_PLAYERS.remove(player.getUUID());
            }

            if (ScaleTypes.EYE_HEIGHT.getScaleData(player).getTargetScale() != currentTargetEye) {
                ScaleTypes.EYE_HEIGHT.getScaleData(player).setTargetScale(currentTargetEye);
                ScaleTypes.HEIGHT.getScaleData(player).setTargetScale(currentTargetModel);

                ScaleTypes.EYE_HEIGHT.getScaleData(player).setScale(currentTargetEye);
                ScaleTypes.HEIGHT.getScaleData(player).setScale(currentTargetModel);
            }

        } else {
            TRAPPED_PLAYERS.remove(player.getUUID());

            if (ScaleTypes.EYE_HEIGHT.getScaleData(player).getTargetScale() != 1.0f) {
                player.setPose(Pose.STANDING);
                player.setShiftKeyDown(false);
                player.refreshDimensions();
                ScaleTypes.EYE_HEIGHT.getScaleData(player).setTargetScale(1.0f);
                ScaleTypes.HEIGHT.getScaleData(player).setTargetScale(1.0f);

                ScaleTypes.EYE_HEIGHT.getScaleData(player).setScale(1.0f);
                ScaleTypes.HEIGHT.getScaleData(player).setScale(1.0f);
            }
        }
    }
}