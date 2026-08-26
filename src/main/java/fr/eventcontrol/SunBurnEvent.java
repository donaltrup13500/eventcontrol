package fr.eventcontrol;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

final class SunBurnEvent {
    private static boolean active;

    private SunBurnEvent() { }

    static boolean isActive() {
        return active;
    }

    static void start(MinecraftServer server) {
        active = true;
        server.getPlayerList().broadcastSystemMessage(
            net.minecraft.network.chat.Component.literal(
                "Soleil dangereux activé : couvrez-vous avec un bloc pendant le jour."), false);
    }

    static void stop(MinecraftServer server) {
        active = false;
        server.getPlayerList().broadcastSystemMessage(
            net.minecraft.network.chat.Component.literal("Soleil dangereux désactivé."), false);
    }

    static void tick(List<ServerPlayer> players) {
        if (!active) {
            return;
        }
        for (ServerPlayer player : players) {
            if (!player.isAlive() || player.isCreative() || player.isSpectator()
                || !player.level().isDay() || !player.level().canSeeSky(player.blockPosition().above())
                || player.isInWaterOrBubble()) {
                continue;
            }
            player.setRemainingFireTicks(Math.max(player.getRemainingFireTicks(), 40));
            player.hurt(player.damageSources().onFire(), 1.0F);
        }
    }
}