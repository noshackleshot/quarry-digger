package com.shackleshot.quarrydigger;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.bus.api.IEventBus;

public class CreativeTabInit {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB, QuarryDiggerMod.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN =
            TABS.register("main", () -> CreativeModeTab.builder()
                    .title(net.minecraft.network.chat.Component.translatable("itemGroup.quarrydigger"))
                    .icon(() -> new ItemStack(QuarryDiggerMod.QUARRY_DIGGER_BLOCK.get()))
                    .displayItems((params, out) -> {
                        out.accept(QuarryDiggerMod.QUARRY_DIGGER_ITEM.get());
                        out.accept(QuarryDiggerMod.ENERGY_QUARRY_DIGGER_ITEM.get());
                        out.accept(QuarryDiggerMod.ENERGY_QUARRY_DIGGER_ITEM_MEDIUM.get());
                        out.accept(QuarryDiggerMod.QUARRY_CORE_ITEM.get());
                    })
                    .build());

    public static void register(IEventBus modBus) {
        TABS.register(modBus);
    }
}
