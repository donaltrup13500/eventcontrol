package fr.eventcontrol;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;

public class EventSubMenuScreen extends AbstractContainerScreen<AbstractContainerMenu> {
    private static final int WIDTH = 300;
    private final boolean inventoryView;
    private final Set<Integer> selectedSlots = new HashSet<>();

    public EventSubMenuScreen(AbstractContainerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.inventoryView = menu instanceof PlayerInventoryMenu;
        this.imageWidth = WIDTH;
        this.imageHeight = inventoryView ? 240 : 300;
        this.inventoryLabelY = 1000;
    }

    @Override
    protected void init() {
        super.init();
        this.clearWidgets();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        if (inventoryView) {
            addActionButton(53, "Retour", this.leftPos + 90, this.topPos + 210, 120, 24);
            return;
        }

        if (menu instanceof LavaMenu) {
            addGridButtons(new int[]{10, 11, 12, 13, 14, 15},
                new String[]{"5 min", "10 min", "15 min", "20 min", "25 min", "30 min"}, 3, 52);
            addGridButtons(new int[]{19, 20, 21}, new String[]{"Vitesse x1", "Vitesse x2", "Vitesse x3"}, 3, 112);
            addActionButton(23, "Démarrer / appliquer", leftPos + 32, topPos + 165, 130, 25);
            addActionButton(24, "Arrêter", leftPos + 172, topPos + 165, 96, 25);
            addActionButton(25, "Retour", leftPos + 130, topPos + 205, 120, 24);
        } else if (menu instanceof DeathSwapMenu) {
            addGridButtons(new int[]{10, 11, 12, 13}, new String[]{"1 min", "5 min", "10 min", "15 min"}, 4, 58);
            addActionButton(22, "Activer", leftPos + 32, topPos + 165, 96, 25);
            addActionButton(23, "Désactiver", leftPos + 136, topPos + 165, 96, 25);
            addActionButton(25, "Retour", leftPos + 130, topPos + 205, 120, 24);
        } else if (menu instanceof RandomEffectsMenu) {
            addGridButtons(playerSlots(),
                playerLabels(), 3, 52);
            addGridButtons(new int[]{19, 20, 21}, new String[]{"10 sec", "30 sec", "60 sec"}, 3, 145);
            addActionButton(23, "Activer", leftPos + 30, topPos + 215, 110, 25);
            addActionButton(24, "Désactiver", leftPos + 155, topPos + 215, 110, 25);
            addActionButton(25, "Retour", leftPos + 90, topPos + 250, 120, 24);
        } else if (menu instanceof GameSpeedMenu) {
            addGridButtons(new int[]{9, 10, 11, 12, 13, 14, 15, 16},
                new String[]{"x1", "x2", "x4", "x6", "x8", "x10", "x20", "x50"}, 4, 58);
            addActionButton(22, "Retour", leftPos + 130, topPos + 165, 120, 24);
        } else if (menu instanceof PlayerSelectMenu || menu instanceof PlayerInventorySelectMenu) {
            addGridButtons(playerSlots(),
                playerLabels(), 3, 58);
            addActionButton(22, "Retour", leftPos + 130, topPos + 165, 120, 24);
        } else if (menu instanceof PlayerSizeMenu) {
            addGridButtons(new int[]{11, 13, 15}, new String[]{"Réduire", "Taille normale", "Agrandir"}, 3, 70);
            addActionButton(22, "Retour aux joueurs", leftPos + 115, topPos + 145, 150, 24);
        } else if (menu instanceof ConfirmMenu) {
            addActionButton(11, "Confirmer", leftPos + 20, topPos + 100, 130, 26);
            addActionButton(15, "Annuler / retour", leftPos + 150, topPos + 100, 130, 26);
        } else if (menu instanceof VirusMenu) {
            addGridButtons(new int[]{10, 11, 12, 13},
                new String[]{"30 sec", "60 sec", "2 min", "5 min"}, 4, 58);
            addActionButton(22, "Activer", leftPos + 32, topPos + 165, 96, 25);
            addActionButton(23, "Désactiver", leftPos + 136, topPos + 165, 96, 25);
            addActionButton(25, "Retour", leftPos + 130, topPos + 205, 120, 24);
        } else if (menu instanceof JumpDeathMenu) {
            addActionButton(22, "Activer", leftPos + 30, topPos + 110, 115, 26);
            addActionButton(23, "Désactiver", leftPos + 155, topPos + 110, 115, 26);
            addActionButton(25, "Retour", leftPos + 130, topPos + 165, 120, 24);
        } else if (menu instanceof ToggleSettingsMenu) {
            addActionButton(22, "Activer", leftPos + 30, topPos + 110, 115, 26);
            addActionButton(23, "Désactiver", leftPos + 155, topPos + 110, 115, 26);
            addActionButton(25, "Retour", leftPos + 130, topPos + 165, 120, 24);
        }
    }

