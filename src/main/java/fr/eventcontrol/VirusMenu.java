package fr.eventcontrol;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class VirusMenu extends EventActionMenu {
    public VirusMenu(int containerId, Inventory inventory) {
        super(EventControl.VIRUS_MENU_TYPE.get(), containerId);
    }

    @Override
    public void clicked(int id, int dragType, net.minecraft.world.inventory.ClickType clickType, Player player) {
        if (!(player instanceof ServerPlayer serverPlayer) || serverPlayer.getServer() == null) {
            return;
        }
        switch (id) {
            case 10, 11, 12, 13 -> VirusEvent.setGraceDurationSeconds(new int[]{30, 60, 120, 300}[id - 10]);
            case 16 -> VirusEvent.setTimerVisible(!VirusEvent.isTimerVisible());
            case 22 -> VirusEvent.start(serverPlayer.getServer());
            case 23 -> VirusEvent.stop(serverPlayer.getServer(), true);
            case 25 -> EventControl.openMenuLater(serverPlayer, new EventMenu.Provider());
            default -> { }
        }
    }

    public static class Provider implements MenuProvider {
        @Override
        public Component getDisplayName() {
            return Component.literal("Virus");
        }

        @Override
        public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
            return new VirusMenu(containerId, inventory);
        }
    }
}
