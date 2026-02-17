package wile.rsgauges.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import wile.rsgauges.libmc.detail.Auxiliaries;

import java.util.List;

public abstract class RsItem extends Item {
  RsItem(Item.Properties properties) {
    super(properties);
  }

  @Override
  public void appendHoverText(ItemStack stack, TooltipContext tooltipContext, List<Component> tooltip, TooltipFlag tooltipFlag) {
    Auxiliaries.Tooltip.addInformation(stack, tooltip, true);
  }
}