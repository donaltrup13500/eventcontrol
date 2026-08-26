package fr.eventcontrol;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(modid = EventControl.MOD_ID, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(EventControl.EVENT_MENU_TYPE.get(), EventMenuScreen::new);
        event.register(EventControl.VIRUS_MENU_TYPE.get(), EventSubMenuScreen::new);
        event.register(EventControl.LAVA_MENU_TYPE.get(), EventSubMenuScreen::new);
        event.register(EventControl.DEATH_SWAP_MENU_TYPE.get(), EventSubMenuScreen::new);
        event.register(EventControl.RANDOM_EFFECTS_MENU_TYPE.get(), EventSubMenuScreen::new);
        event.register(EventControl.GAME_SPEED_MENU_TYPE.get(), EventSubMenuScreen::new);
        event.register(EventControl.PLAYER_SELECT_MENU_TYPE.get(), EventSubMenuScreen::new);
        event.register(EventControl.PLAYER_INVENTORY_SELECT_MENU_TYPE.get(), EventSubMenuScreen::new);
        event.register(EventControl.PLAYER_SIZE_MENU_TYPE.get(), EventSubMenuScreen::new);
        event.register(EventControl.PLAYER_INVENTORY_MENU_TYPE.get(), EventSubMenuScreen::new);
        event.register(EventControl.CONFIRM_MENU_TYPE.get(), EventSubMenuScreen::new);
        event.register(EventControl.JUMP_DEATH_MENU_TYPE.get(), EventSubMenuScreen::new);
        event.register(EventControl.TOGGLE_INVENTORY_MENU_TYPE.get(), EventSubMenuScreen::new);
        event.register(EventControl.TOGGLE_BORDER_MENU_TYPE.get(), EventSubMenuScreen::new);
        event.register(EventControl.TOGGLE_HEALTH_MENU_TYPE.get(), EventSubMenuScreen::new);
        event.register(EventControl.TOGGLE_SUN_MENU_TYPE.get(), EventSubMenuScreen::new);
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        TimerHud.render(event);
        VirusHud.render(event);
    }
}
