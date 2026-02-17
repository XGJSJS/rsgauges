/*
 * @file OptionalRecipeCondition.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Recipe condition to enable opt'ing out JSON based recipes.
 */
package wile.rsgauges.neoforge.libmc.detail;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public record OptionalRecipeCondition(@Nullable ResourceLocation result, List<ResourceLocation> all_required,
                                      List<ResourceLocation> any_missing, List<ResourceLocation> all_required_tags,
                                      List<ResourceLocation> any_missing_tags, boolean experimental,
                                      boolean result_is_tag) implements ICondition {
  private static boolean with_experimental = false;
  private static boolean without_recipes = false;
  private static Predicate<Block> block_optouts = (block)->false;
  private static Predicate<Item> item_optouts = (item)->false;

  public static void on_config(boolean enable_experimental, boolean disable_all_recipes,
                               Predicate<Block> block_optout_provider,
                               Predicate<Item> item_optout_provider) {
    with_experimental = enable_experimental;
    without_recipes = disable_all_recipes;
    block_optouts = block_optout_provider;
    item_optouts = item_optout_provider;
  }

  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append("Optional recipe, all-required: [");
    for(ResourceLocation e:all_required) sb.append(e.toString()).append(",");
    for(ResourceLocation e:all_required_tags) sb.append("#").append(e.toString()).append(",");
    sb.delete(sb.length()-1, sb.length()).append("], any-missing: [");
    for(ResourceLocation e:any_missing) sb.append(e.toString()).append(",");
    for(ResourceLocation e:any_missing_tags) sb.append("#").append(e.toString()).append(",");
    sb.delete(sb.length()-1, sb.length()).append("]");
    if(experimental) sb.append(" EXPERIMENTAL");
    return sb.toString();
  }

  @Override
  public boolean test(@NotNull IContext context) {
    if(without_recipes) return false;
    if((experimental) && (!with_experimental)) return false;
    //final Collection<ResourceLocation> item_tags = SerializationTags.getInstance().getOrEmpty(Registry.ITEM_REGISTRY).getAvailableTags();
    if(result != null) {
      boolean item_registered = BuiltInRegistries.ITEM.containsKey(result);
      if(!item_registered) return false; // required result not registered
      if(item_optouts.test(BuiltInRegistries.ITEM.get(result))) return false;
      if(BuiltInRegistries.BLOCK.containsKey(result) && block_optouts.test(BuiltInRegistries.BLOCK.get(result))) return false;
    }
    if(!all_required.isEmpty()) {
      for(ResourceLocation rl:all_required) {
        if(!BuiltInRegistries.ITEM.containsKey(rl)) return false;
      }
    }
    if(!all_required_tags.isEmpty()) {
      for(ResourceLocation rl:all_required_tags) {
        if(BuiltInRegistries.ITEM.getTag(TagKey.create(Registries.ITEM, rl)).isEmpty()) return false;  // if(!item_tags.contains(rl)) return false;
      }
    }
    if(!any_missing.isEmpty()) {
      for(ResourceLocation rl:any_missing) {
        if(!BuiltInRegistries.ITEM.containsKey(rl)) return true;
      }
      return false;
    }
    if(!any_missing_tags.isEmpty()) {
      for(ResourceLocation rl:any_missing_tags) {
        if(BuiltInRegistries.ITEM.getTag(TagKey.create(Registries.ITEM, rl)).isEmpty()) return true; // if(!item_tags.contains(rl)) return true;
      }
      return false;
    }
    return true;
  }

  @Override
  public @NotNull MapCodec<? extends ICondition> codec() {
    return Serializer.CODEC;
  }

  public static class Serializer {
    public static final MapCodec<OptionalRecipeCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.list(Codec.STRING).fieldOf("required").orElse(new ArrayList<>()).forGetter(Serializer::encodeRequired),
            Codec.list(Codec.STRING).fieldOf("missing").orElse(new ArrayList<>()).forGetter(Serializer::encodeMissing),
            Codec.STRING.fieldOf("result").orElse(null).forGetter(Serializer::encodeResult),
            Codec.BOOL.fieldOf("experimental").orElse(false).forGetter(Serializer::encodeBoolean)).apply(instance, Serializer::decode));

    public static List<String> encodeRequired(OptionalRecipeCondition condition) {
      return condition.all_required.stream().map(ResourceLocation::toString).toList();
    }

    public static List<String> encodeMissing(OptionalRecipeCondition condition) {
      return condition.any_missing.stream().map(ResourceLocation::toString).toList();
    }

    public static String encodeResult(OptionalRecipeCondition condition) {
      return (condition.result != null) ? ((condition.result_is_tag ? "#" : "") + condition.result) : null;
    }

    public static boolean encodeBoolean(OptionalRecipeCondition condition) {
      return condition.experimental;
    }

    public static OptionalRecipeCondition decode(List<String> requiredString, List<String> missingString, @Nullable String resultString, boolean experimental) {
      List<ResourceLocation> required = new ArrayList<>();
      List<ResourceLocation> missing = new ArrayList<>();
      List<ResourceLocation> required_tags = new ArrayList<>();
      List<ResourceLocation> missing_tags = new ArrayList<>();
      ResourceLocation result = null;
      boolean result_is_tag = false;
      if (resultString != null) {
        if(resultString.startsWith("#")) {
          result = ResourceLocation.parse(resultString.substring(1));
          result_is_tag = true;
        } else {
          result = ResourceLocation.parse(resultString);
        }
      }
      if (!requiredString.isEmpty()) {
        for (String s : requiredString) {
          if(s.startsWith("#")) {
            required_tags.add(ResourceLocation.parse(s.substring(1)));
          } else {
            required.add(ResourceLocation.parse(s));
          }
        }
      }
      if (!missingString.isEmpty()) {
        for (String s : missingString) {
          if(s.startsWith("#")) {
            missing_tags.add(ResourceLocation.parse(s.substring(1)));
          } else {
            missing.add(ResourceLocation.parse(s));
          }
        }
      }
      return new OptionalRecipeCondition(result, required, missing, required_tags, missing_tags, experimental, result_is_tag);
    }
  }
}