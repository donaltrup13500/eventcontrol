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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class DeathSwapMenu extends ChestMenu {
    private static final int[] TIME_SLOTS = {10, 11, 12, 13};
    private static final int[] TIMES = {1, 5, 10, 15};
    private static final int TIMER_SLOT = 16;
    private static final int START_SLOT = 22;
    private static final int BACK_SLOT = 25;

    private final Container deathSwapContainer;
    private int selectedMinutes = 5;
    private boolean showTimer = true;

    public DeathSwapMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new SimpleContainer(27));
    }

    private DeathSwapMenu(int containerId, Inventory inventory, Container container) {
        super(MenuType.GENERIC_9x3, containerId, inventory, container, 3);
        deathSwapContainer = container;
        refreshItems();
    }

    private void refreshItems() {
        for (int slot = 0; slot < deathSwapContainer.getContainerSize(); slot++) {
            deathSwapContainer.setItem(slot, EventControl.namedItem(
                slot / 9 == 1 ? Items.GRAY_STAINED_GLASS_PANE : Items.BLACK_STAINED_GLASS_PANE, " "));
        }
        deathSwapContainer.setItem(4, EventControl.namedItem(Items.ENDER_PEARL, "Death Swap"));
        for (int index = 0; index < TIME_SLOTS.length; index++) {
            int minutes = TIMES[index];
            deathSwapContainer.setItem(TIME_SLOTS[index], namedItem(
                minutes == selectedMinutes ? Items.LIME_DYE : Items.CLOCK,
                "Échange toutes les " + minutes + " min"));
        }
        deathSwapContainer.setItem(TIMER_SLOT, namedItem(
            showTimer ? Items.LIME_DYE : Items.GRAY_DYE,
            "Temps en haut : " + (showTimer ? "VISIBLE" : "CACHÉ")));
        deathSwapContainer.setItem(START_SLOT, EventControl.namedItem(
            EventControl.isDeathSwap() ? Items.REDSTONE_BLOCK : Items.EMERALD,
            EventControl.isDeathSwap() ? "Arrêter" : "Démarrer"));
        deathSwapContainer.setItem(BACK_SLOT, EventControl.namedItem(Items.ARROW, "Retour au menu"));
    }

    private static ItemStack namedItem(Item item, String name) {
        return EventControl.namedItem(item, name);
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId < 0 || slotId >= deathSwapContainer.getContainerSize()) {
            super.clicked(slotId, button, clickType, player);
            return;
        }
        if (clickType != ClickType.PICKUP || button != 0 || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        for (int index = 0; index < TIME_SLOTS.length; index++) {
            if (slotId == TIME_SLOTS[index]) {
                selectedMinutes = TIMES[index];
                refreshItems();
                broadcastChanges();
                return;
            }
        }
        if (slotId == TIMER_SLOT) {
            showTimer = !showTimer;
            if (EventControl.isDeathSwap()) {
                EventControl.setDeathSwapTimerVisible(showTimer);
            }
            refreshItems();
            broadcastChanges();
        } else if (slotId == START_SLOT && serverPlayer.getServer() != null) {
            if (EventControl.isDeathSwap()) {
                EventControl.stopDeathSwap(serverPlayer.getServer(), "Death Swap arrêté.");
            } else {
                EventControl.openMenuLater(serverPlayer, new ConfirmMenu.Provider(
                    "Confirmer Death Swap", "Démarrer", () -> EventControl.startDeathSwap(
                        serverPlayer.getServer(), selectedMinutes, showTimer), new Provider()));
                return;
            }
            serverPlayer.closeContainer();
        } else if (slotId == BACK_SLOT) {
            EventControl.openMenuLater(serverPlayer, new EventMenu.Provider());
        }
    }

    public static class Provider implements MenuProvider {
        @Override
        public Component getDisplayName() {
            return Component.literal("Réglages Death Swap");
        }

        @Override
        public net.minecraft.world.inventory.AbstractContainerMenu createMenu(
            int containerId, Inventory inventory, Player player) {
            return new DeathSwapMenu(containerId, inventory);
        }
    }
}