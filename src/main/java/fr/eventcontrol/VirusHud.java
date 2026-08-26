package fr.eventcontrol;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

public final class VirusHud {
    private VirusHud() {
    }

    public static void render(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }
        if (!TimerHud.isVirusActive()) {
            return;
        }
        LivingEntity virus = findVirus(minecraft);
        if (virus == null) {
            return;
        }

        GuiGraphics graphics = event.getGuiGraphics();
        int width = graphics.guiWidth();
        int panelWidth = 142;
        int panelHeight = 38;
        int x = width - panelWidth - 8;
        int y = 10;
        float healthRatio = Math.max(0.0F, Math.min(1.0F, virus.getHealth() / virus.getMaxHealth()));

        graphics.fill(x, y, x + panelWidth, y + panelHeight, 0xD90A111A);
        graphics.renderOutline(x, y, panelWidth, panelHeight, 0xFF4DE1C1);
        graphics.fill(x, y, x + 3, y + panelHeight, 0xFFEF596F);
        graphics.drawString(minecraft.font, "VIRUS", x + 9, y + 5, 0xFF8DF8E0, false);
        graphics.drawString(minecraft.font, levelText(virus), x + 9, y + 16, 0xFFB5C9D8, false);
        graphics.drawString(minecraft.font,
            (int) Math.ceil(virus.getHealth()) + " / " + (int) Math.ceil(virus.getMaxHealth()),
            x + 82, y + 16, 0xFFFFFFFF, false);
        graphics.fill(x + 9, y + 28, x + panelWidth - 9, y + 32, 0xFF25333D);
        graphics.fill(x + 9, y + 28, x + 9 + Math.round((panelWidth - 18) * healthRatio), y + 32, 0xFFEF596F);
    }

    private static LivingEntity findVirus(Minecraft minecraft) {
        for (Entity entity : minecraft.level.entitiesForRendering()) {
            if (entity instanceof LivingEntity living && entity.getCustomName() != null
                && entity.getCustomName().getString().startsWith("Virus - Niveau")) {
                return living;
            }
        }
        return null;
    }

    private static Component levelText(LivingEntity virus) {
        String name = virus.getCustomName() == null ? "Niveau ?" : virus.getCustomName().getString();
        return Component.literal(name.replace("Virus - ", ""));
    }
}
