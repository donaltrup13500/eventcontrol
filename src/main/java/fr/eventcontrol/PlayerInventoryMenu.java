package fr.eventcontrol;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class PlayerInventoryMenu extends ChestMenu {
    private static final int BACK_SLOT = 53;
    private final PlayerInventoryViewContainer viewContainer;

    public PlayerInventoryMenu(int containerId, Inventory viewerInventory, ServerPlayer target) {
        this(containerId, viewerInventory, target, new PlayerInventoryViewContainer(target.getInventory()));
    }

    private PlayerInventoryMenu(int containerId, Inventory viewerInventory, ServerPlayer target,
                                PlayerInventoryViewContainer container) {
        super(MenuType.GENERIC_9x6, containerId, viewerInventory, container, 6);
        viewContainer = container;
        viewContainer.decorate();
        viewContainer.setItem(4, EventControl.namedItem(Items.NETHER_STAR,
            "Inventaire de " + target.getName().getString()));
        viewContainer.setItem(3, EventControl.namedItem(Items.BLUE_CONCRETE, "Inventaire ciblé"));
        viewContainer.setItem(5, EventControl.namedItem(Items.BLUE_CONCRETE, "Barre rapide"));
        viewContainer.setItem(50, EventControl.namedItem(Items.IRON_CHESTPLATE, "Armure à droite"));
        viewContainer.setItem(51, EventControl.namedItem(Items.SHIELD, "Main secondaire à droite"));
        viewContainer.setItem(BACK_SLOT, EventControl.namedItem(Items.ARROW, "Retour aux joueurs"));
    }

    @Override
    public boolean stillValid(Player player) {
        return player instanceof ServerPlayer && viewContainer.target().isAlive();
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId == BACK_SLOT && player instanceof ServerPlayer serverPlayer) {
            EventControl.openMenuLater(serverPlayer, new PlayerInventorySelectMenu.Provider());
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    public static class Provider implements MenuProvider {
        private final ServerPlayer target;

        public Provider(ServerPlayer target) {
            this.target = target;
        }

        @Override
        public Component getDisplayName() {
            return Component.literal("Inventaire de " + target.getName().getString());
        }

        @Override
        public net.minecraft.world.inventory.AbstractContainerMenu createMenu(
            int containerId, Inventory inventory, Player player) {
            return new PlayerInventoryMenu(containerId, inventory, target);
        }
    }

    private static class PlayerInventoryViewContainer extends SimpleContainer {
        private final Inventory target;

        PlayerInventoryViewContainer(Inventory target) {
            super(54);
            this.target = target;
        }

        ServerPlayer target() {
            return (ServerPlayer) target.player;
        }

        void decorate() {
            for (int slot = 0; slot < getContainerSize(); slot++) {
                if (!isTargetSlot(slot)) {
                    super.setItem(slot, EventControl.namedItem(
                        slot / 9 == 5 ? Items.BLUE_STAINED_GLASS_PANE : Items.GRAY_STAINED_GLASS_PANE, " "));
                }
            }
        }

        private boolean isTargetSlot(int slot) {
            return slot >= 9 && slot <= 49;
        }

        private int targetSlot(int slot) {
            if (slot >= 9 && slot <= 35) {
                return slot;
            }
            if (slot >= 36 && slot <= 44) {
                return slot - 36;
            }
            if (slot >= 45 && slot <= 48) {
                return 48 - slot;
            }
            return 40;
        }

        @Override
        public ItemStack getItem(int slot) {
            return isTargetSlot(slot) ? target.getItem(targetSlot(slot)) : super.getItem(slot);
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            if (isTargetSlot(slot)) {
                target.setItem(targetSlot(slot), stack);
                target.setChanged();
            } else {
                super.setItem(slot, stack);
            }
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            return isTargetSlot(slot) ? target.removeItem(targetSlot(slot), amount) : super.removeItem(slot, amount);
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            return isTargetSlot(slot) ? target.removeItemNoUpdate(targetSlot(slot)) : super.removeItemNoUpdate(slot);
        }
    }
}
