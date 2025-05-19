package com.shackleshot.quarrydigger.energy;

import java.util.List;
import java.util.Optional;

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
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.capabilities.Capabilities;

public class EnergyQuarryDiggerBlockEntity extends BlockEntity implements MenuProvider {
    public static final int CAPACITY = 10_000;
    public static final int ENERGY_PER_OPERATION = 10_000;
    public static final int BREAK_INTERVAL = 20;

    private int breakProgress = 0;
    private int gridIndex = 0;
    private int currentY = 0;
    private int startX = 0, startZ = 0;
    private boolean searchingAbove = false;

    public final EnergyStorage energy = new EnergyStorage(CAPACITY, CAPACITY, CAPACITY);

    public EnergyQuarryDiggerBlockEntity(BlockPos pos, BlockState state) {
        super(EnergyBlockEntityTypeInit.ENERGY_QUARRY_DIGGER_BLOCK_ENTITY.get(), pos, state);
        resetDiggingPosition();
    }

    private void resetDiggingPosition() {
        Direction facing = getBlockState().getValue(ChestBlock.FACING);
        Direction back = facing.getOpposite();
        Direction left = back.getCounterClockWise();
        BlockPos base = worldPosition.relative(back).relative(left);

        startX = base.getX();
        startZ = base.getZ();
        currentY = worldPosition.getY() - 1;
        gridIndex = 0;
        breakProgress = 0;
        searchingAbove = false;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider prov) {
        super.saveAdditional(tag, prov);
        tag.putInt("Energy", energy.getEnergyStored());
        tag.putInt("BreakProgress", breakProgress);
        tag.putInt("GridIndex", gridIndex);
        tag.putInt("CurrentY", currentY);
        tag.putInt("StartX", startX);
        tag.putInt("StartZ", startZ);
        tag.putBoolean("SearchingAbove", searchingAbove);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider prov) {
        super.loadAdditional(tag, prov);
        energy.extractEnergy(energy.getEnergyStored(), false);
        energy.receiveEnergy(tag.getInt("Energy"), false);
        breakProgress = tag.getInt("BreakProgress");
        gridIndex = tag.getInt("GridIndex");
        currentY = tag.getInt("CurrentY");
        startX = tag.getInt("StartX");
        startZ = tag.getInt("StartZ");
        searchingAbove = tag.getBoolean("SearchingAbove");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider prov) {
        CompoundTag tag = super.getUpdateTag(prov);
        saveAdditional(tag, prov);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider prov) {
        loadAdditional(tag, prov);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider prov) {
        super.onDataPacket(net, pkt, prov);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.quarrydigger.energy_quarry_digger");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new EnergyQuarryDiggerMenu(id, inv, this.worldPosition);
    }

    public EnergyStorage getEnergyStorage() {
        return energy;
    }

    private BlockPos getGridPos(Direction right, Direction back, int dx, int dz, int y) {
        int x = startX + right.getStepX() * dx + back.getStepX() * dz;
        int z = startZ + right.getStepZ() * dx + back.getStepZ() * dz;
        return new BlockPos(x, y, z);
    }

    private static boolean layerHasBreakables(Level level, Direction right, Direction back, int startX, int startZ, int y) {
        for (int dx = 0; dx < 3; dx++)
            for (int dz = 0; dz < 3; dz++) {
                int x = startX + right.getStepX() * dx + back.getStepX() * dz;
                int z = startZ + right.getStepZ() * dx + back.getStepZ() * dz;
                BlockState bs = level.getBlockState(new BlockPos(x, y, z));
                if (!bs.isAir() && !bs.is(Blocks.BEDROCK)) return true;
            }
        return false;
    }

    private static int findHighestBreakableY(Level level, Direction right, Direction back, int startX, int startZ, int minY, int maxY) {
        for (int y = maxY; y >= minY; y--) {
            if (layerHasBreakables(level, right, back, startX, startZ, y)) return y;
        }
        return -1;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, EnergyQuarryDiggerBlockEntity be) {
        if (level.isClientSide) return;

        Direction facing = state.getValue(ChestBlock.FACING);
        Direction back = facing.getOpposite();
        Direction right = back.getClockWise();
        int minY = 1; // protect from mining the void, change if needed
        int maxY = be.worldPosition.getY() - 1;

        int yWithBlock = findHighestBreakableY(level, right, back, be.startX, be.startZ, minY, maxY);
        if (yWithBlock > be.currentY) {
            be.currentY = yWithBlock;
            be.gridIndex = 0;
            be.breakProgress = 0;
            be.searchingAbove = true;
            be.setChanged();
            return;
        }
        be.searchingAbove = false;

        if (!layerHasBreakables(level, right, back, be.startX, be.startZ, be.currentY)) {
            if (be.currentY > minY) {
                be.currentY--;
                be.gridIndex = 0;
                be.breakProgress = 0;
                be.setChanged();
            }
            return;
        }

        if (be.energy.getEnergyStored() < ENERGY_PER_OPERATION) {
            be.breakProgress = 0;
            return;
        }

        for (int i = 0; i < 9; i++) {
            int idx = (be.gridIndex + i) % 9;
            int dx = idx % 3;
            int dz = idx / 3;
            BlockPos target = be.getGridPos(right, back, dx, dz, be.currentY);
            BlockState targetState = level.getBlockState(target);

            if (targetState.isAir() || targetState.is(Blocks.BEDROCK))
                continue;

            if (++be.breakProgress < BREAK_INTERVAL) {
                be.gridIndex = idx;
                be.setChanged();
                return;
            }

            if (!level.isClientSide && !targetState.isAir() && !targetState.is(Blocks.BEDROCK)) {
                List<net.minecraft.world.item.ItemStack> drops =
                        Block.getDrops(targetState, (ServerLevel) level, target, null);
                BlockPos outPos = pos.relative(facing);
                Optional<IItemHandler> handlerOpt = Optional.ofNullable(level.getCapability(
                        Capabilities.ItemHandler.BLOCK,
                        outPos,
                        facing.getOpposite()
                ));
                IItemHandler handler = handlerOpt.orElse(null);

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
                be.energy.extractEnergy(ENERGY_PER_OPERATION, false);
                be.gridIndex = (idx + 1) % 9;
                be.breakProgress = 0;
                be.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
                return;
            }
        }
        be.breakProgress = 0;
        be.gridIndex = 0;
    }
}
