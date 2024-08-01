/*
 * @file JEIPlugin.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * JEI plugin (see https://github.com/mezz/JustEnoughItems/wiki/Creating-Plugins)
 */
package wile.rsgauges.eapi.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import wile.rsgauges.ModConfig;
import wile.rsgauges.RsGaugesMod;
import wile.rsgauges.libmc.detail.Auxiliaries;
import wile.rsgauges.libmc.detail.ModRegistries;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@JeiPlugin
public class ModJEIPlugin implements IModPlugin {
  @Override
  public @NotNull ResourceLocation getPluginUid() {
    return new ResourceLocation(RsGaugesMod.MODID, "jei_plugin_uid");
  }

  @Override
  public void onRuntimeAvailable(@NotNull IJeiRuntime jeiRuntime) {
    HashSet<Item> blacklisted = new HashSet<>();
    for (Block e : ModRegistries.getRegisteredBlocks()) {
      if (ModConfig.isOptedOut(e) && (BuiltInRegistries.ITEM.getKey(e.asItem()).getPath()).equals((BuiltInRegistries.BLOCK.getKey(e).getPath()))) {
        blacklisted.add(e.asItem());
      }
    }
    for (Item e : ModRegistries.getRegisteredItems()) {
      if (ModConfig.isOptedOut(e) && (!(e instanceof BlockItem))) {
        blacklisted.add(e);
      }
    }
    if (!blacklisted.isEmpty()) {
      List<ItemStack> blacklist = blacklisted.stream().map(ItemStack::new).collect(Collectors.toList());
      try {
        jeiRuntime.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, blacklist);
      } catch (Exception e) {
          Auxiliaries.logger().warn("Exception in JEI opt-out processing: '{}', skipping further JEI optout processing.", e.getMessage());
      }
    }
  }
}