package com.shackleshot.quarrydigger;

import java.util.function.Supplier;

import com.shackleshot.quarrydigger.energy.EnergyQuarryDiggerMenu;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.MenuType;

import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.bus.api.IEventBus;

public class MenuTypeInit {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(BuiltInRegistries.MENU, QuarryDiggerMod.MOD_ID);

    public static final Supplier<MenuType<QuarryDiggerMenu>> QUARRY_DIGGER_MENU =
            MENUS.register("quarry_digger_menu", () ->
                    IMenuTypeExtension.create(
                            (windowId, inv, buf) -> new QuarryDiggerMenu(windowId, inv, buf.readBlockPos())
                    )
            );

    public static final Supplier<MenuType<EnergyQuarryDiggerMenu>> ENERGY_QUARRY_DIGGER_MENU =
            MENUS.register("energy_quarry_digger_menu", () ->
                    IMenuTypeExtension.create(
                            (windowId, inv, buf) -> new EnergyQuarryDiggerMenu(windowId, inv, buf.readBlockPos())
                    )
            );

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
    }
}
