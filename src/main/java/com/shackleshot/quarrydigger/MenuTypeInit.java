// src/main/java/com/shackleshot/quarrydigger/MenuTypeInit.java
package com.shackleshot.quarrydigger;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class MenuTypeInit {
    public static final DeferredRegister<MenuType<?>>
            MENUS = DeferredRegister.create(
            ForgeRegistries.MENU_TYPES,
            QuarryDiggerMod.MOD_ID
    );

    public static final RegistryObject<MenuType<QuarryDiggerMenu>>
            QUARRY_DIGGER_MENU = MENUS.register(
            "quarry_digger_menu",
            () -> IForgeMenuType.create(
                    (windowId, inv, data) ->
                            new QuarryDiggerMenu(windowId, inv, data.readBlockPos())
            )
    );
}
