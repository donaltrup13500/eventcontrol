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

import java.util.List;

public class PlayerInventorySelectMenu extends ChestMenu {
    private final Container menuContainer;
    private final List<ServerPlayer> players;

    public PlayerInventorySelectMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new SimpleContainer(27), inventory.player.getServer() == null
            ? List.of() : inventory.player.getServer().getPlayerList().getPlayers());
    }

    private PlayerInventorySelectMenu(int containerId, Inventory inventory, Container container,
                                     List<ServerPlayer> players) {
        super(MenuType.GENERIC_9x3, containerId, inventory, container, 3);
        menuContainer = container;
        this.players = players;
        refreshItems();
    }

    private void refreshItems() {
        for (int slot = 0; slot < menuContainer.getContainerSize(); slot++) {
            menuContainer.setItem(slot, EventControl.namedItem(
                slot / 9 == 1 ? Items.GRAY_STAINED_GLASS_PANE : Items.BLACK_STAINED_GLASS_PANE, " "));
        }
        menuContainer.setItem(4, EventControl.namedItem(Items.CHEST, "Choisir un inventaire"));
        for (int index = 0; index < Math.min(players.size(), 7); index++) {
            menuContainer.setItem(10 + index, EventControl.namedItem(
                Items.PLAYER_HEAD, "Voir : " + players.get(index).getName().getString()));
        }
        menuContainer.setItem(22, EventControl.namedItem(Items.ARROW, "Retour au menu"));
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId < 0 || slotId >= menuContainer.getContainerSize()) {
            super.clicked(slotId, button, clickType, player);
            return;
        }
        if (clickType != ClickType.PICKUP || button != 0 || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (slotId >= 10 && slotId < 17 && slotId - 10 < players.size()) {
            EventControl.openMenuLater(serverPlayer,
                new PlayerInventoryMenu.Provider(players.get(slotId - 10)));
        } else if (slotId == 22) {
            EventControl.openMenuLater(serverPlayer, new EventMenu.Provider());
        }
    }

    public static class Provider implements MenuProvider {
        @Override
        public Component getDisplayName() {
            return Component.literal("Choisir un joueur");
        }

        @Override
        public net.minecraft.world.inventory.AbstractContainerMenu createMenu(
            int containerId, Inventory inventory, Player player) {
            return new PlayerInventorySelectMenu(containerId, inventory);
        }
    }
}
