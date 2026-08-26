package fr.eventcontrol;

import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

final class JumpDeathEvent {
    private static boolean active;
    private static final Map<UUID, Boolean> previousGroundStates = new HashMap<>();

    private JumpDeathEvent() { }

    static boolean isActive() {
        return active;
    }

    static void start(List<ServerPlayer> players) {
        active = true;
        previousGroundStates.clear();
        for (ServerPlayer player : players) {
            previousGroundStates.put(player.getUUID(), player.onGround());
        }
    }

    static void tick(List<ServerPlayer> players) {
        if (!active) {
            return;
        }
        for (ServerPlayer player : players) {
            boolean wasOnGround = previousGroundStates.getOrDefault(player.getUUID(), true);
            boolean onGround = player.onGround();
            previousGroundStates.put(player.getUUID(), onGround);
            if (player.isAlive() && wasOnGround && !onGround
                && player.getDeltaMovement().y > 0.0D) {
                explodeAndKill(player);
            }
        }
    }

    static void stop() {
        active = false;
        previousGroundStates.clear();
    }

    private static void explodeAndKill(ServerPlayer player) {
        player.serverLevel().explode(null, player.getX(), player.getY(), player.getZ(),
            4.0F, player.level().getLevelData().isRaining(),
            net.minecraft.world.level.Level.ExplosionInteraction.TNT);
        player.kill();
    }
}