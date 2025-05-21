package com.shackleshot.quarrydigger.energymedium;

import java.util.List;
import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.capabilities.Capabilities;

public class EnergyQuarryDiggerBlockEntityMedium extends BlockEntity implements MenuProvider {
    public static final int CAPACITY = 20_000;

    public static final double PARTICLE_FADE_STEP = 0.1D;

    protected final int digRadius;
    protected final int energyPerOperation;
    protected final int breakInterval;

    private int breakProgress = 0;
    private int gridIndex = 0;
    private int currentY = 0;
    private int startX = 0, startZ = 0;
    private Direction facingCached = null;

    public final EnergyStorage energy = new EnergyStorage(CAPACITY, CAPACITY, CAPACITY);

    public EnergyQuarryDiggerBlockEntityMedium(BlockPos pos, BlockState state, int digRadius, int energyPerOperation, int breakInterval) {
        super(EnergyBlockEntityTypeInitMedium.ENERGY_QUARRY_DIGGER_BLOCK_ENTITY_MEDIUM.get(), pos, state);
        this.digRadius = digRadius;
        this.energyPerOperation = energyPerOperation;
        this.breakInterval = breakInterval;
        resetDiggingPosition(state.getValue(ChestBlock.FACING));
    }

    public EnergyQuarryDiggerBlockEntityMedium(BlockPos pos, BlockState state) {
        this(pos, state, 6, 20_000, 10);
    }

    private void resetDiggingPosition(Direction facing) {
        Direction back = facing.getOpposite();
        Direction right = back.getClockWise();

        BlockPos behind = worldPosition.relative(back);

        int half = digRadius / 2;
        startX = behind.getX() - right.getStepX() * (half - (digRadius % 2 == 0 ? 1 : 0));
        startZ = behind.getZ() - right.getStepZ() * (half - (digRadius % 2 == 0 ? 1 : 0));

        currentY = worldPosition.getY() - 1;
        gridIndex = 0;
        breakProgress = 0;
        facingCached = facing;
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
        if (facingCached != null) tag.putInt("Facing", facingCached.ordinal());
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
        if (tag.contains("Facing")) facingCached = Direction.values()[tag.getInt("Facing")];
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
        return Component.translatable("container.quarrydigger.energy_quarry_digger_medium");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new EnergyQuarryDiggerMenuMedium(id, inv, this.worldPosition);
    }

    public EnergyStorage getEnergyStorage() {
        return energy;
    }

    private BlockPos getGridPos(Direction right, Direction back, int dx, int dz, int y) {
        return new BlockPos(
                startX + right.getStepX() * dx + back.getStepX() * dz,
                y,
                startZ + right.getStepZ() * dx + back.getStepZ() * dz
        );
    }

    private boolean layerHasBreakables(Level level, Direction right, Direction back, int y) {
        for (int dx = 0; dx < digRadius; dx++)
            for (int dz = 0; dz < digRadius; dz++) {
                BlockPos pos = getGridPos(right, back, dx, dz, y);
                BlockState bs = level.getBlockState(pos);
                if (!bs.isAir() && !bs.is(Blocks.BEDROCK)) return true;
            }
        return false;
    }

    private int findHighestBreakableY(Level level, Direction right, Direction back, int minY, int maxY) {
        for (int y = maxY; y >= minY; y--) {
            if (layerHasBreakables(level, right, back, y)) return y;
        }
        return -1;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, EnergyQuarryDiggerBlockEntityMedium be) {
        if (level.isClientSide) return;

        Direction facing = state.getValue(ChestBlock.FACING);
        Direction back = facing.getOpposite();
        Direction right = back.getClockWise();

        if (be.facingCached == null || be.facingCached != facing) {
            be.resetDiggingPosition(facing);
        }

        int minY = 1;
        int maxY = be.worldPosition.getY() - 1;

        int yWithBlock = be.findHighestBreakableY(level, right, back, minY, maxY);
        if (yWithBlock > be.currentY) {
            be.currentY = yWithBlock;
            be.gridIndex = 0;
            be.breakProgress = 0;
            be.setChanged();
            return;
        }

        if (!be.layerHasBreakables(level, right, back, be.currentY)) {
            if (be.currentY > minY) {
                be.currentY--;
                be.gridIndex = 0;
                be.breakProgress = 0;
                be.setChanged();
            }
            return;
        }

        if (be.energy.getEnergyStored() < be.energyPerOperation) {
            be.breakProgress = 0;
            return;
        }

        int gridArea = be.digRadius * be.digRadius;
        for (int i = 0; i < gridArea; i++) {
            int idx = (be.gridIndex + i) % gridArea;
            int dx = idx % be.digRadius;
            int dz = idx / be.digRadius;
            BlockPos target = be.getGridPos(right, back, dx, dz, be.currentY);
            BlockState targetState = level.getBlockState(target);

            if (targetState.isAir() || targetState.is(Blocks.BEDROCK))
                continue;

            if (++be.breakProgress < be.breakInterval) {
                if (!level.isClientSide) {
                    double quarryX = be.worldPosition.getX() + 0.5;
                    double quarryY = be.worldPosition.getY() + 0.5;
                    double quarryZ = be.worldPosition.getZ() + 0.5;
                    double targetX = target.getX() + 0.5;
                    double targetZ = target.getZ() + 0.5;
                    double targetY = target.getY() + 0.5;

                    for (double t = 0; t <= 1; t += 0.05) {
                        double x = quarryX + t * (targetX - quarryX);
                        double z = quarryZ + t * (targetZ - quarryZ);
                        ((ServerLevel) level).sendParticles(ParticleTypes.END_ROD, x, quarryY, z, 1, 0, 0, 0, 0);
                    }
                    // Вот тут используем константу!
                    for (double y = quarryY; y >= targetY; y -= PARTICLE_FADE_STEP) {
                        ((ServerLevel) level).sendParticles(ParticleTypes.END_ROD, targetX, y, targetZ, 1, 0, 0, 0, 0);
                    }
                }
                be.gridIndex = idx;
                be.setChanged();
                return;
            }

            if (!level.isClientSide && !targetState.isAir() && !targetState.is(Blocks.BEDROCK)) {
                List<ItemStack> drops =
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
                be.energy.extractEnergy(be.energyPerOperation, false);
                be.gridIndex = (idx + 1) % gridArea;
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
