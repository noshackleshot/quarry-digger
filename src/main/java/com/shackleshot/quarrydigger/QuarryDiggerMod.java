package com.shackleshot.quarrydigger;

import com.shackleshot.quarrydigger.energy.EnergyBlockEntityTypeInit;
import com.shackleshot.quarrydigger.energy.EnergyQuarryDiggerBlock;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredRegister.Blocks;
import net.neoforged.neoforge.registries.DeferredRegister.Items;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@Mod(QuarryDiggerMod.MOD_ID)
public class QuarryDiggerMod {
    public static final String MOD_ID = "quarrydigger";

    public static final Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    public static final Items ITEMS = DeferredRegister.createItems(MOD_ID);

    public static final DeferredItem<Item> QUARRY_CORE_ITEM =
            ITEMS.register("quarry_core", () -> new Item(new Item.Properties()));

    public static final DeferredBlock<QuarryDiggerBlock> QUARRY_DIGGER_BLOCK =
            BLOCKS.registerBlock(
                    "quarry_digger_block",
                    QuarryDiggerBlock::new,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(4.0f)
            );

    public static final DeferredItem<BlockItem> QUARRY_DIGGER_ITEM =
            ITEMS.registerSimpleBlockItem(
                    "quarry_digger_block",
                    QUARRY_DIGGER_BLOCK,
                    new Item.Properties()
            );

    public static final DeferredBlock<EnergyQuarryDiggerBlock> ENERGY_QUARRY_DIGGER_BLOCK =
            BLOCKS.registerBlock(
                    "energy_quarry_digger_block",
                    EnergyQuarryDiggerBlock::new,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(4.0f)
            );

    public static final DeferredItem<BlockItem> ENERGY_QUARRY_DIGGER_ITEM =
            ITEMS.registerSimpleBlockItem(
                    "energy_quarry_digger_block",
                    ENERGY_QUARRY_DIGGER_BLOCK
            );

    public QuarryDiggerMod(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BlockEntityTypeInit.BLOCK_ENTITIES.register(modBus);
        MenuTypeInit.MENUS.register(modBus);
        SoundInit.SOUND_EVENTS.register(modBus);
        EnergyBlockEntityTypeInit.BLOCK_ENTITIES.register(modBus);

        CreativeTabInit.register(modBus);

        modBus.addListener(this::registerCapabilities);

        System.out.println("[QuarryDigger] Mod initialized");
    }

    @SubscribeEvent
    public void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                EnergyBlockEntityTypeInit.ENERGY_QUARRY_DIGGER_BLOCK_ENTITY.get(),
                (be, side) -> be.getEnergyStorage()
        );
    }
}