package com.shackleshot.quarrydigger;

import com.shackleshot.quarrydigger.energy.EnergyQuarryDiggerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock;

@EventBusSubscriber(
        modid = QuarryDiggerMod.MOD_ID,
        bus   = Bus.GAME
)
public class ForgeEventSubscriber {
    @SubscribeEvent
    public static void onRightClickBlock(RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide() || event.getHand() != InteractionHand.MAIN_HAND) return;

        BlockPos pos = event.getPos();
        // Проверяем и стандартный блок, и энерго-блок
        var state = level.getBlockState(pos);
        if (!state.is(QuarryDiggerMod.QUARRY_DIGGER_BLOCK.get()) &&
                !state.is(QuarryDiggerMod.ENERGY_QUARRY_DIGGER_BLOCK.get())) {
            return;
        }

        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof MenuProvider provider)) return;

        serverPlayer.openMenu(provider, pos);
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }
}
