package com.shackleshot.quarrydigger;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(QuarryDiggerMod.MOD_ID)
public class QuarryDiggerMod {
    public static final String MOD_ID = "quarrydigger";

    private static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);

    public static final RegistryObject<Block> QUARRY_DIGGER_BLOCK = BLOCKS.register(
            "quarry_digger_block",
            () -> new QuarryDiggerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(4.0f))
    );

    public static final RegistryObject<Item> QUARRY_DIGGER_ITEM = ITEMS.register(
            "quarry_digger_block",
            () -> new BlockItem(QUARRY_DIGGER_BLOCK.get(), new Item.Properties())
    );

    public QuarryDiggerMod() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BlockEntityTypeInit.BLOCK_ENTITIES.register(bus);
        MenuTypeInit.MENUS.register(bus);
        SoundInit.register(); // Register sounds
        System.out.println("[QuarryDigger] Mod initialized");
    }
}