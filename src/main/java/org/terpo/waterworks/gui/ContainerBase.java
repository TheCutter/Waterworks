package org.terpo.waterworks.gui;

import org.terpo.waterworks.inventory.FilteredFluidSlotItemHandler;
import org.terpo.waterworks.inventory.SlotDefinition;
import org.terpo.waterworks.tileentity.TileWaterworks;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerBase extends Container {

	private final TileWaterworks te;
	protected final IItemHandler itemHandler;
	public ContainerBase(IInventory playerInv, TileWaterworks te) {
		this.te = te;
		this.itemHandler = this.te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
		// both inventories. The two calls below make sure that slots are defined for both
		// inventories.
		addOwnSlots();
		addPlayerSlots(playerInv);
	}

	protected void addOwnSlots() {
		// Tile Entity, Slot 0-1, Slot IDs 0-1
		// 0 - Input
		// 1 - Output

		final SlotItemHandler input = new FilteredFluidSlotItemHandler(this.itemHandler, 0, 44, 35, SlotDefinition.I);
		final SlotItemHandler output = new FilteredFluidSlotItemHandler(this.itemHandler, 1, 116, 35, SlotDefinition.O);

		((FilteredFluidSlotItemHandler) input).addItemToFilter(Items.GLASS_BOTTLE);
		((FilteredFluidSlotItemHandler) input).addItemToFilter(Items.POTIONITEM);

		addSlotToContainer(input);
		addSlotToContainer(output);
	}

	private void addPlayerSlots(IInventory playerInv) {
		final int SLOTWIDTH = 18;
		// Slots for the main inventory
		for (int row = 0; row < 3; ++row) {
			for (int col = 0; col < 9; ++col) {
				final int x = 8 + col * SLOTWIDTH;
				final int y = row * SLOTWIDTH + 84;
				this.addSlotToContainer(new Slot(playerInv, col + row * 9 + 9, x, y));
			}
		}

		// Slots for the hotbar
		for (int row = 0; row < 9; ++row) {
			final int x = 8 + row * 18;
			final int y = 142;
			this.addSlotToContainer(new Slot(playerInv, row, x, y));
		}

	}

	// @Nullable
	@Override
	public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		final Slot slot = this.inventorySlots.get(index);

		if (slot != null && slot.getHasStack()) {
			final ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();
			final int invsize = this.te.getINVSIZE();
			if (index < invsize) {
				if (!this.mergeItemStack(itemstack1, invsize, this.inventorySlots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.mergeItemStack(itemstack1, 0, invsize, false)) {
				return ItemStack.EMPTY;
			}

			if (itemstack1.isEmpty()) {
				slot.putStack(ItemStack.EMPTY);
			} else {
				slot.onSlotChanged();
			}
		}

		return itemstack;
	}

	@Override
	public boolean canInteractWith(PlayerEntity playerIn) {
		return this.te.canInteractWith(playerIn);
	}
}
