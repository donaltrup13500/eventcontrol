package fr.eventcontrol;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;

public class JumpDeathMenu extends EventActionMenu {
    public JumpDeathMenu(int containerId, Inventory inventory) {
        super(EventControl.JUMP_DEATH_MENU_TYPE.get(), containerId);
    }

    @Override
    public void clicked(int id, int dragType, ClickType clickType, Player player) {
        if (clickType != ClickType.PICKUP || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (id == 22 && serverPlayer.getServer() != null) {
                JumpDeathEvent.start(serverPlayer.getServer().getPlayerList().getPlayers());
            serverPlayer.closeContainer();
        } else if (id == 23) {
            JumpDeathEvent.stop();
            serverPlayer.closeContainer();
        } else if (id == 25) {
            EventControl.openMenuLater(serverPlayer, new EventMenu.Provider());
        }
    }

    public static class Provider implements MenuProvider {
        @Override
        public Component getDisplayName() {
            return Component.literal("Saut mortel");
        }

        @Override
        public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
            return new JumpDeathMenu(containerId, inventory);
        }
    }
}