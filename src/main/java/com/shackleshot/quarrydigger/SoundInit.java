package com.shackleshot.quarrydigger;

import net.minecraft.core.registries.BuiltInRegistries;            // реестр звуков в NeoForge
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

public class SoundInit {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, QuarryDiggerMod.MOD_ID);

    public static final Holder<SoundEvent> QUARRY_DIGGER_WORKING = SOUND_EVENTS.register(
            "quarry_digger_working",
            SoundEvent::createVariableRangeEvent
    );
}
