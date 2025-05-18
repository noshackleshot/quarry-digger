package com.shackleshot.quarrydigger.common;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.capabilities.Capabilities;

public abstract class AbstractQuarryDiggerBlockEntity extends BlockEntity implements MenuProvider {
    protected int breakProgress, gridIndex, currentY, startX, startZ;
    protected static final int BREAK_INTERVAL = 20;

    public AbstractQuarryDiggerBlockEntity(
            BlockEntityType<?> type,
            BlockPos pos,
            BlockState state
    ) {
        super(type, pos, state);
        resetDiggingPosition();
    }

    protected void resetDiggingPosition() {
        Direction facing = getBlockState().getValue(ChestBlock.FACING);
        Direction back   = facing.getOpposite();
        Direction left   = back.getCounterClockWise();
        BlockPos base    = worldPosition.relative(back).relative(left);

        startX        = base.getX();
        startZ        = base.getZ();
        currentY      = worldPosition.getY() - 1;
        gridIndex     = 0;
        breakProgress = 0;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider prov) {
        super.saveAdditional(tag, prov);
        tag.putInt("BreakProgress", breakProgress);
        tag.putInt("GridIndex", gridIndex);
        tag.putInt("CurrentY", currentY);
        tag.putInt("StartX", startX);
        tag.putInt("StartZ", startZ);
        saveExtraData(tag, prov);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider prov) {
        super.loadAdditional(tag, prov);
        breakProgress = tag.getInt("BreakProgress");
        gridIndex     = tag.getInt("GridIndex");
        currentY      = tag.getInt("CurrentY");
        startX        = tag.getInt("StartX");
        startZ        = tag.getInt("StartZ");
        loadExtraData(tag, prov);
    }

    protected abstract void saveExtraData(CompoundTag tag, HolderLookup.Provider prov);
    protected abstract void loadExtraData(CompoundTag tag, HolderLookup.Provider prov);
    protected abstract boolean canOperate();
    protected abstract void onOperation();

    public static <T extends AbstractQuarryDiggerBlockEntity> void commonTick(
            Level level,
            BlockPos pos,
            BlockState state,
            T be
    ) {
        if (level.isClientSide() || !be.canOperate()) {
            return;
        }

        Direction facing = state.getValue(ChestBlock.FACING);
        Direction back   = facing.getOpposite();
        Direction right  = back.getClockWise();

        // 1) Find new highest block above currentY
        int topY = be.worldPosition.getY() - 1;
        boolean foundAbove = false;
        outer:
        for (int y = topY; y > be.currentY; y--) {
            for (int dx = 0; dx < 3; dx++) {
                for (int dz = 0; dz < 3; dz++) {
                    int x = be.startX + right.getStepX() * dx + back.getStepX() * dz;
                    int z = be.startZ + right.getStepZ() * dx + back.getStepZ() * dz;
                    BlockPos check = new BlockPos(x, y, z);
                    var st = level.getBlockState(check);
                    if (!st.isAir() && !st.is(Blocks.BEDROCK)) {
                        be.currentY      = y;
                        be.gridIndex     = 0;
                        be.breakProgress = 0;
                        foundAbove       = true;
                        break outer;
                    }
                }
            }
        }
        if (foundAbove) {
            be.setChanged();
        }

        // 2) Check if layer has any blocks left
        boolean layerHasBlocks = false;
        for (int dx = 0; dx < 3 && !layerHasBlocks; dx++) {
            for (int dz = 0; dz < 3; dz++) {
                int x = be.startX + right.getStepX() * dx + back.getStepX() * dz;
                int z = be.startZ + right.getStepZ() * dx + back.getStepZ() * dz;
                if (!level.getBlockState(new BlockPos(x, be.currentY, z)).isAir()) {
                    layerHasBlocks = true;
                    break;
                }
            }
        }
        if (!layerHasBlocks) {
            be.currentY--;
            be.gridIndex     = 0;
            be.breakProgress = 0;
            be.setChanged();
            level.invalidateCapabilities(pos);
            return;
        }

        // 3) Break blocks in sequence
        if (be.breakProgress++ >= BREAK_INTERVAL) {
            int dx = be.gridIndex % 3;
            int dz = be.gridIndex / 3;
            int x  = be.startX + right.getStepX() * dx + back.getStepX() * dz;
            int z  = be.startZ + right.getStepZ() * dx + back.getStepZ() * dz;
            BlockPos target = new BlockPos(x, be.currentY, z);
            var ts = level.getBlockState(target);

            if (!ts.isAir() && !ts.is(Blocks.BEDROCK)) {
                List<net.minecraft.world.item.ItemStack> drops =
                        Block.getDrops(ts, (ServerLevel) level, target, null);

                BlockPos outPos = pos.relative(facing);
                IItemHandler handler = level.getCapability(
                        Capabilities.ItemHandler.BLOCK,
                        outPos,
                        facing.getOpposite()
                );

                for (var drop : drops) {
                    var rem = drop.copy();
                    if (handler != null) {
                        for (int slot = 0; slot < handler.getSlots() && !rem.isEmpty(); slot++) {
                            rem = handler.insertItem(slot, rem, false);
                        }
                    }
                    if (!rem.isEmpty()) {
                        Block.popResource(level, pos.above(), rem);
                    }
                }

                level.setBlock(target, Blocks.AIR.defaultBlockState(), 3);
                be.onOperation();
                be.setChanged();
                level.invalidateCapabilities(pos);
                level.sendBlockUpdated(pos, state, state, 3);
            }

            be.gridIndex     = (be.gridIndex + 1) % 9;
            be.breakProgress = 0;
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.literal(""); // Подклассы переопределяют по необходимости
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return null; // Подклассы переопределяют по необходимости
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider prov) {
        CompoundTag tag = super.getUpdateTag(prov);
        saveAdditional(tag, prov);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(
            Connection net,
            ClientboundBlockEntityDataPacket pkt,
            HolderLookup.Provider prov
    ) {
        super.onDataPacket(net, pkt, prov);
    }
}