package com.shackleshot.quarrydigger.energy;

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

public class EnergyQuarryDiggerBlockEntity extends BlockEntity implements MenuProvider {
    public static final int CAPACITY = 10_000;

    protected final int digRadius;
    protected final int energyPerOperation;
    protected final int breakInterval;

    private int breakProgress = 0;
    private int gridIndex = 0;
    private int startX = 0, startZ = 0;

    public final EnergyStorage energy = new EnergyStorage(CAPACITY, CAPACITY, CAPACITY);

    public EnergyQuarryDiggerBlockEntity(BlockPos pos, BlockState state, int digRadius, int energyPerOperation, int breakInterval) {
        super(EnergyBlockEntityTypeInit.ENERGY_QUARRY_DIGGER_BLOCK_ENTITY.get(), pos, state);
        this.digRadius = digRadius;
        this.energyPerOperation = energyPerOperation;
        this.breakInterval = breakInterval;
        resetDiggingPosition();
    }

    // Для совместимости (стандартный 3x3, 10_000 энергии, 20 тиков на блок)
    public EnergyQuarryDiggerBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, 3, 10_000, 20);
    }

    private void resetDiggingPosition() {
        Direction facing = getBlockState().getValue(ChestBlock.FACING);
        Direction back = facing.getOpposite();
        Direction left = back.getCounterClockWise();
        BlockPos base = worldPosition.relative(back).relative(left);

        startX = base.getX();
        startZ = base.getZ();
        gridIndex = 0;
        breakProgress = 0;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider prov) {
        super.saveAdditional(tag, prov);
        tag.putInt("Energy", energy.getEnergyStored());
        tag.putInt("BreakProgress", breakProgress);
        tag.putInt("GridIndex", gridIndex);
        tag.putInt("StartX", startX);
        tag.putInt("StartZ", startZ);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider prov) {
        super.loadAdditional(tag, prov);
        energy.extractEnergy(energy.getEnergyStored(), false);
        energy.receiveEnergy(tag.getInt("Energy"), false);
        breakProgress = tag.getInt("BreakProgress");
        gridIndex = tag.getInt("GridIndex");
        startX = tag.getInt("StartX");
        startZ = tag.getInt("StartZ");
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

    // Получение позиции в сетке копания любого радиуса
    private BlockPos getGridPos(Direction right, Direction back, int dx, int dz, int y) {
        int x = startX + right.getStepX() * dx + back.getStepX() * dz;
        int z = startZ + right.getStepZ() * dx + back.getStepZ() * dz;
        return new BlockPos(x, y, z);
    }

    // Получаем "поверхность" - высоты верхних твёрдых блоков в каждой колонке
    private int[] getSurfaceHeights(Level level, Direction right, Direction back) {
        int[] surface = new int[digRadius * digRadius];
        int minY = level.getMinBuildHeight();
        int maxY = worldPosition.getY() - 1;
        for (int dx = 0; dx < digRadius; dx++) {
            for (int dz = 0; dz < digRadius; dz++) {
                int idx = dx + dz * digRadius;
                surface[idx] = minY; // по умолчанию - минимум мира (вдруг нет ни одного блока)
                for (int y = maxY; y >= minY; y--) {
                    BlockPos pos = getGridPos(right, back, dx, dz, y);
                    BlockState bs = level.getBlockState(pos);
                    if (!bs.isAir() && !bs.is(Blocks.BEDROCK)) {
                        surface[idx] = y;
                        break;
                    }
                }
            }
        }
        return surface;
    }

    // Гладкая ли поверхность?
    private boolean isSurfaceFlat(int[] surface) {
        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        for (int h : surface) {
            if (h < min) min = h;
            if (h > max) max = h;
        }
        return max - min <= 1;
    }

    // Возвращает следующий блок для копания — в зависимости от типа поверхности
    private BlockPos findNextBreakableSmart(Level level, Direction right, Direction back) {
        int minY = level.getMinBuildHeight();
        int maxY = worldPosition.getY() - 1;
        int gridArea = digRadius * digRadius;
        int startIdx = gridIndex;
        int[] surface = getSurfaceHeights(level, right, back);

        if (isSurfaceFlat(surface)) {
            // Если поверхность гладкая — копаем по слоям, сверху вниз
            for (int y = maxY; y >= minY; y--) {
                for (int tries = 0; tries < gridArea; tries++) {
                    int idx = (startIdx + tries) % gridArea;
                    int dx = idx % digRadius;
                    int dz = idx / digRadius;
                    BlockPos target = getGridPos(right, back, dx, dz, y);
                    BlockState state = level.getBlockState(target);
                    if (!state.isAir() && !state.is(Blocks.BEDROCK)) {
                        gridIndex = idx;
                        return target;
                    }
                }
            }
        } else {
            // Поверхность не ровная — ищем верхний из всех доступных блоков (максимальный surface)
            int maxSurface = Integer.MIN_VALUE;
            for (int idx = 0; idx < gridArea; idx++) {
                if (surface[idx] > maxSurface) {
                    maxSurface = surface[idx];
                }
            }
            for (int tries = 0; tries < gridArea; tries++) {
                int idx = (startIdx + tries) % gridArea;
                if (surface[idx] == maxSurface) {
                    int dx = idx % digRadius;
                    int dz = idx / digRadius;
                    BlockPos target = getGridPos(right, back, dx, dz, maxSurface);
                    BlockState state = level.getBlockState(target);
                    if (!state.isAir() && !state.is(Blocks.BEDROCK)) {
                        gridIndex = idx;
                        return target;
                    }
                }
            }
        }
        return null;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, EnergyQuarryDiggerBlockEntity be) {
        if (level.isClientSide) return;

        Direction facing = state.getValue(ChestBlock.FACING);
        Direction back = facing.getOpposite();
        Direction right = back.getClockWise();
        int gridArea = be.digRadius * be.digRadius;

        if (be.energy.getEnergyStored() < be.energyPerOperation) {
            be.breakProgress = 0;
            return;
        }

        BlockPos target = be.findNextBreakableSmart(level, right, back);

        if (target == null) {
            be.breakProgress = 0;
            return;
        }

        BlockState targetState = level.getBlockState(target);

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
                for (double y = quarryY; y >= targetY; y -= 0.1) {
                    ((ServerLevel) level).sendParticles(ParticleTypes.END_ROD, targetX, y, targetZ, 1, 0, 0, 0, 0);
                }
            }
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
            be.breakProgress = 0;
            be.gridIndex = (be.gridIndex + 1) % gridArea;
            be.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }
}
