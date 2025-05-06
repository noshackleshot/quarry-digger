package com.shackleshot.quarrydigger;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = QuarryDiggerMod.MOD_ID,
        bus   = Mod.EventBusSubscriber.Bus.FORGE
)
public class ForgeEventSubscriber {

    @SubscribeEvent
    public static void onRightClickBlock(RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        BlockPos pos = event.getPos();
        if (!level.getBlockState(pos).is(QuarryDiggerMod.QUARRY_DIGGER_BLOCK.get()))
            return;

        Player player = event.getEntity();  // <- здесь getEntity(), а не getPlayer() :contentReference[oaicite:0]{index=0}
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof MenuProvider provider)) return;

        serverPlayer.openMenu(provider, (FriendlyByteBuf buf) -> buf.writeBlockPos(pos));
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }
}
