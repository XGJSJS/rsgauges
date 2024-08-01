/*
 * @file ColorSupport.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2019 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Encapsulates colour handling for blocks and their item representations.
 */
package wile.rsgauges.libmc.detail;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import java.util.Arrays;
import java.util.Optional;

public final class ColorUtils {

  // -------------------------------------------------------------------------------------------------------------------
  // Dyes
  // -------------------------------------------------------------------------------------------------------------------

  public static class DyeColorProperty extends EnumProperty<DyeColor> {
    public DyeColorProperty(String name) {
      super(name, DyeColor.class, Arrays.asList(DyeColor.values()));
    }

    public static DyeColorProperty create(String name) {
      return new DyeColorProperty(name);
    }
  }

  public static Optional<DyeColor> getColorFromDyeItem(ItemStack stack) {
    final Item item = stack.getItem();
    if (item instanceof DyeItem dyeItem) return Optional.of(dyeItem.getDyeColor());
    // There must be a standard for that somewhere ...
    /*if(stack.is(ItemTags.DYES_BLACK)) return Optional.of(DyeColor.BLACK);
    if(stack.is(Tags.Items.DYES_RED)) return Optional.of(DyeColor.RED);
    if(stack.is(Tags.Items.DYES_GREEN)) return Optional.of(DyeColor.GREEN);
    if(stack.is(Tags.Items.DYES_BROWN)) return Optional.of(DyeColor.BROWN);
    if(stack.is(Tags.Items.DYES_BLUE)) return Optional.of(DyeColor.BLUE);
    if(stack.is(Tags.Items.DYES_PURPLE)) return Optional.of(DyeColor.PURPLE);
    if(stack.is(Tags.Items.DYES_CYAN)) return Optional.of(DyeColor.CYAN);
    if(stack.is(Tags.Items.DYES_LIGHT_GRAY)) return Optional.of(DyeColor.LIGHT_GRAY);
    if(stack.is(Tags.Items.DYES_GRAY)) return Optional.of(DyeColor.GRAY);
    if(stack.is(Tags.Items.DYES_PINK)) return Optional.of(DyeColor.PINK);
    if(stack.is(Tags.Items.DYES_LIME)) return Optional.of(DyeColor.LIME);
    if(stack.is(Tags.Items.DYES_YELLOW)) return Optional.of(DyeColor.YELLOW);
    if(stack.is(Tags.Items.DYES_LIGHT_BLUE)) return Optional.of(DyeColor.LIGHT_BLUE);
    if(stack.is(Tags.Items.DYES_MAGENTA)) return Optional.of(DyeColor.MAGENTA);
    if(stack.is(Tags.Items.DYES_ORANGE)) return Optional.of(DyeColor.ORANGE);
    if(stack.is(Tags.Items.DYES_WHITE)) return Optional.of(DyeColor.WHITE);*/
    return Optional.empty();
  }
}