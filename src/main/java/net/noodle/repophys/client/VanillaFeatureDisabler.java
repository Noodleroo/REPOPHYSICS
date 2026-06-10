package net.noodle.repophys.client;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.noodle.repophys.Repophys;
import net.noodle.repophys.network.ModNetworking;

@EventBusSubscriber(modid = Repophys.MODID)
public class VanillaFeatureDisabler {

    @SubscribeEvent
    public static void onItemPickup(ItemEntityPickupEvent.Pre event) {
        Player player = event.getPlayer();

        // --- FIXED: Use our new static server tracker map ---
        if (!player.level().isClientSide()) {
            if (ModNetworking.ACTIVE_HOLDERS.contains(player.getUUID())) {
                // Explicitly deny item pickup while hauling physics objects
                event.setCanPickup(net.neoforged.neoforge.common.util.TriState.FALSE);
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(net.neoforged.neoforge.client.event.ClientTickEvent.Pre event) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();

        if (mc.player != null && net.noodle.repophys.client.ClientInputHandler.isCurrentlyHolding) {
            // Prevent discarding equipped inventory items while holding an object
            mc.options.keyDrop.setDown(false);

            while (mc.options.keyDrop.consumeClick()) {
                // Drain the click buffer
            }
        }
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!event.getLevel().isClientSide()) {
            // --- FIXED: Check the active server holders set ---
            if (ModNetworking.ACTIVE_HOLDERS.contains(event.getEntity().getUUID())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onMobSpawn(FinalizeSpawnEvent event) {
        if (event.getSpawnType() == MobSpawnType.NATURAL && event.getEntity() instanceof Creeper) {
            event.setCanceled(true);
        }
    }
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        net.minecraft.world.entity.player.Player player = event.getEntity();

        // Only run this logic on the logical server side
        if (player.level().isClientSide) return;

        // Force hunger to stay locked full so vanilla healing doesn't mess with custom power-up health mechanics
        if (player.getFoodData().needsFood() || player.getHealth() < player.getMaxHealth()) {
            player.getFoodData().setFoodLevel(20);
        }

        // Suppress vanilla sprinting exhaustion so it doesn't drain hunger while sprinting
        if (player.isSprinting()) {
            player.getFoodData().addExhaustion(-0.2F);
        }
    }

    // --- 2. DISABLE VANILLA FALL DAMAGE BUFFER FOR EXTRA JUMPS ---
    @SubscribeEvent
    public static void onPlayerFall(LivingFallEvent event) {
        if (event.getEntity() instanceof net.minecraft.world.entity.player.Player player) {
            // If the player has unlocked any mid-air jumps from power-ups, reduce their fall damage impact
            if (PlayerPowerUps.extraJumpsCount > 0) {
                event.setDistance(event.getDistance() * 0.5F);
            }
        }
    }
}