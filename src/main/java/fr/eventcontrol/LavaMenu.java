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

public class LavaMenu extends EventActionMenu {
    private static final int[] PREPARATION_SLOTS = {10, 11, 12, 13, 14, 15};
    private static final int[] PREPARATION_MINUTES = {5, 10, 15, 20, 25, 30};
    private static final int[] SPEED_SLOTS = {19, 20, 21};
    private static final int[] SPEEDS = {1, 2, 3};
    private static final int START_SLOT = 23;
    private static final int BACK_SLOT = 25;
    private static final int STOP_SLOT = 24;

    private final Container lavaContainer;
    private int selectedMinutes = EventControl.getLavaPreparationMinutes();
    private int selectedSpeed = EventControl.getLavaSpeedMultiplier();

    public LavaMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new SimpleContainer(27));
    }

    private LavaMenu(int containerId, Inventory inventory, Container container) {
        super(EventControl.LAVA_MENU_TYPE.get(), containerId);
        lavaContainer = container;
        refreshItems();
    }

    private void refreshItems() {
        for (int slot = 0; slot < lavaContainer.getContainerSize(); slot++) {
            lavaContainer.setItem(slot, new ItemStack(slot == 1 || slot == 4 || slot == 7
                ? Items.ORANGE_STAINED_GLASS_PANE : Items.BLACK_STAINED_GLASS_PANE));
        }
        lavaContainer.setItem(4, EventControl.namedItem(Items.LAVA_BUCKET,
            EventControl.isRisingLava() ? "Montée de lave • active" : "Réglage de la montée de lave"));
        lavaContainer.setItem(10, EventControl.namedItem(Items.CLOCK, "Préparation"));
        lavaContainer.setItem(16, EventControl.namedItem(Items.REDSTONE, "Vitesse"));
        for (int index = 0; index < PREPARATION_SLOTS.length; index++) {
            int minutes = PREPARATION_MINUTES[index];
            lavaContainer.setItem(PREPARATION_SLOTS[index], EventControl.namedItem(
                minutes == selectedMinutes ? Items.LIME_DYE : Items.CLOCK,
                "Départ dans " + minutes + " min"));
        }
        for (int index = 0; index < SPEED_SLOTS.length; index++) {
            int speed = SPEEDS[index];
            lavaContainer.setItem(SPEED_SLOTS[index], EventControl.namedItem(
                speed == selectedSpeed ? Items.LIME_DYE : Items.REDSTONE,
                "Vitesse x" + speed));
        }
        lavaContainer.setItem(START_SLOT, EventControl.namedItem(Items.FLINT_AND_STEEL,
            EventControl.isRisingLava() ? "Appliquer la vitesse" : "Démarrer l'événement"));
        lavaContainer.setItem(STOP_SLOT, EventControl.namedItem(Items.BARRIER, "Arrêter la montée"));
        lavaContainer.setItem(BACK_SLOT, EventControl.namedItem(Items.ARROW, "Retour au menu"));
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (clickType != ClickType.PICKUP || button != 0 || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        for (int index = 0; index < PREPARATION_SLOTS.length; index++) {
            if (slotId == PREPARATION_SLOTS[index]) {
                selectedMinutes = PREPARATION_MINUTES[index];
                refreshItems();
                broadcastChanges();
                return;
            }
        }
        for (int index = 0; index < SPEED_SLOTS.length; index++) {
            if (slotId == SPEED_SLOTS[index]) {
                selectedSpeed = SPEEDS[index];
                if (EventControl.isRisingLava()) {
                    EventControl.setLavaSpeedMultiplier(selectedSpeed);
                }
                refreshItems();
                broadcastChanges();
                return;
            }
        }
        if (slotId == STOP_SLOT && EventControl.isRisingLava()) {
            EventControl.setRisingLava(false);
            serverPlayer.closeContainer();
        } else if (slotId == START_SLOT) {
            if (EventControl.isRisingLava()) {
                EventControl.setLavaSpeedMultiplier(selectedSpeed);
                serverPlayer.closeContainer();
            } else {
                EventControl.openMenuLater(serverPlayer, new ConfirmMenu.Provider(
                    "Confirmer la lave", "Démarrer", () -> EventControl.startRisingLava(selectedMinutes, selectedSpeed),
                    new Provider()));
            }
        } else if (slotId == BACK_SLOT) {
            EventControl.openMenuLater(serverPlayer, new EventMenu.Provider());
        }
    }

    public static class Provider implements MenuProvider {
        @Override
        public Component getDisplayName() {
            return Component.literal("Réglages de la lave");
        }

        @Override
        public net.minecraft.world.inventory.AbstractContainerMenu createMenu(
            int containerId, Inventory inventory, Player player) {
            return new LavaMenu(containerId, inventory);
        }
    }
}