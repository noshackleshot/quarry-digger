// src/main/java/com/shackleshot/quarrydigger/ClientSetup.java
package com.shackleshot.quarrydigger;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraft.client.gui.screens.MenuScreens;

@Mod.EventBusSubscriber(
        modid = QuarryDiggerMod.MOD_ID,
        bus   = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public class ClientSetup {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        MenuScreens.register(
                MenuTypeInit.QUARRY_DIGGER_MENU.get(),
                QuarryDiggerScreen::new
        );
    }
}
