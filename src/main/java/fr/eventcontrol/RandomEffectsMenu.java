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

public class RandomEffectsMenu extends EventActionMenu {
    private static final int[] PLAYER_SLOTS = {10, 11, 12, 13, 14, 15, 16};
    private static final int[] TIME_SLOTS = {19, 20, 21};
    private static final int[] TIMES = {10, 30, 60};
    private static final int TIMER_SLOT = 18;
    private static final int START_SLOT = 23;
    private static final int BACK_SLOT = 25;

    private final Container menuContainer;
    private final List<ServerPlayer> players;
    private int selectedPlayer = -1;
    private int selectedSeconds = 30;

    public RandomEffectsMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new SimpleContainer(27), inventory.player.getServer() == null
            ? List.of() : inventory.player.getServer().getPlayerList().getPlayers());
    }

    private RandomEffectsMenu(int containerId, Inventory inventory, Container container, List<ServerPlayer> players) {
        super(EventControl.RANDOM_EFFECTS_MENU_TYPE.get(), containerId);
        menuContainer = container;
        this.players = players;
        refreshItems();
    }

    private void refreshItems() {
        for (int slot = 0; slot < menuContainer.getContainerSize(); slot++) {
            menuContainer.setItem(slot, EventControl.namedItem(
                slot / 9 == 1 ? Items.LIGHT_BLUE_STAINED_GLASS_PANE : Items.BLACK_STAINED_GLASS_PANE, " "));
        }
        menuContainer.setItem(4, EventControl.namedItem(Items.POTION, "Effets aléatoires • ciblage"));
        for (int index = 0; index < Math.min(players.size(), PLAYER_SLOTS.length); index++) {
            ServerPlayer target = players.get(index);
            menuContainer.setItem(PLAYER_SLOTS[index], EventControl.namedItem(
                index == selectedPlayer ? Items.LIME_DYE : Items.PLAYER_HEAD,
                "Cible : " + target.getName().getString()));
        }
        for (int index = 0; index < TIME_SLOTS.length; index++) {
            int seconds = TIMES[index];
            menuContainer.setItem(TIME_SLOTS[index], EventControl.namedItem(
                seconds == selectedSeconds ? Items.LIME_DYE : Items.CLOCK,
                "Nouvel effet toutes les " + seconds + " s"));
        }
        menuContainer.setItem(TIMER_SLOT, EventControl.namedItem(
            EventControl.isRandomEffectsBarVisible() ? Items.LIME_DYE : Items.GRAY_DYE,
            "Barre du haut : " + (EventControl.isRandomEffectsBarVisible() ? "VISIBLE" : "CACHÉE")));
        menuContainer.setItem(START_SLOT, EventControl.namedItem(
            EventControl.isRandomEffects() ? Items.REDSTONE_BLOCK : Items.EMERALD,
            EventControl.isRandomEffects() ? "Arrêter" : "Démarrer"));
        menuContainer.setItem(BACK_SLOT, EventControl.namedItem(Items.ARROW, "Retour au menu"));
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (clickType != ClickType.PICKUP || button != 0 || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        for (int index = 0; index < PLAYER_SLOTS.length; index++) {
            if (slotId == PLAYER_SLOTS[index] && index < players.size()) {
                selectedPlayer = index;
                refreshItems();
                broadcastChanges();
                return;
            }
        }
        for (int index = 0; index < TIME_SLOTS.length; index++) {
            if (slotId == TIME_SLOTS[index]) {
                selectedSeconds = TIMES[index];
                refreshItems();
                broadcastChanges();
                return;
            }
        }
        if (slotId == TIMER_SLOT) {
            EventControl.setRandomEffectsBarVisible(!EventControl.isRandomEffectsBarVisible());
            refreshItems();
            broadcastChanges();
            return;
        }
        if (slotId == START_SLOT && serverPlayer.getServer() != null) {
            if (!EventControl.isRandomEffects() && selectedPlayer >= 0 && selectedPlayer < players.size()) {
                EventControl.startRandomEffects(serverPlayer.getServer(), players.get(selectedPlayer), selectedSeconds);
            } else {
                serverPlayer.displayClientMessage(Component.literal("Choisis d'abord un joueur."), true);
                return;
            }
            serverPlayer.closeContainer();
        } else if (slotId == 24 && serverPlayer.getServer() != null) {
            EventControl.stopRandomEffects(serverPlayer.getServer());
            serverPlayer.closeContainer();
        } else if (slotId == BACK_SLOT) {
            EventControl.openMenuLater(serverPlayer, new EventMenu.Provider());
        }
    }

    public static class Provider implements MenuProvider {
        @Override
        public Component getDisplayName() {
            return Component.literal("Réglages des effets");
        }

        @Override
        public net.minecraft.world.inventory.AbstractContainerMenu createMenu(
            int containerId, Inventory inventory, Player player) {
            return new RandomEffectsMenu(containerId, inventory);
        }
    }
}