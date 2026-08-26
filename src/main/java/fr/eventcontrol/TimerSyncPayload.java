package fr.eventcontrol;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

record TimerSyncPayload(boolean lavaActive, int lavaRemaining, int lavaTotal,
                        boolean deathSwapActive, int deathSwapRemaining, int deathSwapTotal,
                        boolean effectsActive, int effectsRemaining, int effectsTotal,
                        boolean virusActive, int virusRemaining, int virusTotal)
    implements CustomPacketPayload {
    static final Type<TimerSyncPayload> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(EventControl.MOD_ID, "timer_sync"));
    static final StreamCodec<RegistryFriendlyByteBuf, TimerSyncPayload> STREAM_CODEC = StreamCodec.of(
        (buffer, payload) -> {
            buffer.writeBoolean(payload.lavaActive());
            buffer.writeVarInt(payload.lavaRemaining());
            buffer.writeVarInt(payload.lavaTotal());
            buffer.writeBoolean(payload.deathSwapActive());
            buffer.writeVarInt(payload.deathSwapRemaining());
            buffer.writeVarInt(payload.deathSwapTotal());
            buffer.writeBoolean(payload.effectsActive());
            buffer.writeVarInt(payload.effectsRemaining());
            buffer.writeVarInt(payload.effectsTotal());
            buffer.writeBoolean(payload.virusActive());
            buffer.writeVarInt(payload.virusRemaining());
            buffer.writeVarInt(payload.virusTotal());
        },
        buffer -> new TimerSyncPayload(
            buffer.readBoolean(), buffer.readVarInt(), buffer.readVarInt(),
            buffer.readBoolean(), buffer.readVarInt(), buffer.readVarInt(),
            buffer.readBoolean(), buffer.readVarInt(), buffer.readVarInt(),
            buffer.readBoolean(), buffer.readVarInt(), buffer.readVarInt()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}