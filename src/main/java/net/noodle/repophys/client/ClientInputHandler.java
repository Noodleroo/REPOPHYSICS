package net.noodle.repophys.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.noodle.repophys.Repophys;
import net.noodle.repophys.grab.PlayerGrabHandler;
import net.noodle.repophys.movement.PlayerRollHandler;
import net.noodle.repophys.movement.PlayerSquishHandler;
import net.noodle.repophys.network.ServerboundGrabPacket;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = Repophys.MODID, value = Dist.CLIENT)
public class ClientInputHandler {
    public static int clientHoveredEntityId = -1;

    public static boolean isCurrentlyHolding = false;
    public static boolean isHeavyCrouching = false;
    public static int slideTicks = 0;
    private static boolean wasSprintingLastTick = false;
    private static boolean wasSneakingLastTick = false;
    private static boolean isMousePhysicallyDown = false;
    public static float customHandPoseTicks = 0.0f;

    @SubscribeEvent
    public static void onClientTickPre(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        // 1. DIRECT HARDWARE CHECK
        long windowHandle = mc.getWindow().getWindow();
        boolean isLeftClickHeldDown = GLFW.glfwGetMouseButton(windowHandle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        isMousePhysicallyDown = isLeftClickHeldDown;

        boolean hasValidTarget = PlayerGrabHandler.clientHoveredEntityId != -1
                || PlayerGrabHandler.grabbedEntity != null;

        if (isMousePhysicallyDown && mc.screen == null) {
            isCurrentlyHolding = true;
            customHandPoseTicks += 0.05f;
        } else {
            if (isCurrentlyHolding) {
                int droppedEntityId = -1;
                if (PlayerGrabHandler.grabbedEntity != null) {
                    droppedEntityId = PlayerGrabHandler.grabbedEntity.getId();
                    PlayerGrabHandler.lastLoggedEntityId = droppedEntityId;
                    PlayerGrabHandler.snapIgnoreTicks = 20;
                }
                PacketDistributor.sendToServer(
                        new ServerboundGrabPacket(droppedEntityId, 0, 0, 0, true)
                );
                PlayerGrabHandler.forceRelease(false);
            }

            isCurrentlyHolding = false;
            customHandPoseTicks = 0.0f;
        }

        // 2. Process physics dragging engine ticks
        net.noodle.repophys.grab.PlayerGrabHandler.executeEntityGrabTick();

        if (net.noodle.repophys.grab.PlayerGrabHandler.grabbedEntity != null) {
            int targetId = net.noodle.repophys.grab.PlayerGrabHandler.grabbedEntity.getId();
            Entity clientSideEntity = mc.level.getEntity(targetId);
            if (clientSideEntity != null) {
                clientSideEntity.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
                clientSideEntity.noPhysics = true;
            }
        }

        if (slideTicks > 0) slideTicks--;

        // Heavy crouching state calculation
        if (isCurrentlyHolding && mc.screen == null) {
            boolean isSneakPressed = mc.options.keyShift.isDown();
            if (isSneakPressed) {
                isHeavyCrouching = true;
                Vec3 currentMovement = mc.player.getDeltaMovement();
                if (currentMovement != null) {
                    mc.player.setDeltaMovement(currentMovement.x * 0.06D, currentMovement.y, currentMovement.z * 0.3D);
                }
            } else {
                isHeavyCrouching = false;
            }
        } else {
            isHeavyCrouching = false;
        }
    }

    @SubscribeEvent
    public static void onAbsoluteLeftClickOverride(InputEvent.MouseButton.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null || mc.level == null) return;

        if (event.getButton() == org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            event.setCanceled(true);

            if (event.getAction() == org.lwjgl.glfw.GLFW.GLFW_PRESS) {
                isMousePhysicallyDown = true;
                isCurrentlyHolding = true;
                customHandPoseTicks = 0.0f;

                mc.player.displayClientMessage(net.minecraft.network.chat.Component.literal("Holding"), true);
            }
        }
    }

    @SubscribeEvent
    public static void onMovementInputUpdate(MovementInputUpdateEvent event) {
        if (event.getEntity() == null) return;
        if (event.getEntity() instanceof LocalPlayer player) {
            boolean hasCeilingAbove = PlayerSquishHandler.TRAPPED_PLAYERS.contains(player.getUUID());
            boolean isSneakPressed = Minecraft.getInstance().options.keyShift.isDown();
            boolean isRolling = PlayerRollHandler.isRolling();
            boolean justPressedSneak = isSneakPressed && !wasSneakingLastTick;

            if (justPressedSneak && wasSprintingLastTick && slideTicks == 0 && !isRolling && player.onGround()) {
                slideTicks = 9;
            }

            wasSprintingLastTick = player.isSprinting();
            wasSneakingLastTick = isSneakPressed;

            boolean isSliding = slideTicks > 0;

            if (isSneakPressed || hasCeilingAbove || isRolling || isSliding) {
                isHeavyCrouching = true;
                if (player.input != null) {
                    player.input.shiftKeyDown = true;
                    if (isRolling) {
                        player.input.leftImpulse = 0f;
                        player.input.forwardImpulse = 1.0f;
                    } else if (isSliding) {
                        player.input.forwardImpulse = 1.0f;
                        player.input.leftImpulse = 0f;
                        float lookAngle = player.getYRot() * ((float) Math.PI / 180F);
                        double slideMultiplier = 0.14D * (slideTicks / 9.0D);
                        Vec3 vel = player.getDeltaMovement();
                        player.setDeltaMovement(vel.x + (-Math.sin(lookAngle) * slideMultiplier), vel.y, vel.z + (Math.cos(lookAngle) * slideMultiplier));
                    } else {
                        player.xxa *= 0.3f;
                        player.zza *= 0.2f;
                    }
                }
            } else {
                isHeavyCrouching = false;
            }
        }
    }
}