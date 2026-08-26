package fr.eventcontrol;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;

public class EventMenu extends AbstractContainerMenu {
    private final StatusData status = new StatusData();

    public EventMenu(int containerId, Inventory inventory) {
        super(EventControl.EVENT_MENU_TYPE.get(), containerId);
        status.refresh();
        addDataSlots(status);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.isAlive();
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        switch (id) {
            case 101 -> {
                if (player instanceof ServerPlayer serverPlayer) {
                    EventControl.openMenuLater(serverPlayer, new PlayerInventorySelectMenu.Provider());
                }
                return true;
            }
            case 102 -> {
                if (player instanceof ServerPlayer serverPlayer) {
                    EventControl.openMenuLater(serverPlayer,
                        new ToggleSettingsMenu.Provider(ToggleSettingsMenu.INVENTORY));
                }
                return true;
            }
            case 115 -> {
                return true;
            }
            case 103 -> {
                if (player instanceof ServerPlayer serverPlayer && serverPlayer.getServer() != null) {
                    EventControl.openMenuLater(serverPlayer,
                        new ToggleSettingsMenu.Provider(ToggleSettingsMenu.BORDER));
                }
                return true;
            }
            case 116 -> {
                return true;
            }
            case 104 -> {
                if (player instanceof ServerPlayer serverPlayer) {
                    EventControl.openMenuLater(serverPlayer, new LavaMenu.Provider());
                }
                return true;
            }
            case 105 -> {
                if (player instanceof ServerPlayer serverPlayer) {
                    EventControl.openMenuLater(serverPlayer, new PlayerSelectMenu.Provider());
                }
                return true;
            }
            case 106 -> {
                if (player instanceof ServerPlayer serverPlayer) {
                    EventControl.openMenuLater(serverPlayer,
                        new ToggleSettingsMenu.Provider(ToggleSettingsMenu.HEALTH));
                }
                return true;
            }
            case 117 -> {
                return true;
            }
            case 107 -> {
                if (player instanceof ServerPlayer serverPlayer) {
                    EventControl.openMenuLater(serverPlayer, new DeathSwapMenu.Provider());
                }
                return true;
            }
            case 108 -> {
                if (player instanceof ServerPlayer serverPlayer) {
                    EventControl.openMenuLater(serverPlayer, new RandomEffectsMenu.Provider());
                }
                return true;
            }
            case 109 -> {
                if (player instanceof ServerPlayer serverPlayer) {
                    EventControl.openMenuLater(serverPlayer, new GameSpeedMenu.Provider());
                }
                return true;
            }
            case 110 -> {
                if (player instanceof ServerPlayer serverPlayer && serverPlayer.getServer() != null) {
                    EventControl.stopAllEvents(serverPlayer.getServer());
                }
                status.refresh();
                broadcastChanges();
                return true;
            }
            case 111 -> {
                player.closeContainer();
                return true;
            }
            case 112 -> {
                if (player instanceof ServerPlayer serverPlayer) {
                    EventControl.openMenuLater(serverPlayer, new VirusMenu.Provider());
                }
                return true;
            }
            case 113 -> {
                if (player instanceof ServerPlayer serverPlayer) {
                    EventControl.openMenuLater(serverPlayer, new JumpDeathMenu.Provider());
                }
                return true;
            }
            case 114 -> {
                if (player instanceof ServerPlayer serverPlayer) {
                    EventControl.openMenuLater(serverPlayer,
                        new ToggleSettingsMenu.Provider(ToggleSettingsMenu.SUN));
                }
                return true;
            }
            case 118 -> {
                return true;
            }
            case 119 -> {
                return true;
            }
            case 120 -> {
                return true;
            }
            case 121 -> {
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    public boolean isLavaActive() {
        return status.get(0) != 0;
    }

    public boolean isSharedHealthActive() {
        return status.get(1) != 0;
    }

    public boolean isSharedInventoryActive() {
        return status.get(2) != 0;
    }

    public boolean isBorderActive() {
        return status.get(3) != 0;
    }

    public int displayedSpeed() {
        return status.get(4);
    }

    public boolean isVirusActive() {
        return status.get(5) != 0;
    }

    public boolean isJumpDeathActive() {
        return status.get(6) != 0;
    }

    public boolean isSunBurnActive() {
        return status.get(7) != 0;
    }

    private static class StatusData implements ContainerData {
        private final int[] values = new int[8];

        void refresh() {
            values[0] = EventControl.isRisingLava() ? 1 : 0;
            values[1] = EventControl.isSharedHealth() ? 1 : 0;
            values[2] = EventControl.isSharedInventory() ? 1 : 0;
            values[3] = EventControl.isGrowingBorder() ? 1 : 0;
            values[4] = EventControl.getGameSpeedMultiplier();
            values[5] = VirusEvent.isActive() ? 1 : 0;
            values[6] = JumpDeathEvent.isActive() ? 1 : 0;
            values[7] = SunBurnEvent.isActive() ? 1 : 0;
        }

        @Override
        public int get(int index) {
            return values[index];
        }

        @Override
        public void set(int index, int value) {
            values[index] = value;
        }

        @Override
        public int getCount() {
            return 8;
        }
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
