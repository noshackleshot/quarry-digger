package com.shackleshot.quarrydigger.energy;

import java.util.function.Supplier;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.bus.api.IEventBus;

import com.shackleshot.quarrydigger.QuarryDiggerMod;

public class EnergyBlockEntityTypeInit {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, QuarryDiggerMod.MOD_ID);

    public static final Supplier<BlockEntityType<EnergyQuarryDiggerBlockEntity>>
            ENERGY_QUARRY_DIGGER_BLOCK_ENTITY = BLOCK_ENTITIES.register(
            "energy_quarry_digger_block_entity",
            () -> BlockEntityType.Builder.of(
                            EnergyQuarryDiggerBlockEntity::new,
                            QuarryDiggerMod.ENERGY_QUARRY_DIGGER_BLOCK.get()
                    )
                    .build(null)
    );

    public static void register(IEventBus modBus) {
        BLOCK_ENTITIES.register(modBus);
    }
}
