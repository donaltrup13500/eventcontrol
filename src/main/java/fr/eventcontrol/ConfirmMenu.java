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

public class ConfirmMenu extends EventActionMenu {
    private final Container confirmContainer;
    private final Runnable action;
    private final MenuProvider cancelProvider;

    public ConfirmMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, "Confirmation", "Confirmer", () -> { },
            new EventMenu.Provider());
    }

    public ConfirmMenu(int containerId, Inventory inventory, String title, String message,
                       Runnable action, MenuProvider cancelProvider) {
        this(containerId, inventory, new SimpleContainer(27), title, message, action, cancelProvider);
    }

    private ConfirmMenu(int containerId, Inventory inventory, Container container, String title, String message,
                        Runnable action, MenuProvider cancelProvider) {
        super(EventControl.CONFIRM_MENU_TYPE.get(), containerId);
        confirmContainer = container;
        this.action = action;
        this.cancelProvider = cancelProvider;
        for (int slot = 0; slot < 27; slot++) {
            confirmContainer.setItem(slot, EventControl.namedItem(
                slot / 9 == 1 ? Items.ORANGE_STAINED_GLASS_PANE : Items.BLACK_STAINED_GLASS_PANE, " "));
        }
        confirmContainer.setItem(4, EventControl.namedItem(Items.NETHER_STAR, "Confirmation • " + title));
        confirmContainer.setItem(11, EventControl.namedItem(Items.LIME_DYE, "Confirmer : " + message));
        confirmContainer.setItem(15, EventControl.namedItem(Items.RED_DYE, "Annuler / retour"));
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (clickType != ClickType.PICKUP || button != 0 || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (slotId == 11) {
            action.run();
            serverPlayer.closeContainer();
        } else if (slotId == 15) {
            EventControl.openMenuLater(serverPlayer, cancelProvider);
        }
    }

    public static class Provider implements MenuProvider {
        private final String title;
        private final String message;
        private final Runnable action;
        private final MenuProvider cancelProvider;

        public Provider(String title, String message, Runnable action, MenuProvider cancelProvider) {
            this.title = title;
            this.message = message;
            this.action = action;
            this.cancelProvider = cancelProvider;
        }

        @Override
        public Component getDisplayName() {
            return Component.literal("Confirmation");
        }

        @Override
        public net.minecraft.world.inventory.AbstractContainerMenu createMenu(
            int containerId, Inventory inventory, Player player) {
            return new ConfirmMenu(containerId, inventory, title, message, action, cancelProvider);
        }
    }
}
