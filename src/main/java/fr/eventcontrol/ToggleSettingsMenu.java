package fr.eventcontrol;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;

final class ToggleSettingsMenu extends EventActionMenu {
    static final int INVENTORY = 0;
    static final int BORDER = 1;
    static final int HEALTH = 2;
    static final int SUN = 3;
    private final int setting;

    ToggleSettingsMenu(int containerId, Inventory inventory, int setting) {
        super(menuType(setting), containerId);
        this.setting = setting;
    }

    int setting() {
        return setting;
    }

    @Override
    public void clicked(int id, int button, ClickType clickType, Player player) {
        if (clickType != ClickType.PICKUP || !(player instanceof ServerPlayer serverPlayer)
            || serverPlayer.getServer() == null) {
            return;
        }
        if (id == 22) {
            setEnabled(serverPlayer, true);
            serverPlayer.closeContainer();
        } else if (id == 23) {
            setEnabled(serverPlayer, false);
            serverPlayer.closeContainer();
        } else if (id == 25) {
            EventControl.openMenuLater(serverPlayer, new EventMenu.Provider());
        }
    }

    private void setEnabled(ServerPlayer player, boolean enabled) {
        switch (setting) {
            case INVENTORY -> EventControl.setSharedInventory(enabled);
            case HEALTH -> EventControl.setSharedHealth(enabled);
            case BORDER -> {
                if (enabled) {
                    EventControl.startGrowingBorder(player.getServer(), player);
                } else {
                    EventControl.stopGrowingBorder(player.getServer());
                }
            }
            case SUN -> {
                if (enabled) {
                    SunBurnEvent.start(player.getServer());
                } else {
                    SunBurnEvent.stop(player.getServer());
                }
            }
            default -> { }
        }
    }

    private static net.minecraft.world.inventory.MenuType<?> menuType(int setting) {
        return switch (setting) {
            case INVENTORY -> EventControl.TOGGLE_INVENTORY_MENU_TYPE.get();
            case BORDER -> EventControl.TOGGLE_BORDER_MENU_TYPE.get();
            case HEALTH -> EventControl.TOGGLE_HEALTH_MENU_TYPE.get();
            case SUN -> EventControl.TOGGLE_SUN_MENU_TYPE.get();
            default -> throw new IllegalArgumentException("Unknown toggle setting: " + setting);
        };
    }

    static final class Provider implements MenuProvider {
        private final int setting;

        Provider(int setting) {
            this.setting = setting;
        }

        @Override
        public Component getDisplayName() {
            return Component.literal(title(setting));
        }

        @Override
        public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
            return new ToggleSettingsMenu(containerId, inventory, setting);
        }
    }

    static String title(int setting) {
        return switch (setting) {
            case INVENTORY -> "Inventaire partagé";
            case BORDER -> "Bordure évolutive";
            case HEALTH -> "Vie partagée";
            case SUN -> "Soleil dangereux";
            default -> "Réglage";
        };
    }

    static boolean isActive(ToggleSettingsMenu menu) {
        return switch (menu.setting) {
            case INVENTORY -> EventControl.isSharedInventory();
            case BORDER -> EventControl.isGrowingBorder();
            case HEALTH -> EventControl.isSharedHealth();
            case SUN -> SunBurnEvent.isActive();
            default -> false;
        };
    }
}
