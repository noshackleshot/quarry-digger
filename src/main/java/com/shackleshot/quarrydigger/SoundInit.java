package com.shackleshot.quarrydigger;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class SoundInit {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, QuarryDiggerMod.MOD_ID);

    public static final RegistryObject<SoundEvent> QUARRY_DIGGER_WORKING = SOUNDS.register(
            "quarry_digger_working",
            () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(QuarryDiggerMod.MOD_ID, "quarry_digger_working")
            )
    );

    public static void register() {
        SOUNDS.register(FMLJavaModLoadingContext.get().getModEventBus());
        System.out.println("[QuarryDigger] Registering sound: quarrydigger:quarry_digger_working");
    }
}