package com.shackleshot.quarrydigger;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import java.util.List;

import static net.minecraft.world.level.block.ChestBlock.FACING;

public class QuarryDiggerBlockEntity extends BlockEntity implements MenuProvider {
    public final ItemStackHandler inventory = new ItemStackHandler(1);
    private final LazyOptional<IItemHandler> invCap = LazyOptional.of(() -> inventory);

    private int burnTime;
    private static final int BURN_TIME_PER_COAL = 1600;
    private static final int BREAK_INTERVAL = 20;

    private int breakProgress;
    private int gridIndex;
    private int currentY;
    private int startX;
    private int startZ;
    private boolean diggingComplete = false;

    public QuarryDiggerBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeInit.QUARRY_DIGGER_BLOCK_ENTITY.get(), pos, state);
        resetDiggingPosition();
    }

    private void resetDiggingPosition() {
        Direction facing = getBlockState().getValue(FACING);
        Direction back = facing.getOpposite();
        Direction left = back.getCounterClockWise();
        BlockPos base = worldPosition.relative(back).relative(left);

        startX = base.getX();
        startZ = base.getZ();
        currentY = worldPosition.getY() - 1;
        gridIndex = 0;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider prov) {
        super.saveAdditional(tag, prov);
        tag.putInt("BurnTime", burnTime);
        tag.putBoolean("DiggingComplete", diggingComplete);
        tag.putInt("BreakProgress", breakProgress);
        tag.putInt("GridIndex", gridIndex);
        tag.putInt("CurrentY", currentY);
        tag.putInt("StartX", startX);
        tag.putInt("StartZ", startZ);
        tag.put("Inv", inventory.serializeNBT(prov));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider prov) {
        super.loadAdditional(tag, prov);
        burnTime = tag.getInt("BurnTime");
        diggingComplete = tag.getBoolean("DiggingComplete");
        breakProgress = tag.getInt("BreakProgress");
        gridIndex = tag.getInt("GridIndex");
        currentY = tag.getInt("CurrentY");
        startX = tag.getInt("StartX");
        startZ = tag.getInt("StartZ");
        inventory.deserializeNBT(prov, tag.getCompound("Inv"));
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
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return invCap.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Coal Input");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new QuarryDiggerMenu(id, inv, this.worldPosition);
    }

    private void sync() {
        if (!level.isClientSide && level instanceof ServerLevel server) {
            server.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, QuarryDiggerBlockEntity be) {
        if (level.isClientSide) return;
        if (be.diggingComplete) return;

        int prevBurn = be.burnTime;

        // Подгружаем уголь
        if (be.burnTime <= 0
                && be.inventory.getStackInSlot(0).getCount() > 0) {
            be.inventory.extractItem(0, 1, false);
            be.burnTime = BURN_TIME_PER_COAL;
            be.breakProgress = 0;
            be.resetDiggingPosition();
            be.setChanged();
            be.sync();
        }

        if (be.burnTime > 0) {
            Direction facing = state.getValue(FACING);
            Direction back = facing.getOpposite();
            Direction right = back.getClockWise();

            // 1) Ищем блоки в 3×3 области СВЕРХУ currentY (до worldY-1)
            int topY = be.worldPosition.getY() - 1;
            boolean foundAbove = false;
            outer:
            for (int y = topY; y > be.currentY; y--) {
                for (int dx = 0; dx < 3; dx++) {
                    for (int dz = 0; dz < 3; dz++) {
                        int x = be.startX + right.getStepX() * dx + back.getStepX() * dz;
                        int z = be.startZ + right.getStepZ() * dx + back.getStepZ() * dz;
                        BlockPos check = new BlockPos(x, y, z);
                        BlockState s = level.getBlockState(check);
                        if (!s.isAir() && !s.is(Blocks.BEDROCK)) {
                            be.currentY = y;
                            be.gridIndex = 0;
                            be.breakProgress = 0;
                            foundAbove = true;
                            break outer;
                        }
                    }
                }
            }
            if (foundAbove) {
                be.setChanged();
                be.sync();
            }

            // 2) Проверяем сам слой currentY
            boolean layerHasBlocks = false;
            for (int dx = 0; dx < 3 && !layerHasBlocks; dx++) {
                for (int dz = 0; dz < 3; dz++) {
                    int x = be.startX + right.getStepX() * dx + back.getStepX() * dz;
                    int z = be.startZ + right.getStepZ() * dx + back.getStepZ() * dz;
                    BlockPos target = new BlockPos(x, be.currentY, z);
                    BlockState ts = level.getBlockState(target);
                    if (!ts.isAir() && !ts.is(Blocks.BEDROCK)) {
                        layerHasBlocks = true;
                        break;
                    }
                }
            }

            if (!layerHasBlocks) {
                // переход на следующий слой вниз
                be.gridIndex = 0;
                be.currentY--;
                be.breakProgress = 0;
                if (be.currentY < level.getMinBuildHeight()) {
                    be.burnTime = 0;
                    be.diggingComplete = true;
                }
                be.setChanged();
                be.sync();
                return;
            }

            // 3) Разрушаем блок по таймеру
            if (be.breakProgress++ >= BREAK_INTERVAL) {
                int dx = be.gridIndex % 3;
                int dz = be.gridIndex / 3;
                int x = be.startX + right.getStepX() * dx + back.getStepX() * dz;
                int z = be.startZ + right.getStepZ() * dx + back.getStepZ() * dz;
                BlockPos target = new BlockPos(x, be.currentY, z);
                BlockState ts = level.getBlockState(target);

                if (!ts.isAir() && !ts.is(Blocks.BEDROCK)) {
                    List<ItemStack> drops = Block.getDrops(ts, (ServerLevel) level, target, null);
                    BlockPos outputPos = pos.relative(facing);
                    BlockEntity outBe = level.getBlockEntity(outputPos);
                    LazyOptional<IItemHandler> outCap = (outBe != null)
                            ? outBe.getCapability(ForgeCapabilities.ITEM_HANDLER, facing.getOpposite())
                            : LazyOptional.empty();

                    for (ItemStack drop : drops) {
                        ItemStack rem = drop.copy();
                        if (outCap.isPresent()) {
                            IItemHandler h = outCap.orElse(null);
                            for (int slot = 0; slot < h.getSlots() && !rem.isEmpty(); slot++) {
                                rem = h.insertItem(slot, rem, false);
                            }
                        }
                        if (!rem.isEmpty()) {
                            Block.popResource(level, pos.above(), rem);
                        }
                    }
                    level.setBlock(target, Blocks.AIR.defaultBlockState(), 3);
                }

                be.gridIndex = (be.gridIndex + 1) % 9;
                be.breakProgress = 0;
                be.setChanged();
            }
        }

        if (be.burnTime != prevBurn) {
            be.sync();
        }
    }
}
