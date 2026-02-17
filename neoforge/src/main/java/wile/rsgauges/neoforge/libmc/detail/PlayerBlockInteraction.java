/*
 * @file PlayerBlockInteraction.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Encapsulates Player interaction events with blocks
 */
package wile.rsgauges.neoforge.libmc.detail;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import wile.rsgauges.libmc.detail.INeighbourBlockInteractionSensitive;

public class PlayerBlockInteraction {
  public static void init() {
    NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,false, PlayerInteractEvent.LeftClickBlock.class, PlayerBlockInteraction::onPlayerInteractLeft);
    NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, PlayerInteractEvent.RightClickBlock.class, PlayerBlockInteraction::onPlayerInteractRight);
  }

  public static void onPlayerInteractLeft(PlayerInteractEvent.LeftClickBlock event) {
    final Level world = event.getLevel();
    if(world.isClientSide()) return;
    if(!((event.getHand() == InteractionHand.MAIN_HAND) && (event.getFace() != Direction.DOWN)))
      return;
    final BlockPos fromPos = event.getPos();
    for(Direction facing: Direction.values()) {
      if(event.getFace() == facing) continue;
      final BlockPos pos = fromPos.relative(facing);
      final BlockState state = event.getLevel().getBlockState(pos);
      if(!((state.getBlock()) instanceof INeighbourBlockInteractionSensitive)) continue;
      if(((INeighbourBlockInteractionSensitive)state.getBlock()).onNeighborBlockPlayerInteraction(world, pos, state, fromPos, event.getEntity(), event.getHand())) {
        event.setCanceled(InteractionResult.CONSUME.consumesAction());
      }
    }
  }

  public static void onPlayerInteractRight(PlayerInteractEvent.RightClickBlock event) {
    final Level world = event.getLevel();
    if(world.isClientSide()) return;
    if (event.getHand() != InteractionHand.MAIN_HAND)
      return;
    final BlockPos fromPos = event.getPos();
    for(Direction facing: Direction.values()) {
      if(event.getFace() == facing) continue;
      final BlockPos pos = fromPos.relative(facing);
      final BlockState state = event.getLevel().getBlockState(pos);
      if(!((state.getBlock()) instanceof INeighbourBlockInteractionSensitive)) continue;
      if(((INeighbourBlockInteractionSensitive)state.getBlock()).onNeighborBlockPlayerInteraction(world, pos, state, fromPos, event.getEntity(), event.getHand())) {
        ((ICancellableEvent) event).setCanceled(InteractionResult.CONSUME.consumesAction());
      }
    }
  }
}