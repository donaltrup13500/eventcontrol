package fr.eventcontrol;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

final class TimerHud {
    private static TimerSyncPayload state = new TimerSyncPayload(false, 0, 1, false, 0, 1,
        false, 0, 1, false, 0, 1);

    private TimerHud() { }

    static void handle(TimerSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> state = payload);
    }

    static boolean isVirusActive() {
        return state.virusActive();
    }

    static void render(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }
        GuiGraphics graphics = event.getGuiGraphics();
        int x = 10;
        int y = 10;
        int width = 178;
        int height = 24;
        int gap = 3;
        y = renderBar(graphics, x, y, width, height, "LAVE", state.lavaActive(),
            state.lavaRemaining(), state.lavaTotal(), 0xFFE35D6A, gap);
        y = renderBar(graphics, x, y, width, height, "DEATH SWAP", state.deathSwapActive(),
            state.deathSwapRemaining(), state.deathSwapTotal(), 0xFFB78CFF, gap);
        y = renderBar(graphics, x, y, width, height, "EFFET", state.effectsActive(),
            state.effectsRemaining(), state.effectsTotal(), 0xFF55B7E8, gap);
        renderBar(graphics, x, y, width, height, "VIRUS", state.virusActive(),
            state.virusRemaining(), state.virusTotal(), 0xFFEF596F, gap);
    }

    private static int renderBar(GuiGraphics graphics, int x, int y, int width, int height,
                                 String label, boolean active, int remaining, int total, int color, int gap) {
        if (!active) {
            return y;
        }
        float progress = Math.max(0.0F, Math.min(1.0F, (float) remaining / Math.max(1, total)));
        graphics.fill(x, y, x + width, y + height, 0xD90A111A);
        graphics.renderOutline(x, y, width, height, 0xFF3B526B);
        graphics.fill(x, y, x + 3, y + height, color);
        graphics.drawString(Minecraft.getInstance().font, label, x + 8, y + 4, 0xFFFFFFFF, false);
        graphics.drawString(Minecraft.getInstance().font, formatTime(remaining),
            x + width - 42, y + 4, 0xFFFFFFFF, false);
        graphics.fill(x + 8, y + 16, x + width - 8, y + 20, 0xFF25333D);
        graphics.fill(x + 8, y + 16, x + 8 + Math.round((width - 16) * progress), y + 20, color);
        return y + height + gap;
    }

    private static String formatTime(int ticks) {
        int seconds = Math.max(0, ticks / 20);
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }
}