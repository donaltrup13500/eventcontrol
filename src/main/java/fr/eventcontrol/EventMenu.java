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

public class EventMenu extends ChestMenu {
    private static final int VIEW_INVENTORY_BUTTON = 10;
    private static final int INVENTORY_BUTTON = 11;
    private static final int BORDER_BUTTON = 12;
    private static final int LAVA_BUTTON = 13;
    private static final int SIZE_BUTTON = 14;
    private static final int HEALTH_BUTTON = 15;
    private static final int DEATH_SWAP_BUTTON = 16;
    private static final int RANDOM_EFFECTS_BUTTON = 17;
    private static final int GAME_SPEED_BUTTON = 20;
    private static final int STOP_ALL_BUTTON = 21;
    private static final int CLOSE_BUTTON = 22;

    private final Container eventContainer;

    public EventMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new SimpleContainer(27));
    }

    private EventMenu(int containerId, Inventory inventory, Container container) {
        super(MenuType.GENERIC_9x3, containerId, inventory, container, 3);
        eventContainer = container;
        refreshItems();
    }

    private void refreshItems() {
        for (int slot = 0; slot < eventContainer.getContainerSize(); slot++) {
            eventContainer.setItem(slot, new ItemStack(slot / 9 == 1
                ? Items.GRAY_STAINED_GLASS_PANE : Items.BLACK_STAINED_GLASS_PANE));
        }
        eventContainer.setItem(4, EventControl.namedItem(Items.NETHER_STAR, "Event Master v1.2.7"));
        eventContainer.setItem(VIEW_INVENTORY_BUTTON,
            EventControl.namedItem(Items.CHEST, "Voir l'inventaire d'un joueur"));
        eventContainer.setItem(INVENTORY_BUTTON,
            EventControl.statusItem(EventControl.isSharedInventory(), "Inventaire partagé"));
        eventContainer.setItem(BORDER_BUTTON,
            EventControl.statusItem(EventControl.isGrowingBorder(), "Bordure évolutive"));
        eventContainer.setItem(LAVA_BUTTON,
            EventControl.statusItem(EventControl.isRisingLava(), "Montée de lave"));
        eventContainer.setItem(SIZE_BUTTON,
            EventControl.namedItem(Items.PLAYER_HEAD, "Taille des joueurs"));
        eventContainer.setItem(HEALTH_BUTTON, EventControl.statusItem(
            EventControl.isSharedHealth(), "♥ Vie partagée"));
        eventContainer.setItem(DEATH_SWAP_BUTTON,
            EventControl.statusItem(EventControl.isDeathSwap(), "Death Swap"));
        eventContainer.setItem(RANDOM_EFFECTS_BUTTON,
            EventControl.statusItem(EventControl.isRandomEffects(), "Effets aléatoires"));
        eventContainer.setItem(GAME_SPEED_BUTTON, EventControl.namedItem(
            Items.SUGAR, "Vitesse du jeu : x" + EventControl.getGameSpeedMultiplier()));
        eventContainer.setItem(STOP_ALL_BUTTON, EventControl.namedItem(Items.BARRIER, "Arrêter tous les événements"));
        eventContainer.setItem(CLOSE_BUTTON, EventControl.namedItem(Items.BARRIER, "Fermer"));
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < eventContainer.getContainerSize()) {
            if (clickType != ClickType.PICKUP || button != 0 || !(player instanceof ServerPlayer)) {
                return;
            }
            switch (slotId) {
                case VIEW_INVENTORY_BUTTON -> {
                    if (player instanceof ServerPlayer serverPlayer) {
                        EventControl.openMenuLater(serverPlayer, new PlayerInventorySelectMenu.Provider());
                    }
                    return;
                }
                case INVENTORY_BUTTON -> EventControl.setSharedInventory(!EventControl.isSharedInventory());
                case BORDER_BUTTON -> {
                    if (player instanceof ServerPlayer serverPlayer && serverPlayer.getServer() != null) {
                        if (EventControl.isGrowingBorder()) {
                            EventControl.stopGrowingBorder(serverPlayer.getServer());
                        } else {
                            EventControl.startGrowingBorder(serverPlayer.getServer(), serverPlayer);
                        }
                    }
                }
                case LAVA_BUTTON -> {
                    if (player instanceof ServerPlayer serverPlayer) {
                        EventControl.openMenuLater(serverPlayer, new LavaMenu.Provider());
                    }
                    return;
                }
                case SIZE_BUTTON -> {
                    if (player instanceof ServerPlayer serverPlayer) {
                        EventControl.openMenuLater(serverPlayer, new PlayerSelectMenu.Provider());
                    }
                    return;
                }
                case HEALTH_BUTTON -> EventControl.setSharedHealth(!EventControl.isSharedHealth());
                case DEATH_SWAP_BUTTON -> {
                    if (player instanceof ServerPlayer serverPlayer) {
                        EventControl.openMenuLater(serverPlayer, new DeathSwapMenu.Provider());
                    }
                    return;
                }
                case RANDOM_EFFECTS_BUTTON -> {
                    if (player instanceof ServerPlayer serverPlayer) {
                        EventControl.openMenuLater(serverPlayer, new RandomEffectsMenu.Provider());
                    }
                    return;
                }
                case GAME_SPEED_BUTTON -> {
                    if (player instanceof ServerPlayer serverPlayer) {
                        EventControl.openMenuLater(serverPlayer, new GameSpeedMenu.Provider());
                    }
                    return;
                }
                case STOP_ALL_BUTTON -> {
                    if (player instanceof ServerPlayer serverPlayer && serverPlayer.getServer() != null) {
                        EventControl.stopAllEvents(serverPlayer.getServer());
                    }
                }
                case CLOSE_BUTTON -> player.closeContainer();
                default -> {
                    return;
                }
            }
            refreshItems();
            broadcastChanges();
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    public static class Provider implements MenuProvider {
        @Override
        public Component getDisplayName() {
            return Component.literal("Event Master");
        }

        @Override
        public net.minecraft.world.inventory.AbstractContainerMenu createMenu(
            int containerId, Inventory inventory, Player player) {
            return new EventMenu(containerId, inventory);
        }
    }
}
