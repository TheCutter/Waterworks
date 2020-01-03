package org.terpo.waterworks.tileentity;

import org.terpo.waterworks.helper.FluidHelper;
import org.terpo.waterworks.helper.GeneralItemStackHandler;
import org.terpo.waterworks.init.WaterworksConfig;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

public class TileEntityRainTankWood extends TileWaterworks {

	protected FluidStack fluidResource = null;
	private static final int INVENTORY_SLOT_COUNT = 2;

	public TileEntityRainTankWood() {
		this(FluidHelper.getFluidResource(WaterworksConfig.rainCollection.woodenRainTankCollectedFluidName,
				WaterworksConfig.rainCollection.woodenRainTankFillrate),
				WaterworksConfig.rainCollection.woodenRainTankCapacity);
	}

	public TileEntityRainTankWood(FluidStack stack, int capacity) {
		super(INVENTORY_SLOT_COUNT, capacity);
		this.fluidResource = stack;

		this.fluidTank.setCanFill(false);
		this.fluidTank.setTileEntity(this);

		this.itemStackHandler = new GeneralItemStackHandler(this.inventorySize, this);

		this.itemStackHandler.setInputFlagForIndex(0, true);
		this.itemStackHandler.setOutputFlagForIndex(1, true);
	}

	@Override
	protected void updateServerSide() {

		if (fillFluid()) {
			this.isDirty = true;
		}

		if (needsUpdate(20) && isRefilling()) {
			this.isDirty = true;
		}
		super.updateServerSide();
	}

	@Override
	public boolean shouldRefresh(World worldIn, BlockPos posIn, IBlockState oldState, IBlockState newState) {
		return oldState.getBlock() != newState.getBlock();
	}

	public int getStateLevel() {
		return Math.round((this.fluidTank.getFluidAmount() * 4.0f / this.fluidTank.getCapacity()));
	}

	protected boolean isRefilling() {
		final BlockPos position = getPos().up();

		if (this.world.isRainingAt(position)) {
			this.fluidTank.fillInternal(this.fluidResource, true);
			return true;
		}
		return false;
	}
}
