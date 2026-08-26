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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class PlayerSelectMenu extends EventActionMenu {
    private final Container playerContainer;
    private final List<ServerPlayer> players;

    public PlayerSelectMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new SimpleContainer(27), inventory.player.getServer() == null
            ? List.of() : inventory.player.getServer().getPlayerList().getPlayers());
    }

    private PlayerSelectMenu(int containerId, Inventory inventory, Container container, List<ServerPlayer> players) {
        super(EventControl.PLAYER_SELECT_MENU_TYPE.get(), containerId);
        playerContainer = container;
        this.players = players;
        refreshItems();
    }

    private void refreshItems() {
        for (int slot = 0; slot < playerContainer.getContainerSize(); slot++) {
            playerContainer.setItem(slot, EventControl.namedItem(
                slot / 9 == 1 ? Items.GREEN_STAINED_GLASS_PANE : Items.BLACK_STAINED_GLASS_PANE, " "));
        }
        playerContainer.setItem(4, EventControl.namedItem(Items.PLAYER_HEAD, "Choisir un joueur"));
        for (int index = 0; index < Math.min(players.size(), 7); index++) {
            ServerPlayer target = players.get(index);
            playerContainer.setItem(10 + index, EventControl.namedItem(
                Items.PLAYER_HEAD, target.getName().getString() + " - taille "
                    + String.format("%.2f", EventControl.getPlayerSize(target))));
        }
        playerContainer.setItem(22, EventControl.namedItem(Items.ARROW, "Retour au menu"));
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (clickType != ClickType.PICKUP || button != 0 || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (slotId >= 10 && slotId < 17 && slotId - 10 < players.size()) {
            EventControl.openMenuLater(serverPlayer, new PlayerSizeMenu.Provider(players.get(slotId - 10)));
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
            return new PlayerSelectMenu(containerId, inventory);
        }
    }
}