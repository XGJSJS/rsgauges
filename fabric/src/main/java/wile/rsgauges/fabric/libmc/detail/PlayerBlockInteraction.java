package wile.rsgauges.fabric.libmc.detail;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import wile.rsgauges.libmc.detail.INeighbourBlockInteractionSensitive;

public class PlayerBlockInteraction {
    public static boolean onPlayerInteract(Level world, Player player, BlockPos fromPos) {
        if (world.isClientSide())
            return true;
        for (Direction facing: Direction.values()) {
            if (player.getDirection() == facing)
                continue;
            final BlockPos pos = fromPos.relative(facing);
            final BlockState state = world.getBlockState(pos);
            if (state.getBlock() instanceof INeighbourBlockInteractionSensitive blockInteractionSensitive) {
                if (blockInteractionSensitive.onNeighborBlockPlayerInteraction(world, pos, state, fromPos, player, InteractionHand.MAIN_HAND, true)) {
                    return false;
                }
            }
        }
        return true;
    }
}