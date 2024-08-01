package wile.rsgauges.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wile.rsgauges.libmc.detail.Auxiliaries;

import java.util.List;

public abstract class RsItem extends Item {
  RsItem(Item.Properties properties) {
    super(properties);
  }

  @Override
  public void appendHoverText(@NotNull ItemStack stack, @Nullable Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
    if (world != null && world.isClientSide())
      Auxiliaries.Tooltip.addInformation(stack, tooltip, true);
  }
}