package com.shackleshot.quarrydigger;

import java.util.function.Supplier;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.bus.api.IEventBus;

public class BlockEntityTypeInit {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, QuarryDiggerMod.MOD_ID);

    public static final Supplier<BlockEntityType<QuarryDiggerBlockEntity>> QUARRY_DIGGER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register(
                    "quarry_digger_block_entity",
                    () -> BlockEntityType.Builder.of(
                                    QuarryDiggerBlockEntity::new,
                                    QuarryDiggerMod.QUARRY_DIGGER_BLOCK.get()
                            )
                            .build(null)
            );

    public static void register(IEventBus modBus) {
        BLOCK_ENTITIES.register(modBus);
    }
}
