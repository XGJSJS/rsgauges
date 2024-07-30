/*
 * @file SensitiveGlassBlock.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2018 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Class representing full, transparent blocks with different
 * look depending on the redstone power they receive.
 *
 * Light handling see Mojang Redstone Lamp.
 */
package wile.rsgauges.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import wile.rsgauges.libmc.detail.Auxiliaries;
import wile.rsgauges.libmc.detail.ColorUtils;

import javax.annotation.Nullable;
import java.util.Optional;

public class SensitiveGlassBlock extends RsBlock
{
  public static final BooleanProperty POWERED = BooleanProperty.create("powered");
  public static final ColorUtils.DyeColorProperty COLOR = ColorUtils.DyeColorProperty.create("color");

  // -------------------------------------------------------------------------------------------------------------------

  public SensitiveGlassBlock(BlockBehaviour.Properties properties)
  {
    super(RSBLOCK_CONFIG_TRANSLUCENT, properties, Auxiliaries.getPixeledAABB(0, 0, 0, 16, 16,16 ));
    registerDefaultState(super.defaultBlockState().setValue(POWERED, false).setValue(COLOR, DyeColor.WHITE));
  }

  @Override
  @Nullable
  public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state)
  { return null; }

  // -------------------------------------------------------------------------------------------------------------------
  // Block overrides
  // -------------------------------------------------------------------------------------------------------------------

  // Light reduction
  @OnlyIn(Dist.CLIENT)
  @SuppressWarnings("deprecation")
  public float getShadeBrightness(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos)
  { return 0.95f; }

  @Override
  @OnlyIn(Dist.CLIENT)
  @SuppressWarnings("deprecation")
  public boolean skipRendering(@NotNull BlockState state, BlockState adjacentBlockState, @NotNull Direction side)
  {
    if((!(adjacentBlockState.getBlock() instanceof SensitiveGlassBlock))) return false;
    return (adjacentBlockState.getValue(POWERED) == state.getValue(POWERED));
  }

  @Override
  public boolean isPossibleToRespawnInThis(@NotNull BlockState state) {
    return false;
  }

  @Override
  public boolean shouldCheckWeakPower(BlockState state, SignalGetter level, BlockPos pos, Direction side) {
    return false;
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder)
  { super.createBlockStateDefinition(builder); builder.add(POWERED, COLOR); }

  @Override
  public void tick(@NotNull BlockState state, ServerLevel world, @NotNull BlockPos pos, @NotNull RandomSource random)
  {
    if(world.isClientSide()) return;
    if(state.getValue(POWERED) && (!(world.hasNeighborSignal(pos)))) {
      world.setBlock(pos, state.setValue(POWERED, false), 1|2|8|16);
    }
  }

  @Override
  public boolean propagatesSkylightDown(@NotNull BlockState state, @NotNull BlockGetter reader, @NotNull BlockPos pos)
  { return true; }

  @Override
  public BlockState getStateForPlacement(@NotNull BlockPlaceContext context)
  {
    final BlockState state = super.getStateForPlacement(context);
    return (state==null) ? (null) : (state.setValue(POWERED, context.getLevel().hasNeighborSignal(context.getClickedPos())));
  }

  @Override
  public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit)
  {
    final ItemStack stack = player.getItemInHand(hand);
    Optional<DyeColor> dye = ColorUtils.getColorFromDyeItem(stack);
    if(dye.isEmpty()) return InteractionResult.PASS;
    world.setBlock(pos, state.setValue(COLOR, dye.get()), 1|2);
    return world.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
  }

  @Override
  public void neighborChanged(@NotNull BlockState state, Level world, @NotNull BlockPos pos, @NotNull Block block, @NotNull BlockPos fromPos, boolean isMoving)
  {
    if(world.isClientSide()) return;
    final boolean was_powered = state.getValue(POWERED);
    final boolean powered = world.hasNeighborSignal(pos);
    if(was_powered == powered) return;
    if (powered) {
      world.setBlock(pos, state.setValue(POWERED, true), 1|2);
    } else {
      world.scheduleTick(pos, this, 4);
    }
  }
}