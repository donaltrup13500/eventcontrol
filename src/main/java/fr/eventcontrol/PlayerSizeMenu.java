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

public class PlayerSizeMenu extends ChestMenu {
    private final Container sizeContainer;
    private final ServerPlayer target;

    public PlayerSizeMenu(int containerId, Inventory inventory, ServerPlayer target) {
        this(containerId, inventory, new SimpleContainer(27), target);
    }

    private PlayerSizeMenu(int containerId, Inventory inventory, Container container, ServerPlayer target) {
        super(MenuType.GENERIC_9x3, containerId, inventory, container, 3);
        sizeContainer = container;
        this.target = target;
        refreshItems();
    }

    private void refreshItems() {
        for (int slot = 0; slot < sizeContainer.getContainerSize(); slot++) {
            sizeContainer.setItem(slot, EventControl.namedItem(
                slot / 9 == 1 ? Items.GRAY_STAINED_GLASS_PANE : Items.BLACK_STAINED_GLASS_PANE, " "));
        }
        sizeContainer.setItem(4, EventControl.namedItem(Items.PLAYER_HEAD,
            target.getName().getString() + " - taille " + String.format("%.2f", EventControl.getPlayerSize(target))));
        sizeContainer.setItem(11, EventControl.namedItem(Items.RED_DYE, "Réduire"));
        sizeContainer.setItem(13, EventControl.namedItem(Items.WHITE_DYE, "Taille normale"));
        sizeContainer.setItem(15, EventControl.namedItem(Items.GREEN_DYE, "Agrandir"));
        sizeContainer.setItem(22, EventControl.namedItem(Items.ARROW, "Retour aux joueurs"));
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId < 0 || slotId >= sizeContainer.getContainerSize()) {
            super.clicked(slotId, button, clickType, player);
            return;
        }
        if (clickType != ClickType.PICKUP || button != 0 || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (slotId == 11) {
            EventControl.changePlayerSize(target, -0.25D);
        } else if (slotId == 13) {
            EventControl.resetPlayerSize(target);
        } else if (slotId == 15) {
            EventControl.changePlayerSize(target, 0.25D);
        } else if (slotId == 22) {
            EventControl.openMenuLater(serverPlayer, new PlayerSelectMenu.Provider());
            return;
        } else {
            return;
        }
        refreshItems();
        broadcastChanges();
    }

    public static class Provider implements MenuProvider {
        private final ServerPlayer target;

        public Provider(ServerPlayer target) {
            this.target = target;
        }

        @Override
        public Component getDisplayName() {
            return Component.literal("Taille de " + target.getName().getString());
        }

        @Override
        public net.minecraft.world.inventory.AbstractContainerMenu createMenu(
            int containerId, Inventory inventory, Player player) {
            return new PlayerSizeMenu(containerId, inventory, target);
        }
    }
}