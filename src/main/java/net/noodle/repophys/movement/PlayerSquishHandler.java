package net.noodle.repophys.movement;

import net.minecraft.world.entity.Pose;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
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

    @SubscribeEvent
    public static void processSquishLogic(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player == null) return;


        // Scan a 3D box up to 0.8 blocks above the head
        AABB ceilingCheckArea = player.getBoundingBox().expandTowards(0.0D, 0.8D, 0.0D);
        boolean hasCeilingAbove = !player.level().noCollision(player, ceilingCheckArea);
        boolean isCurrentlyShrunk = ScaleTypes.EYE_HEIGHT.getScaleData(player).getTargetScale() == EXTREME_EYE_TARGET;

        boolean isRolling = PlayerRollHandler.isRolling();

        if (player.isCrouching() || (hasCeilingAbove && isCurrentlyShrunk)|| isRolling) {
            // 1. Determine targets based on ceiling proximity
            float currentTargetEye = CROUCH_EYE_TARGET;
            float currentTargetModel = CROUCH_MODEL_TARGET;

            if (!player.isCrouching()) {
                player.setShiftKeyDown(true);
                // This updates the entity's network data parameter directly
                player.setPose(Pose.CROUCHING);

                // For modern versions, forcing standard crouching size overrides vanilla's visual reset
                player.refreshDimensions();
            }

            if (hasCeilingAbove) {
                currentTargetEye = EXTREME_EYE_TARGET;
                currentTargetModel = EXTREME_MODEL_TARGET;

                // Track that this specific player is trapped
                TRAPPED_PLAYERS.add(player.getUUID());
            } else {
                TRAPPED_PLAYERS.remove(player.getUUID());
            }

            // 2. Apply scales and force an instant snap down/further down
            if (ScaleTypes.EYE_HEIGHT.getScaleData(player).getTargetScale() != currentTargetEye) {
                ScaleTypes.EYE_HEIGHT.getScaleData(player).setTargetScale(currentTargetEye);
                ScaleTypes.HEIGHT.getScaleData(player).setTargetScale(currentTargetModel);

                // Directly hardcoded to snap immediately
                ScaleTypes.EYE_HEIGHT.getScaleData(player).setScale(currentTargetEye);
                ScaleTypes.HEIGHT.getScaleData(player).setScale(currentTargetModel);
            }

        } else {

            TRAPPED_PLAYERS.remove(player.getUUID());
            // 3. Reset back to adult height instantly when standing up
            if (ScaleTypes.EYE_HEIGHT.getScaleData(player).getTargetScale() != 1.0f) {
                player.setPose(Pose.STANDING);
                player.setShiftKeyDown(false);
                player.refreshDimensions();
                ScaleTypes.EYE_HEIGHT.getScaleData(player).setTargetScale(1.0f);
                ScaleTypes.HEIGHT.getScaleData(player).setTargetScale(1.0f);

                // Directly hardcoded to snap immediately
                ScaleTypes.EYE_HEIGHT.getScaleData(player).setScale(1.0f);
                ScaleTypes.HEIGHT.getScaleData(player).setScale(1.0f);
            }
        }
    }
}