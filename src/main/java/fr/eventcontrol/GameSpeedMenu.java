package fr.eventcontrol;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

public class GameSpeedMenu extends EventActionMenu {
    private static final int[] SPEED_SLOTS = {9, 10, 11, 12, 13, 14, 15, 16};
    private static final int[] SPEEDS = {1, 2, 4, 6, 8, 10, 20, 50};
    private final Container speedContainer;

    public GameSpeedMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new SimpleContainer(27));
    }

    private GameSpeedMenu(int containerId, Inventory inventory, Container container) {
        super(EventControl.GAME_SPEED_MENU_TYPE.get(), containerId);
        speedContainer = container;
        refreshItems();
    }

    private void refreshItems() {
        for (int slot = 0; slot < speedContainer.getContainerSize(); slot++) {
            speedContainer.setItem(slot, EventControl.namedItem(
                slot / 9 == 1 ? Items.YELLOW_STAINED_GLASS_PANE : Items.BLACK_STAINED_GLASS_PANE, " "));
        }
        speedContainer.setItem(4, EventControl.namedItem(Items.SUGAR, "Vitesse du monde • réglage"));
        for (int index = 0; index < SPEED_SLOTS.length; index++) {
            int speed = SPEEDS[index];
            speedContainer.setItem(SPEED_SLOTS[index], EventControl.namedItem(
                speed == EventControl.getGameSpeedMultiplier() ? Items.LIME_DYE : Items.SUGAR,
                "Vitesse x" + speed));
        }
        speedContainer.setItem(22, EventControl.namedItem(Items.ARROW, "Retour au menu"));
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (clickType != ClickType.PICKUP || button != 0 || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        for (int index = 0; index < SPEED_SLOTS.length; index++) {
            if (slotId == SPEED_SLOTS[index]) {
                int selectedSpeed = SPEEDS[index];
                if (selectedSpeed == 50) {
                    EventControl.openMenuLater(serverPlayer, new ConfirmMenu.Provider(
                        "Vitesse extrême", "Activer x50", () -> EventControl.setGameSpeedMultiplier(50),
                        new Provider()));
                    return;
                }
                EventControl.setGameSpeedMultiplier(selectedSpeed);
                refreshItems();
                broadcastChanges();
                return;
            }
        }
        if (slotId == 22) {
            EventControl.openMenuLater(serverPlayer, new EventMenu.Provider());
        }
    }

    public static class Provider implements MenuProvider {
        @Override
        public Component getDisplayName() {
            return Component.literal("Réglage de la vitesse");
        }

        @Override
        public net.minecraft.world.inventory.AbstractContainerMenu createMenu(
            int containerId, Inventory inventory, Player player) {
            return new GameSpeedMenu(containerId, inventory);
        }
    }
}
