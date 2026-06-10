package net.noodle.repophys.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.noodle.repophys.Repophys;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ModNetworking {
    public static final Set<UUID> ACTIVE_HOLDERS = new HashSet<>();
    public static final ConcurrentHashMap<UUID, GrabData> ACTIVE_GRABS = new ConcurrentHashMap<>();

    public record GrabData(int entityId, double tx, double ty, double tz) {}

    public static void init(IEventBus modBus) {
        modBus.addListener(ModNetworking::registerPackets);

        // Connect our tick listener up to the global game event loops
        NeoForge.EVENT_BUS.register(ModNetworking.class);
    }

    private static void registerPackets(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(Repophys.MODID).versioned("1.0.0");
        registrar.playToServer(ServerboundGrabPacket.TYPE, ServerboundGrabPacket.CODEC, (packet, context) -> {
            context.enqueueWork(() -> {
                if (!(context.player() instanceof ServerPlayer player)) return;
                UUID uuid = player.getUUID();

                if (packet.isReleased()) {
                    ACTIVE_GRABS.remove(uuid);

                    ServerLevel world = player.serverLevel();
                    Entity serverEntity = world.getEntity(packet.entityId());
                    if (serverEntity != null && serverEntity.isAlive()) {
                        // Hand operations over to the dedicated physics file
                        PhysicsEngine.executeThrowHandoff(player, world, serverEntity, packet);
                    } else {
                        PhysicsEngine.purgeUserData(uuid);
                    }
                } else {
                    ACTIVE_GRABS.put(uuid, new GrabData(packet.entityId(), packet.tx(), packet.ty(), packet.tz()));
                }
            });
        });
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (ACTIVE_GRABS.isEmpty()) return;

        ACTIVE_GRABS.forEach((playerUUID, grabData) -> {
            ServerPlayer player = event.getServer().getPlayerList().getPlayer(playerUUID);
            if (player == null) {
                ACTIVE_GRABS.remove(playerUUID);
                PhysicsEngine.purgeUserData(playerUUID);
                return;
            }

            ServerLevel world = player.serverLevel();
            Entity serverEntity = world.getEntity(grabData.entityId());

            if (serverEntity != null && serverEntity.isAlive()) {
                // Hand dragging ticks over to the physics file
                PhysicsEngine.tickGrabPhysics(player, world, serverEntity, grabData);
            } else {
                ACTIVE_GRABS.remove(playerUUID);
                PhysicsEngine.purgeUserData(playerUUID);
            }
        });
    }
}
