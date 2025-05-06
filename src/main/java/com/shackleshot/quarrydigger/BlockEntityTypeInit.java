// src/main/java/com/shackleshot/quarrydigger/BlockEntityTypeInit.java
package com.shackleshot.quarrydigger;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockEntityTypeInit {
    public static final DeferredRegister<BlockEntityType<?>>
            BLOCK_ENTITIES = DeferredRegister.create(
            ForgeRegistries.BLOCK_ENTITY_TYPES,
            QuarryDiggerMod.MOD_ID
    );

    public static final RegistryObject<BlockEntityType<QuarryDiggerBlockEntity>>
            QUARRY_DIGGER_BLOCK_ENTITY = BLOCK_ENTITIES.register(
            "quarry_digger_block_entity",
            () -> BlockEntityType.Builder.of(
                            QuarryDiggerBlockEntity::new,
                            QuarryDiggerMod.QUARRY_DIGGER_BLOCK.get()
                    )
                    .build(null)
    );
}
