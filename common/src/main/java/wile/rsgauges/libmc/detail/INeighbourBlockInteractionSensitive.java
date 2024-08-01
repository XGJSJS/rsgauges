package wile.rsgauges.libmc.detail;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface INeighbourBlockInteractionSensitive {
    default boolean onNeighborBlockPlayerInteraction(Level world, BlockPos pos, BlockState state, BlockPos fromPos, LivingEntity entity, InteractionHand hand, boolean isLeftClick) {
        return false;
    }
}