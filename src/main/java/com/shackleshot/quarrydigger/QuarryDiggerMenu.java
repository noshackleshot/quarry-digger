// src/main/java/com/shackleshot/quarrydigger/QuarryDiggerMenu.java
package com.shackleshot.quarrydigger;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class QuarryDiggerMenu extends AbstractContainerMenu {
    private final QuarryDiggerBlockEntity blockEntity;
    private final ItemStackHandler inventory;
    private final ContainerLevelAccess access;

    public QuarryDiggerMenu(int id, Inventory playerInventory, BlockPos pos) {
        super(MenuTypeInit.QUARRY_DIGGER_MENU.get(), id);
        this.access = ContainerLevelAccess.create(
                playerInventory.player.level(), pos
        );

        var be = playerInventory.player.level().getBlockEntity(pos);
        if (!(be instanceof QuarryDiggerBlockEntity qbe))
            throw new IllegalStateException("Wrong BlockEntity!");
        this.blockEntity = qbe;
        this.inventory = qbe.inventory;

        // 1 слот для угля/charcoal
        this.addSlot(new SlotItemHandler(inventory, 0, 80, 34) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.COAL) || stack.is(Items.CHARCOAL);
            }
        });

        // Игровой инвентарь
        for (int row = 0; row < 3; ++row)
            for (int col = 0; col < 9; ++col)
                this.addSlot(new Slot(
                        playerInventory,
                        col + row * 9 + 9,
                        8 + col * 18,
                        84 + row * 18
                ));

        // Хотбар
        for (int col = 0; col < 9; ++col)
            this.addSlot(new Slot(
                    playerInventory,
                    col,
                    8 + col * 18,
                    142
            ));
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(
                access, player, QuarryDiggerMod.QUARRY_DIGGER_BLOCK.get()
        );
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        var slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            result = stackInSlot.copy();

            if (index == 0) {
                // из нашего слота в инвентарь
                if (!this.moveItemStackTo(stackInSlot, 1, this.slots.size(), true))
                    return ItemStack.EMPTY;
            } else if (stackInSlot.is(Items.COAL)
                    || stackInSlot.is(Items.CHARCOAL)) {
                // из инвентаря в наш слот
                if (!this.moveItemStackTo(stackInSlot, 0, 1, false))
                    return ItemStack.EMPTY;
            } else {
                // всё остальное
                if (!this.moveItemStackTo(stackInSlot, 1, this.slots.size(), false))
                    return ItemStack.EMPTY;
            }

            if (stackInSlot.isEmpty())
                slot.set(ItemStack.EMPTY);
            else
                slot.setChanged();

            blockEntity.setChanged();
        }
        return result;
    }
}
