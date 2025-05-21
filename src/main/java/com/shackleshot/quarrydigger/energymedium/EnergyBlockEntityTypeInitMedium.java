package com.shackleshot.quarrydigger.energymedium;

import com.shackleshot.quarrydigger.QuarryDiggerMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class EnergyBlockEntityTypeInitMedium {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, QuarryDiggerMod.MOD_ID);

    public static final Supplier<BlockEntityType<EnergyQuarryDiggerBlockEntityMedium>>
            ENERGY_QUARRY_DIGGER_BLOCK_ENTITY_MEDIUM = BLOCK_ENTITIES.register(
            "energy_quarry_digger_block_entity_medium",
            () -> BlockEntityType.Builder.of(
                    EnergyQuarryDiggerBlockEntityMedium::new,
                    QuarryDiggerMod.ENERGY_QUARRY_DIGGER_BLOCK_MEDIUM.get()
            ).build(null)
    );

    public static void register(IEventBus modBus) {
        BLOCK_ENTITIES.register(modBus);
    }
}
