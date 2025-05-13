package com.shackleshot.quarrydigger;

import com.shackleshot.quarrydigger.energy.EnergyQuarryDiggerScreen;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(
        modid = QuarryDiggerMod.MOD_ID,
        bus   = EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public class ClientSetup {
    @SubscribeEvent
    public static void onRegisterScreens(RegisterMenuScreensEvent event) {
        event.register(
                MenuTypeInit.QUARRY_DIGGER_MENU.get(),
                QuarryDiggerScreen::new
        );
        event.register(
                MenuTypeInit.ENERGY_QUARRY_DIGGER_MENU.get(),
                EnergyQuarryDiggerScreen::new
        );
    }
}
