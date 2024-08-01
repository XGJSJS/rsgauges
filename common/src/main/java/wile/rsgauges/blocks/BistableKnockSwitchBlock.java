/*
 * @file BistableKnockSwitchBlock.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2018 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Seismic mass based adjacent block "knock" detection activate.
 */
package wile.rsgauges.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import wile.rsgauges.detail.ModResources;
import wile.rsgauges.libmc.detail.INeighbourBlockInteractionSensitive;

public class BistableKnockSwitchBlock extends BistableSwitchBlock implements INeighbourBlockInteractionSensitive {
  public BistableKnockSwitchBlock(long config, BlockBehaviour.Properties properties, AABB unrotatedBBUnpowered, @Nullable AABB unrotatedBBPowered, @Nullable ModResources.BlockSoundEvent powerOnSound, @Nullable ModResources.BlockSoundEvent powerOffSound) {
    super(config, properties, unrotatedBBUnpowered, unrotatedBBPowered, powerOnSound, powerOffSound);
  }

  @Override
  public boolean isCube() {
    return true;
  }

  // -------------------------------------------------------------------------------------------------------------------

  @Override
  public boolean onNeighborBlockPlayerInteraction(Level world, BlockPos pos, BlockState state, BlockPos fromPos, LivingEntity entity, InteractionHand hand, boolean isLeftClick) {
    Direction facing = state.getValue(SwitchBlock.FACING);
    if (!pos.relative(facing).equals(fromPos))
      return false;
    onSwitchActivated(world, pos, state, null, facing);
    return false;
  }
}