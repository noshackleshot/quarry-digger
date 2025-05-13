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

    // 5) Его BlockItem (вариант без указания свойств, использует дефолтные)
    public static final DeferredItem<BlockItem> ENERGY_QUARRY_DIGGER_ITEM =
            ITEMS.registerSimpleBlockItem(
                    "energy_quarry_digger_block",
                    ENERGY_QUARRY_DIGGER_BLOCK
            );

    /**
     * Конструктор мода, вызывается NeoForge.
     * В него передаётся только модовая шина событий.
     */
    public QuarryDiggerMod(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BlockEntityTypeInit.BLOCK_ENTITIES.register(modBus);
        MenuTypeInit.MENUS.register(modBus);
        SoundInit.SOUND_EVENTS.register(modBus);
        EnergyBlockEntityTypeInit.BLOCK_ENTITIES.register(modBus);

        // Регистрация события для capabilities
        modBus.addListener(this::registerCapabilities);

        System.out.println("[QuarryDigger] Mod initialized");
    }

    /**
     * Регистрация capabilities для блок-сущностей.
     * Это необходимо для совместимости с новой системой NeoForge 20.3 и Mekanism.
     */
    @SubscribeEvent
    public void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK, // Тип capability для энергии
                EnergyBlockEntityTypeInit.ENERGY_QUARRY_DIGGER_BLOCK_ENTITY.get(), // Тип блок-сущности
                (be, side) -> be.getEnergyStorage() // Лямбда, возвращающая IEnergyStorage
        );
    }
}