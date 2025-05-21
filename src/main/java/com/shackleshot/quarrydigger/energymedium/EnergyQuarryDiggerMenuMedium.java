package com.shackleshot.quarrydigger.energymedium;

import com.shackleshot.quarrydigger.MenuTypeInit;
import com.shackleshot.quarrydigger.QuarryDiggerMod;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;

public class EnergyQuarryDiggerMenuMedium extends AbstractContainerMenu {
    public final EnergyQuarryDiggerBlockEntityMedium be;
    private final ContainerLevelAccess access;
    private final DataSlot energySlot;
    private final DataSlot capacitySlot;

    public EnergyQuarryDiggerMenuMedium(int id, Inventory inv, BlockPos pos) {
        super(MenuTypeInit.ENERGY_QUARRY_DIGGER_MENU_MEDIUM.get(), id);
        this.access = ContainerLevelAccess.create(inv.player.level(), pos);

        var te = inv.player.level().getBlockEntity(pos);
        if (!(te instanceof EnergyQuarryDiggerBlockEntityMedium)) {
            throw new IllegalStateException("Wrong BlockEntity!");
        }
        this.be = (EnergyQuarryDiggerBlockEntityMedium) te;

        this.energySlot = new DataSlot() {
            @Override public int get() { return be.energy.getEnergyStored(); }
            @Override public void set(int value) { /* серверный слот */ }
        };
        this.capacitySlot = new DataSlot() {
            @Override public int get() { return EnergyQuarryDiggerBlockEntityMedium.CAPACITY; }
            @Override public void set(int value) { }
        };
        addDataSlot(energySlot);
        addDataSlot(capacitySlot);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, QuarryDiggerMod.ENERGY_QUARRY_DIGGER_BLOCK_MEDIUM.get());
    }

    public int getEnergyStored() {
        return energySlot.get();
    }

    public int getCapacity() {
        return capacitySlot.get();
    }
}