    private String[] playerLabels() {
        List<String> names = Minecraft.getInstance().level == null ? List.of()
            : Minecraft.getInstance().level.players().stream()
                .map(player -> player.getName().getString()).toList();
        return names.stream().limit(7).toArray(String[]::new);
    }

    private int[] playerSlots() {
        int count = Minecraft.getInstance().level == null ? 0
            : Math.min(7, Minecraft.getInstance().level.players().size());
        int[] slots = new int[count];
        for (int index = 0; index < count; index++) {
            slots[index] = 10 + index;
        }
        return slots;
    }

    private void addGridButtons(int[] slots, String[] labels, int columns, int top) {
        int gap = 6;
        int availableWidth = imageWidth - 40;
        int buttonWidth = Math.max(48, (availableWidth - (columns - 1) * gap) / columns);
        int totalWidth = columns * buttonWidth + (columns - 1) * gap;
        int startX = leftPos + (WIDTH - totalWidth) / 2;
        for (int index = 0; index < slots.length; index++) {
            int column = index % columns;
            int row = index / columns;
            addActionButton(slots[index], labels[index], startX + column * (buttonWidth + gap),
                topPos + top + row * 31, buttonWidth, 25);
        }
    }

    private void addActionButton(int slot, String text, int x, int y, int width, int height) {
        int minX = leftPos + 10;
        int maxX = leftPos + imageWidth - width - 10;
        int minY = topPos + 48;
        int maxY = topPos + imageHeight - height - 10;
        x = Math.max(minX, Math.min(x, maxX));
        y = Math.max(minY, Math.min(y, maxY));
        addRenderableWidget(new EventButton(x, y, width, height, Component.literal(text), button -> {
            if (isChoiceSlot(slot)) {
                selectSlot(slot);
            }
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft != null && minecraft.gameMode != null && minecraft.player != null) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, slot);
            }
        }, () -> selectedSlots.contains(slot)));
    }

    private void selectSlot(int slot) {
        if (menu instanceof LavaMenu && slot >= 10 && slot <= 15) {
            selectedSlots.removeIf(value -> value >= 10 && value <= 15);
        } else if (menu instanceof LavaMenu && slot >= 19 && slot <= 21) {
            selectedSlots.removeIf(value -> value >= 19 && value <= 21);
        } else if (menu instanceof DeathSwapMenu && slot >= 10 && slot <= 13) {
            selectedSlots.removeIf(value -> value >= 10 && value <= 13);
        } else if (menu instanceof RandomEffectsMenu && slot >= 10 && slot <= 16) {
            selectedSlots.removeIf(value -> value >= 10 && value <= 16);
        } else if (menu instanceof RandomEffectsMenu && slot >= 19 && slot <= 21) {
            selectedSlots.removeIf(value -> value >= 19 && value <= 21);
        } else if (menu instanceof GameSpeedMenu && slot >= 9 && slot <= 16) {
            selectedSlots.removeIf(value -> value >= 9 && value <= 16);
        }
        selectedSlots.add(slot);
    }

    private boolean isChoiceSlot(int slot) {
        return menu instanceof LavaMenu && (slot >= 10 && slot <= 15 || slot >= 19 && slot <= 21)
            || menu instanceof DeathSwapMenu && slot >= 10 && slot <= 13
            || menu instanceof RandomEffectsMenu && (slot >= 10 && slot <= 16 || slot >= 19 && slot <= 21)
            || menu instanceof GameSpeedMenu && slot >= 9 && slot <= 16
            || menu instanceof PlayerSelectMenu && slot >= 10 && slot <= 16
            || menu instanceof PlayerInventorySelectMenu && slot >= 10 && slot <= 16
            || menu instanceof PlayerSizeMenu && (slot == 11 || slot == 13 || slot == 15);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        int w = imageWidth;
        int h = imageHeight;
        graphics.fill(0, 0, width, height, 0xB8070B12);
        graphics.fill(0, 0, width, height / 2, 0x1800D9FF);
        graphics.fill(x, y, x + w, y + h, 0xFF070B12);
        graphics.fill(x + 3, y + 3, x + w - 3, y + h - 3, 0xFF111A26);
        graphics.renderOutline(x, y, w, h, 0xFF3B526B);
        graphics.fill(x + 14, y + 14, x + w - 14, y + 43, 0xFF172C3A);
        graphics.fill(x + 14, y + 43, x + w - 14, y + 45, 0xFF4DE1C1);
        graphics.drawCenteredString(font, title, x + w / 2, y + 24, 0xFF8DF8E0);
        graphics.fill(x + 20, y + 54, x + w - 20, y + h - 24, 0xFF0B141D);
        graphics.renderOutline(x + 20, y + 54, w - 40, h - 78, 0xFF263B4B);
        if (inventoryView) {
            graphics.drawString(font, "INVENTAIRE DE LA CIBLE", x + 20, y + 45, 0xFF7AE38B, false);
            graphics.drawString(font, "TON INVENTAIRE", x + 20, y + 136, 0xFF9DE3FF, false);
            for (int row = 0; row < 4; row++) {
                for (int column = 0; column < 9; column++) {
                    int slotX = x + 21 + column * 16;
                    int slotY = y + (row < 3 ? 53 + row * 16 : 105);
                    graphics.renderOutline(slotX, slotY, 16, 16, 0xFF40536A);
                    int viewerSlotY = y + (row < 3 ? 144 + row * 16 : 196);
                    graphics.renderOutline(slotX, viewerSlotY, 16, 16, 0xFF40536A);
                }
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    @Override
    protected void renderSlot(GuiGraphics graphics, Slot slot) {
        if (!inventoryView) return;
        int index = menu.slots.indexOf(slot);
        if (index < 9 || (index >= 50 && index < 54)) return;
        super.renderSlot(graphics, slot);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    private static class EventButton extends Button {
        private final BooleanSupplier selected;

        EventButton(int x, int y, int width, int height, Component message, OnPress onPress,
                    BooleanSupplier selected) {
            super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
            this.selected = selected;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            int background = isHovered() ? 0xFF245365 : 0xFF162936;
            graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), background);
            graphics.renderOutline(getX(), getY(), getWidth(), getHeight(),
                isHovered() ? 0xFF8DF8E0 : 0xFF44677A);
            graphics.drawCenteredString(Minecraft.getInstance().font, getMessage(),
                getX() + getWidth() / 2, getY() + (getHeight() - 8) / 2,
                isHovered() ? 0xFFFFFFFF : 0xFFB5C9D8);
            if (selected.getAsBoolean()) {
                graphics.renderOutline(getX() + 1, getY() + 1, getWidth() - 2, getHeight() - 2, 0xFF4DE1C1);
            }
        }
    }
}
