package com.shackleshot.quarrydigger.energymedium;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

import static com.shackleshot.quarrydigger.energymedium.EnergyBlockEntityTypeInitMedium.ENERGY_QUARRY_DIGGER_BLOCK_ENTITY_MEDIUM;

public class EnergyQuarryDiggerBlockMedium extends Block implements EntityBlock {
    public static final DirectionProperty FACING = ChestBlock.FACING;

    public EnergyQuarryDiggerBlockMedium(Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EnergyQuarryDiggerBlockEntityMedium(pos, state);
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        if (!level.isClientSide) {
            popResource(level, pos, new ItemStack(this));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (type == ENERGY_QUARRY_DIGGER_BLOCK_ENTITY_MEDIUM.get()) {
            return (lvl, p, s, be) ->
                    EnergyQuarryDiggerBlockEntityMedium.tick(lvl, p, s, (EnergyQuarryDiggerBlockEntityMedium) be);
        }
        return null;
    }
}
