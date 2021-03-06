package org.terpo.waterworks.block;

import org.terpo.waterworks.helper.FluidHelper;
import org.terpo.waterworks.helper.WaterworksInventoryHelper;
import org.terpo.waterworks.init.WaterworksBlocks;
import org.terpo.waterworks.init.WaterworksConfig;
import org.terpo.waterworks.init.WaterworksItems;
import org.terpo.waterworks.tileentity.TileEntityGroundwaterPump;
import org.terpo.waterworks.tileentity.TileWaterworks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class BlockGroundwaterPump extends BaseBlockTE<TileEntityGroundwaterPump> {
	private static final VoxelShape zAxisShape = VoxelShapes.create(new AxisAlignedBB(0, 0, .125, 1, 0.8125, .875));
	private static final VoxelShape xAxisShape = VoxelShapes.create(new AxisAlignedBB(.125, 0, 0, .875, 0.8125, 1));

	public BlockGroundwaterPump() {
		super();
		this.setDefaultState(getDefaultState().with(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
	}

	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand,
			BlockRayTraceResult facing) {

		if (!worldIn.isRemote && hand == Hand.MAIN_HAND) {// isRemote true = client
			final TileEntity tileEntity = getTileEntity(worldIn, pos);
			if (tileEntity instanceof TileEntityGroundwaterPump) {
				final ItemStack heldItem = playerIn.getHeldItem(hand);
				if (heldItem.getItem() == WaterworksItems.itemPipeWrench) {
					turnPumpModel(worldIn, pos, state);
					return true;
				}
				if (!heldItem.isEmpty() && !playerIn.isSneaking()
						&& tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null).isPresent()
						&& FluidHelper.interactWithFluidHandler(worldIn, pos, playerIn, hand, facing, tileEntity,
								heldItem)) {
					return true;
				}
				NetworkHooks.openGui((ServerPlayerEntity) playerIn, (INamedContainerProvider) tileEntity,
						tileEntity.getPos());
				return true;
			}
		}
		return true;
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return this.getDefaultState().with(BlockStateProperties.HORIZONTAL_FACING,
				context.getPlacementHorizontalFacing());
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new TileEntityGroundwaterPump();
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public boolean hasComparatorInputOverride(BlockState bs) {
		return true;
	}

	@Override
	public int getComparatorInputOverride(BlockState bs, World world, BlockPos pos) {
		final TileEntity te = getTileEntity(world, pos);
		if (te instanceof TileEntityGroundwaterPump) {
			return getTileEntity(world, pos).getComparatorOutput();
		}
		return 0;
	}

	@Override
	public void onBlockHarvested(World world, BlockPos pos, BlockState state, PlayerEntity player) {
		final TileEntity tileEntity = getTileEntity(world, pos);
		if (tileEntity instanceof TileWaterworks) {
			final LazyOptional<IItemHandler> capability = tileEntity
					.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
			capability.ifPresent(handler -> WaterworksInventoryHelper.dropItemsFromInventory(world, pos, handler));
		}
		BlockGroundwaterPump.breakPipes(world, pos);
		super.onBlockHarvested(world, pos, state, player);
	}

	private static void breakPipes(World world, BlockPos pos) {
		int y = pos.getY() - 1;
		int count = 0;
		while (y >= 0) {
			final BlockPos position = new BlockPos(pos.getX(), y, pos.getZ());
			final BlockState state = world.getBlockState(position);
			if (state.getBlock().equals(WaterworksBlocks.waterPipe)) {
				world.destroyBlock(position, false);
				count++;
				y--;
			} else {
				break;
			}
		}
		if (count > 0) {
			spawnAsEntity(world, pos, new ItemStack(WaterworksBlocks.waterPipe, count));
			if (WaterworksConfig.pump.getGroundwaterPumpSafety()) {
				world.setBlockState(pos.down(), Blocks.COBBLESTONE_SLAB.getDefaultState());
			}

		}

	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, // NOSONAR
			ISelectionContext context) {
		final Direction direction = state.get(BlockStateProperties.HORIZONTAL_FACING);
		return direction.getAxis() == Direction.Axis.X ? xAxisShape : zAxisShape;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) { // NOSONAR
		final Direction direction = state.get(BlockStateProperties.HORIZONTAL_FACING);
		return direction.getAxis() == Direction.Axis.X ? xAxisShape : zAxisShape;
	}

	// ModelTurning
	private static void turnPumpModel(World worldIn, BlockPos pos, BlockState state) {
		worldIn.setBlockState(pos, state.with(BlockStateProperties.HORIZONTAL_FACING,
				state.get(BlockStateProperties.HORIZONTAL_FACING).rotateY()), 2);
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(BlockStateProperties.HORIZONTAL_FACING);
	}

	@Override
	protected TileEntityGroundwaterPump getTileEntity(World world, BlockPos pos) {
		final TileEntity tE = world.getTileEntity(pos);
		if (tE instanceof TileEntityGroundwaterPump) {
			return (TileEntityGroundwaterPump) tE;
		}
		return null;
	}

}
