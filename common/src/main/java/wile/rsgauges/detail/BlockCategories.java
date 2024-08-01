/*
 * @file java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2018 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Block category wrapper matching.
 */
package wile.rsgauges.detail;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import wile.rsgauges.libmc.detail.Auxiliaries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static wile.rsgauges.RsGaugesMod.MODID;

public class BlockCategories {
  public static void update() {}

  public static Matcher getMatcher(String name) {
    return matchers_.getOrDefault(name, filter_none);
  }

  public static List<String> getMatcherNames() {
    return matcher_names_;
  }

  @FunctionalInterface
  public interface Matcher {
    boolean match(Level world, BlockPos pos);
  }

  private static final Matcher filter_none = (final Level w, final BlockPos p) -> false;
  private static final Map<String, Matcher> matchers_;
  private static final List<String> matcher_names_;

  static {
    // --------------------------------------------------------------------------------
    matchers_ = new HashMap<>();

    matchers_.put("any", (final Level w, final BlockPos p) -> !w.getBlockState(p).isAir());

    matchers_.put("solid", (final Level w, final BlockPos p) -> w.getBlockState(p).isSolid());

    matchers_.put("liquid", (final Level w, final BlockPos p) -> (w.getBlockState(p).liquid()) || (!w.getFluidState(p).isEmpty()));

    matchers_.put("air", (final Level w, final BlockPos p) -> w.getBlockState(p).isAir());

    matchers_.put("plant", (final Level w, final BlockPos p) -> {
      BlockState state = w.getBlockState(p);
      Block b = state.getBlock();
      return (b instanceof GrowingPlantBlock)|| Auxiliaries.isInBlockTag(state, new ResourceLocation(MODID, "plants"));
    });

    matchers_.put("material_wood", (final Level w, final BlockPos p) -> Auxiliaries.isInBlockTag(w.getBlockState(p), new ResourceLocation(MODID, "wooden")));

    matchers_.put("material_stone", (final Level w, final BlockPos p) -> Auxiliaries.isInBlockTag(w.getBlockState(p), new ResourceLocation(MODID, "stone_like")));

    matchers_.put("material_glass", (final Level w, final BlockPos p) -> Auxiliaries.isInBlockTag(w.getBlockState(p), new ResourceLocation(MODID, "glass_like")));

    matchers_.put("material_clay", (final Level w, final BlockPos p) -> Auxiliaries.isInBlockTag(w.getBlockState(p), new ResourceLocation(MODID, "clay_like")));

    matchers_.put("material_water", (final Level w, final BlockPos p) -> {
      BlockState st = w.getBlockState(p);
      if(Auxiliaries.isInBlockTag(st , new ResourceLocation(MODID, "water_like"))) return true;
      if(st.getFluidState().isEmpty()) return false;
      return (st.getFluidState().getType() == Fluids.WATER) || (st.getFluidState().getType() == Fluids.FLOWING_WATER);
    });

    matchers_.put("ore", (final Level w, final BlockPos p) -> Auxiliaries.isInBlockTag(w.getBlockState(p), new ResourceLocation(MODID, "ores")));

    matchers_.put("woodlog", (final Level w, final BlockPos p) -> Auxiliaries.isInBlockTag(w.getBlockState(p), new ResourceLocation(MODID, "logs")));

    matchers_.put("crop", (final Level w, final BlockPos p) -> {
      BlockState state = w.getBlockState(p);
      Block b = state.getBlock();
      return (b instanceof CropBlock) || Auxiliaries.isInBlockTag(state, new ResourceLocation(MODID, "crops"));
    });

    matchers_.put("crop_mature", (final Level w, final BlockPos p) -> {
      final BlockState s = w.getBlockState(p);
      final Block b = s.getBlock();
      return ((b instanceof CropBlock) && ((CropBlock)b).isMaxAge(s)) || (b== Blocks.MELON) || (b==Blocks.PUMPKIN);
    });

    matchers_.put("sapling", (final Level w, final BlockPos p) -> Auxiliaries.isInBlockTag(w.getBlockState(p), new ResourceLocation(MODID, "saplings")));

    matchers_.put("soil", (final Level w, final BlockPos p) -> Auxiliaries.isInBlockTag(w.getBlockState(p), new ResourceLocation(MODID, "soils")));

    matchers_.put("fertile", (final Level w, final BlockPos p) -> {
      boolean fertile = false;
      BlockState blockState = w.getBlockState(p);
      if (blockState.is(Blocks.FARMLAND))
        fertile = blockState.getValue(FarmBlock.MOISTURE) > 0;
      return fertile;
    });

    matchers_.put("planks", (final Level w, final BlockPos p) -> Auxiliaries.isInBlockTag(w.getBlockState(p), new ResourceLocation(MODID, "planks")));

    matchers_.put("slab", (final Level w, final BlockPos p) -> Auxiliaries.isInBlockTag(w.getBlockState(p), new ResourceLocation(MODID, "slabs")));

    // --------------------------------------------------------------------------------

    matcher_names_ = new ArrayList<>(); // use case sorted list
    matcher_names_.add("any");
    matcher_names_.add("solid");
    matcher_names_.add("liquid");
    matcher_names_.add("air");
    matcher_names_.add("plant");
    matcher_names_.add("material_wood");
    matcher_names_.add("material_stone");
    matcher_names_.add("material_glass");
    matcher_names_.add("material_clay");
    matcher_names_.add("material_water");
    matcher_names_.add("ore");
    matcher_names_.add("woodlog");
    matcher_names_.add("crop");
    matcher_names_.add("crop_mature");
    matcher_names_.add("sapling");
    matcher_names_.add("soil");
    matcher_names_.add("fertile");
    matcher_names_.add("planks");
    matcher_names_.add("slab");
    matchers_.forEach((k, v) -> {
      if (!matcher_names_.contains(k))
        matcher_names_.add(k);
    });
  }
}