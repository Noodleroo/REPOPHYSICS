package net.noodle.repophys.grab;

public class SableGrabEngine {
/*
    private static int networkThrottleTicks = 0;

    public static void applyTractorBeamPhysics(Player player, Entity entity) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        double holdDistance = RepophysConfig.CLIENT.holdDistance.get();

        // 1. Calculate the ideal target floating destination in front of the player
        Vec3 lookVec = player.getViewVector(1.0F);
        Vec3 targetPos = player.getEyePosition(1.0F).add(
                lookVec.x * holdDistance,
                lookVec.y * holdDistance,
                lookVec.z * holdDistance
        );

        // 2. FIX: Use absolute native entity position vectors to prevent shadow-realm coordinates!
        Vec3 globalEntityPos = entity.position();

        // 3. PD-Controller Calculus (Spring & Damping forces)
        Vec3 distanceDelta = targetPos.subtract(globalEntityPos);
        Vec3 currentVelocity = entity.getDeltaMovement();

        double springStrength = 0.3D;   // Pull strength
        double dampingEffect = 0.15D;  // Friction to prevent infinite wobbling

        double motionX = (distanceDelta.x * springStrength) - (currentVelocity.x * dampingEffect);
        double motionY = (distanceDelta.y * springStrength) - (currentVelocity.y * dampingEffect);
        double motionZ = (distanceDelta.z * springStrength) - (currentVelocity.z * dampingEffect);

        // 4. Tight clamp values to completely prevent world boundary clipping errors
        double maxForce = 0.4D; // Reduced slightly for safer movement limits
        motionX = Mth.clamp(motionX, -maxForce, maxForce);
        motionY = Mth.clamp(motionY, -maxForce, maxForce);
        motionZ = Mth.clamp(motionZ, -maxForce, maxForce);

        // 5. Send the raw velocity packet package under a standard 2-tick network cap
        if (networkThrottleTicks > 0) {
            networkThrottleTicks--;
        } else {
            PacketDistributor.sendToServer(new ServerboundGrabPacket(
                    entity.getId(),
                    motionX,
                    motionY,
                    motionZ,
                    false
            ));
            networkThrottleTicks = 2;
        }
    }

 */
}