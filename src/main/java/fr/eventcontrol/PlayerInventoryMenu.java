package fr.eventcontrol;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class PlayerInventoryMenu extends AbstractContainerMenu {
    private static final int BACK_SLOT = 53;
    private final PlayerInventoryViewContainer viewContainer;

    public PlayerInventoryMenu(int containerId, Inventory viewerInventory) {
        this(containerId, viewerInventory, viewerInventory.player,
            new PlayerInventoryViewContainer(null, true));
    }

    public PlayerInventoryMenu(int containerId, Inventory viewerInventory, ServerPlayer target) {
        this(containerId, viewerInventory, target, new PlayerInventoryViewContainer(target.getInventory(), false));
    }

    private PlayerInventoryMenu(int containerId, Inventory viewerInventory, Player target,
                                PlayerInventoryViewContainer container) {
        super(EventControl.PLAYER_INVENTORY_MENU_TYPE.get(), containerId);
        viewContainer = container;
        addTargetSlots(viewContainer);
        addViewerSlots(viewerInventory);
        viewContainer.decorate();
        String targetName = target == null ? "Joueur ciblé" : target.getName().getString();
        viewContainer.setItem(4, EventControl.namedItem(Items.NETHER_STAR,
            "Inventaire de " + targetName));
        viewContainer.setItem(3, EventControl.namedItem(Items.BLUE_CONCRETE, "Inventaire ciblé"));
        viewContainer.setItem(5, EventControl.namedItem(Items.BLUE_CONCRETE, "Barre rapide"));
        viewContainer.setItem(50, EventControl.namedItem(Items.IRON_CHESTPLATE, "Armure à droite"));
        viewContainer.setItem(51, EventControl.namedItem(Items.SHIELD, "Main secondaire à droite"));
        viewContainer.setItem(BACK_SLOT, EventControl.namedItem(Items.ARROW, "Retour aux joueurs"));
    }

    private void addTargetSlots(PlayerInventoryViewContainer container) {
        for (int index = 0; index < 54; index++) {
            int[] position = targetSlotPosition(index);
            addSlot(new Slot(container, index, position[0], position[1]));
        }
    }

    private void addViewerSlots(Inventory inventory) {
        for (int index = 0; index < 27; index++) {
            addSlot(new Slot(inventory, 9 + index, 22 + (index % 9) * 16,
                145 + (index / 9) * 16));
        }
        for (int index = 0; index < 9; index++) {
            addSlot(new Slot(inventory, index, 22 + index * 16, 197));
        }
    }

    private static int[] targetSlotPosition(int index) {
        if (index >= 9 && index < 36) {
            return new int[]{22 + (index - 9) % 9 * 16, 54 + (index - 9) / 9 * 16};
        }
        if (index >= 36 && index < 45) {
            return new int[]{22 + (index - 36) * 16, 106};
        }
        if (index >= 45 && index < 49) {
            return new int[]{190, 54 + (index - 45) * 16};
        }
        if (index == 49) return new int[]{222, 54};
        return new int[]{-100, -100};
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return viewContainer.target() == null || viewContainer.target().isAlive();
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId == BACK_SLOT && player instanceof ServerPlayer serverPlayer) {
            EventControl.openMenuLater(serverPlayer, new PlayerInventorySelectMenu.Provider());
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        clicked(id, 0, ClickType.PICKUP, player);
        return true;
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
        private final boolean clientMirror;
        private final ItemStack[] mirroredItems = new ItemStack[41];

        PlayerInventoryViewContainer(Inventory target, boolean clientMirror) {
            super(54);
            this.target = target;
            this.clientMirror = clientMirror;
            for (int index = 0; index < mirroredItems.length; index++) {
                mirroredItems[index] = ItemStack.EMPTY;
            }
        }

        Player target() {
            return target == null ? null : target.player;
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
                return 84 - slot;
            }
            return 40;
        }

        @Override
        public ItemStack getItem(int slot) {
            if (!isTargetSlot(slot)) return super.getItem(slot);
            int targetIndex = targetSlot(slot);
            return clientMirror ? mirroredItems[targetIndex] : target.getItem(targetIndex);
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            if (isTargetSlot(slot)) {
                int targetIndex = targetSlot(slot);
                if (clientMirror) {
                    mirroredItems[targetIndex] = stack;
                } else {
                    target.setItem(targetIndex, stack);
                    target.setChanged();
                }
            } else {
                super.setItem(slot, stack);
            }
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            if (!isTargetSlot(slot)) return super.removeItem(slot, amount);
            int targetIndex = targetSlot(slot);
            if (!clientMirror) return target.removeItem(targetIndex, amount);
            ItemStack stack = mirroredItems[targetIndex];
            ItemStack removed = stack.split(amount);
            if (stack.isEmpty()) mirroredItems[targetIndex] = ItemStack.EMPTY;
            return removed;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            if (!isTargetSlot(slot)) return super.removeItemNoUpdate(slot);
            int targetIndex = targetSlot(slot);
            if (!clientMirror) return target.removeItemNoUpdate(targetIndex);
            ItemStack removed = mirroredItems[targetIndex];
            mirroredItems[targetIndex] = ItemStack.EMPTY;
            return removed;
        }
    }
}
