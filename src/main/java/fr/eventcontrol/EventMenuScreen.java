package fr.eventcontrol;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class EventMenuScreen extends AbstractContainerScreen<EventMenu> {
    private static final int BASE_WIDTH = 420;
    private static final int BASE_HEIGHT = 380;
    private static final int BUTTON_WIDTH = 166;
    private static final int BUTTON_HEIGHT = 24;
    private float uiScale = 1.0F;
    private int scrollOffset;

    public EventMenuScreen(EventMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = BASE_WIDTH;
        this.imageHeight = BASE_HEIGHT;
        this.inventoryLabelY = 1000;
    }

    @Override
    protected void init() {
        super.init();
        this.clearWidgets();

        this.uiScale = Math.min(1.0F, Math.min((this.width - 20.0F) / BASE_WIDTH,
            (this.height - 20.0F) / BASE_HEIGHT));
        this.imageWidth = ui(BASE_WIDTH);
        this.imageHeight = ui(BASE_HEIGHT);
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        rebuildButtons();
    }

    private void rebuildButtons() {
        this.clearWidgets();
        int startX = this.leftPos + ui(28);
        int startY = this.topPos + ui(72) - ui(scrollOffset);

        addActionButton(101, "Inventaire", startX, startY, ui(BUTTON_WIDTH), ui(BUTTON_HEIGHT));
        addActionButton(102, "Inv. partagé", startX + ui(170), startY, ui(BUTTON_WIDTH), ui(BUTTON_HEIGHT));

        addActionButton(103, "Bordure", startX, startY + ui(25), ui(BUTTON_WIDTH), ui(BUTTON_HEIGHT));
        addActionButton(104, "Lave", startX + ui(170), startY + ui(25), ui(BUTTON_WIDTH), ui(BUTTON_HEIGHT));

        addActionButton(105, "Taille", startX, startY + ui(50), ui(BUTTON_WIDTH), ui(BUTTON_HEIGHT));
        addActionButton(106, "Vie partagée", startX + ui(170), startY + ui(50), ui(BUTTON_WIDTH), ui(BUTTON_HEIGHT));

        addActionButton(107, "Death Swap", startX, startY + ui(75), ui(BUTTON_WIDTH), ui(BUTTON_HEIGHT));
        addActionButton(108, "Effets", startX + ui(170), startY + ui(75), ui(BUTTON_WIDTH), ui(BUTTON_HEIGHT));

        addActionButton(109, "Vitesse", startX, startY + ui(100), ui(BUTTON_WIDTH), ui(BUTTON_HEIGHT));
        addActionButton(110, "Stop all", startX + ui(170), startY + ui(100), ui(BUTTON_WIDTH), ui(BUTTON_HEIGHT));
        addActionButton(112, "Virus", startX, startY + ui(125), ui(BUTTON_WIDTH), ui(BUTTON_HEIGHT));
        addActionButton(113, "Saut mortel", startX + ui(170), startY + ui(125), ui(BUTTON_WIDTH), ui(BUTTON_HEIGHT));
        addActionButton(114, "Soleil dangereux", startX, startY + ui(150), ui(BUTTON_WIDTH), ui(BUTTON_HEIGHT));

        addActionButton(111, "Fermer", this.leftPos + ui(127), this.topPos + ui(340),
            ui(BUTTON_WIDTH), ui(BUTTON_HEIGHT));
    }

    private int ui(int value) {
        return Math.round(value * this.uiScale);
    }

    private void addActionButton(int id, String text, int x, int y, int width, int height) {
        int minX = this.leftPos + ui(10);
        int maxX = this.leftPos + this.imageWidth - width - ui(10);
        int minY = this.topPos + ui(62);
        int maxY = this.topPos + ui(330) - height;
        if (id != 111 && (y < minY || y + height > maxY + height)) {
            return;
        }
        x = Math.max(minX, Math.min(x, maxX));
        y = id == 111 ? y : Math.max(minY, Math.min(y, maxY));
        this.addRenderableWidget(new EventButton(x, y, width, height, Component.literal(text), button -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft != null && minecraft.gameMode != null) {
                minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, id);
            }
        }));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseX < leftPos + ui(20) || mouseX > leftPos + imageWidth - ui(20)
            || mouseY < topPos + ui(62) || mouseY > topPos + ui(330)) {
            return false;
        }
        int previousOffset = scrollOffset;
        scrollOffset = Math.max(0, Math.min(180, scrollOffset - (int) Math.signum(scrollY) * 25));
        if (previousOffset != scrollOffset) {
            rebuildButtons();
        }
        return true;
    }

    private static class EventButton extends Button {
        private EventButton(int x, int y, int width, int height, Component message, OnPress onPress) {
            super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            int background = isHovered() ? 0xFF245365 : 0xFF162936;
            graphics.fill(getX(), getY(), getX() + width, getY() + height, background);
            graphics.renderOutline(getX(), getY(), width, height, isHovered() ? 0xFF8DF8E0 : 0xFF44677A);
            graphics.drawCenteredString(Minecraft.getInstance().font, getMessage(),
                getX() + width / 2, getY() + (height - 8) / 2, isHovered() ? 0xFFFFFFFF : 0xFFB5C9D8);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        int w = this.imageWidth;
        int h = this.imageHeight;

        graphics.fill(0, 0, this.width, this.height, 0xB8070B12);
        graphics.fill(0, 0, this.width, this.height / 2, 0x1800D9FF);
        graphics.fill(x, y, x + w, y + h, 0xFF070B12);
        graphics.fill(x + ui(3), y + ui(3), x + w - ui(3), y + h - ui(3), 0xFF111A26);
        graphics.renderOutline(x, y, w, h, 0xFF3B526B);
        graphics.fill(x + ui(12), y + ui(12), x + w - ui(12), y + ui(58), 0xFF172C3A);
        graphics.fill(x + ui(12), y + ui(56), x + w - ui(12), y + ui(58), 0xFF4DE1C1);
        graphics.drawString(this.font, "EVENT CONTROL", x + ui(28), y + ui(22), 0xFF8DF8E0, false);
        graphics.drawString(this.font, "ADMIN CONSOLE  /  1.3.4", x + ui(28), y + ui(38), 0xFF7893A8, false);
        graphics.enableScissor(x + ui(20), y + ui(62), x + w - ui(20), y + ui(330));
        int offset = ui(scrollOffset);
        graphics.fill(x + ui(20), y + ui(68) - offset, x + w - ui(20), y + ui(270) - offset, 0xFF0B141D);
        graphics.renderOutline(x + ui(20), y + ui(68) - offset, w - ui(40), ui(202), 0xFF263B4B);
        graphics.fill(x + ui(20), y + ui(68) - offset, x + w - ui(20), y + ui(70) - offset, 0xFF4DE1C1);
        graphics.drawString(this.font, "MODULES D'ÉVÉNEMENT", x + ui(28), y + ui(74) - offset, 0xFFB5C9D8, false);
        graphics.fill(x + ui(20), y + ui(266) - offset, x + w - ui(20), y + ui(330) - offset, 0xFF13202A);
        graphics.renderOutline(x + ui(20), y + ui(266) - offset, w - ui(40), ui(64), 0xFF2B4555);
        graphics.drawString(this.font, "ÉTAT DU SERVEUR", x + ui(28), y + ui(272) - offset, 0xFFB5C9D8, false);
        graphics.drawString(this.font, "LAVE " + (menu.isLavaActive() ? "ACTIVE" : "OFF"), x + ui(28), y + ui(287) - offset,
            menu.isLavaActive() ? 0xFFFF725E : 0xFF8D9AAF, false);
        graphics.drawString(this.font, "INV " + (menu.isSharedInventoryActive() ? "PARTAGÉ" : "NORMALE"), x + ui(128), y + ui(287) - offset,
            menu.isSharedInventoryActive() ? 0xFF7AE38B : 0xFF9BA7BA, false);
        graphics.drawString(this.font, "VIE " + (menu.isSharedHealthActive() ? "PARTAGÉE" : "NORMALE"), x + ui(270), y + ui(287) - offset,
            menu.isSharedHealthActive() ? 0xFF7AE38B : 0xFF9BA7BA, false);
        graphics.drawString(this.font, "BORDURE " + (menu.isBorderActive() ? "ON" : "OFF"), x + ui(28), y + ui(302) - offset,
            menu.isBorderActive() ? 0xFF9DE3FF : 0xFF8D9AAF, false);
        graphics.drawString(this.font, "VITESSE x" + menu.displayedSpeed(), x + ui(160), y + ui(302) - offset, 0xFFFFD166, false);
        graphics.drawString(this.font, "VIRUS " + (menu.isVirusActive() ? "ACTIF" : "OFF"), x + ui(270), y + ui(302) - offset,
            menu.isVirusActive() ? 0xFFFF5252 : 0xFF8D9AAF, false);
        graphics.drawString(this.font, "SAUT " + (menu.isJumpDeathActive() ? "ACTIF" : "OFF"), x + ui(28), y + ui(317) - offset,
            menu.isJumpDeathActive() ? 0xFFFF5252 : 0xFF8D9AAF, false);
        graphics.drawString(this.font, "SOLEIL " + (menu.isSunBurnActive() ? "ACTIF" : "OFF"), x + ui(160), y + ui(317) - offset,
            menu.isSunBurnActive() ? 0xFFFFD166 : 0xFF8D9AAF, false);
        graphics.disableScissor();
        if (scrollOffset > 0) {
            graphics.fill(x + w - ui(13), y + ui(66), x + w - ui(9), y + ui(330), 0xFF263B4B);
            int thumbY = y + ui(66 + scrollOffset * 264 / 180);
            graphics.fill(x + w - ui(13), thumbY, x + w - ui(9), thumbY + ui(42), 0xFF4DE1C1);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    @Override
    protected void renderSlot(GuiGraphics graphics, Slot slot) {
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}
