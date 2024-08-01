/*
 * @file RsBlock.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2018 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Base class for blocks used in this mod. It sets the
 * defaults for block properties, registration categories,
 * and provides default overloads for further blocks.
 * As rsgauges blocks work directional (placed with a
 * defined facing), the basics for `facing` dependent
 * data are implemented here, too.
 */
package wile.rsgauges.blocks;

import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wile.rsgauges.libmc.detail.Auxiliaries;
import wile.rsgauges.libmc.detail.Networking;

import java.util.Collections;
import java.util.List;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;

public abstract class RsBlock extends Block implements EntityBlock {
  public static final long RSBLOCK_CONFIG_CUTOUT             = 0x1000000000000000L;
  public static final long RSBLOCK_CONFIG_TRANSLUCENT        = 0x3000000000000000L;
  public static final long RSBLOCK_NOT_WATERLOGGABLE         = 0x0008000000000000L;

  public final long config;

  // -------------------------------------------------------------------------------------------------------------------

  public RsBlock(long config, BlockBehaviour.Properties properties)
  { this(config, properties, Auxiliaries.getPixeledAABB(0, 0, 0, 16, 16,16 )); }

  public RsBlock(long config, BlockBehaviour.Properties properties, final AABB aabb)
  { this(config, properties, Shapes.create(aabb)); }

  public RsBlock(long config, BlockBehaviour.Properties properties, final VoxelShape vshape)
  { super(properties); this.config = config; registerDefaultState(this.getStateDefinition().any().setValue(WATERLOGGED, false)); }

  @Override
  @Nullable
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, @NotNull BlockState state, @NotNull BlockEntityType<T> te_type)
  { return (world.isClientSide) ? (null) : ((Level w, BlockPos p, BlockState s, T te) -> ((RsTileEntity)te).tick()); } // To be evaluated if

  @Override
  @Nullable
  public <T extends BlockEntity> GameEventListener getListener(@NotNull ServerLevel p_221121_, @NotNull T te)
  { return null; }

  // -------------------------------------------------------------------------------------------------------------------
  // Block overrides
  // -------------------------------------------------------------------------------------------------------------------

  @Override
  public void appendHoverText(final @NotNull ItemStack stack, @Nullable BlockGetter world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
    if (Platform.getEnv() == EnvType.CLIENT)
      Auxiliaries.Tooltip.addInformation(stack, tooltip, true);
  }

  @Override
  @SuppressWarnings("deprecation")
  public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter source, @NotNull BlockPos pos, @NotNull CollisionContext selectionContext)
  { return Shapes.block(); }

  @Override
  @SuppressWarnings("deprecation")
  public @NotNull VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext selectionContext)
  { return getShape(state, world, pos, selectionContext); }

  public boolean isValidSpawn(BlockState state, BlockGetter world, BlockPos pos, SpawnPlacements.Type type, @Nullable EntityType<?> entityType)
  { return false; }

  public PushReaction getPistonPushReaction(BlockState state)
  { return PushReaction.DESTROY; }

  @Override
  protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder)
  { super.createBlockStateDefinition(builder); builder.add(WATERLOGGED); }

  @Override
  @Nullable
  public BlockState getStateForPlacement(@NotNull BlockPlaceContext context)
  {
    BlockState state = super.getStateForPlacement(context);
    if(state==null) return null;
    if((config & RSBLOCK_NOT_WATERLOGGABLE)==0) {
      FluidState fs = context.getLevel().getFluidState(context.getClickedPos());
      state = state.setValue(WATERLOGGED,fs.getType()== Fluids.WATER);
    } else {
      state = state.setValue(WATERLOGGED, false);
    }
    return state;
  }

  @Override
  @SuppressWarnings("deprecation")
  public @NotNull FluidState getFluidState(@NotNull BlockState state)
  { return ((config & RSBLOCK_NOT_WATERLOGGABLE)==0) ? (state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state)) : super.getFluidState(state); }

  @Override
  public boolean propagatesSkylightDown(@NotNull BlockState state, @NotNull BlockGetter reader, @NotNull BlockPos pos)
  {
    if(((config & RSBLOCK_NOT_WATERLOGGABLE)==0) && state.getValue(WATERLOGGED)) return false;
    return super.propagatesSkylightDown(state, reader, pos);
  }

  @Override
  @SuppressWarnings("deprecation")
  public void onRemove(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull BlockState newState, boolean isMoving)
  {
    super.onRemove(state, world, pos, newState, isMoving);
    world.updateNeighbourForOutputSignal(pos, newState.getBlock());
    world.updateNeighborsAt(pos, newState.getBlock());
  }

  @Override
  @SuppressWarnings("deprecation")
  public @NotNull BlockState updateShape(@NotNull BlockState state, @NotNull Direction facing, @NotNull BlockState facingState, @NotNull LevelAccessor world, @NotNull BlockPos pos, @NotNull BlockPos facingPos)
  {
    if((config & RSBLOCK_NOT_WATERLOGGABLE)==0) {
      if(state.getValue(WATERLOGGED)) world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
    }
    return state;
  }

  @Override
  @SuppressWarnings("deprecation")
  public void neighborChanged(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull Block block, @NotNull BlockPos fromPos, boolean isMoving)
  {}

  @Override
  public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
    return Collections.singletonList(new ItemStack(state.getBlock().asItem()));
  }

  @Override
  @SuppressWarnings("deprecation")
  public void attack(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull Player player)
  {}

  @Override
  @SuppressWarnings("deprecation")
  public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit)
  { return InteractionResult.PASS; }

  @Override
  @SuppressWarnings("deprecation")
  public void tick(@NotNull BlockState state, @NotNull ServerLevel world, @NotNull BlockPos pos, @NotNull RandomSource rnd)
  {}

  // -------------------------------------------------------------------------------------------------------------------
  // Basic tile entity
  // -------------------------------------------------------------------------------------------------------------------

  /**
   * Main RsBlock derivate tile entity base
   */
  public static abstract class RsTileEntity extends BlockEntity implements Networking.IPacketTileNotifyReceiver {

    public RsTileEntity(BlockEntityType<?> te_type, BlockPos pos, BlockState state)
    { super(te_type, pos, state); }

    public void write(CompoundTag nbt, boolean updatePacket) {}

    public void read(CompoundTag nbt, boolean updatePacket) {}

    public void tick() {}

    public final void onServerPacketReceived(CompoundTag nbt) {
      read(nbt, true);
    }

    // --------------------------------------------------------------------------------------------------------
    // BlockEntity
    // --------------------------------------------------------------------------------------------------------

    @Override
    public final void saveAdditional(@NotNull CompoundTag nbt) {
      super.saveAdditional(nbt); write(nbt, false);
    }

    @Override
    public final void load(@NotNull CompoundTag nbt)
    {
      super.load(nbt); read(nbt, false);
    }
  }
}