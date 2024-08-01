/*
 * @file Registries.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Common game registry handling.
 */
package wile.rsgauges.libmc.detail;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import wile.rsgauges.RsGaugesMod;
import wile.rsgauges.detail.ModResources;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ModRegistries {
  private static final String creative_tab_icon = "industrial_small_lever";

  private static final DeferredRegister<Block> block_deferred_register = DeferredRegister.create(RsGaugesMod.MODID, Registries.BLOCK);
  private static final DeferredRegister<Item> item_deferred_register = DeferredRegister.create(RsGaugesMod.MODID, Registries.ITEM);
  private static final DeferredRegister<BlockEntityType<?>> block_entity_deferred_register = DeferredRegister.create(RsGaugesMod.MODID, Registries.BLOCK_ENTITY_TYPE);
  private static final DeferredRegister<EntityType<?>> entity_deferred_register = DeferredRegister.create(RsGaugesMod.MODID, Registries.ENTITY_TYPE);
  private static final DeferredRegister<MenuType<?>> menu_deferred_register = DeferredRegister.create(RsGaugesMod.MODID, Registries.MENU);
  public static final DeferredRegister<SoundEvent> sound_deferred_register = DeferredRegister.create(RsGaugesMod.MODID, Registries.SOUND_EVENT);
  public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB = DeferredRegister.create(RsGaugesMod.MODID, Registries.CREATIVE_MODE_TAB);

  private static final Map<String, RegistrySupplier<Block>> registered_blocks = new HashMap<>();
  private static final Map<String, RegistrySupplier<Item>> registered_items = new HashMap<>();
  private static final Map<String, RegistrySupplier<BlockEntityType<?>>> registered_block_entity_types = new HashMap<>();
  private static final Map<String, TagKey<Block>> registered_block_tag_keys = new HashMap<>();
  private static final ArrayList<Pair<Class<?>, RegistrySupplier<Block>>> registered_block_classes = new ArrayList<>();

  public static final RegistrySupplier<CreativeModeTab> TAB = CREATIVE_MODE_TAB.register("rsgauges", () -> CreativeTabRegistry.create(Component.translatable("itemGroup.tabrsgauges"), () -> new ItemStack(registered_items.get(creative_tab_icon).get())));

  public static void init() {}

  // -------------------------------------------------------------------------------------------------------------

  public static Block getBlock(String block_name) {
    return registered_blocks.get(block_name).get();
  }

  public static Item getItem(String name) {
    return registered_items.get(name).get();
  }

  public static BlockEntityType<?> getBlockEntityType(String block_name) {
    return registered_block_entity_types.get(block_name).get();
  }

  public static TagKey<Block> getBlockTagKey(String name) {
    return registered_block_tag_keys.get(name);
  }

  // -------------------------------------------------------------------------------------------------------------

  @NotNull
  public static List<Block> getRegisteredBlocks() {
    return registered_blocks.values().stream().map(RegistrySupplier::get).collect(Collectors.toList());
  }

  @NotNull
  public static List<Item> getRegisteredItems() {
    return registered_items.values().stream().map(RegistrySupplier::get).collect(Collectors.toList());
  }

  // -------------------------------------------------------------------------------------------------------------

  public static <T extends Item> void addItem(String registry_name, Supplier<T> supplier) {
    RegistrySupplier<Item> item = item_deferred_register.register(registry_name, supplier);
    registered_items.put(registry_name, item);
  }

  public static <T extends Block> void addBlock(String registry_name, Supplier<T> block_supplier, Class<?> clazz) {
    RegistrySupplier<Block> block = block_deferred_register.register(registry_name, block_supplier);
    RegistrySupplier<Item> blockItem = item_deferred_register.register(registry_name,
            () -> new BlockItem(block.get(), (new Item.Properties().arch$tab(ModRegistries.TAB))));
    registered_blocks.put(registry_name, block);
    registered_items.put(registry_name, blockItem);
    registered_block_classes.add(Pair.of(clazz, block));
  }

  public static <T extends BlockEntity> void addBlockEntityType(String registry_name, BlockEntityType.BlockEntitySupplier<T> ctor, Class<? extends Block> block_clazz) {
    ArrayList<RegistrySupplier<Block>> blocks = new ArrayList<>();
    for (Pair<Class<?>, RegistrySupplier<Block>> block : registered_block_classes) {
      if (block_clazz.isAssignableFrom(block.getLeft()))
        blocks.add(block.getRight());
    }

    RegistrySupplier<BlockEntityType<?>> blockEntityType = block_entity_deferred_register.register(registry_name,
            () -> BlockEntityType.Builder.of(ctor, blocks.stream().map(RegistrySupplier::get).toList().toArray(new Block[]{})).build(null));
    registered_block_entity_types.put(registry_name, blockEntityType);
  }

  public static void addOptionalBlockTag(String tag_name, ResourceLocation... default_blocks) {
    final Set<Supplier<Block>> default_suppliers = new HashSet<>();
    for (ResourceLocation rl: default_blocks)
      default_suppliers.add(() -> BuiltInRegistries.BLOCK.get(rl));
    final TagKey<Block> key = TagKey.create(Registries.BLOCK, new ResourceLocation(RsGaugesMod.MODID, tag_name));
    registered_block_tag_keys.put(tag_name, key);
  }

  public static void addOptionalBlockTag(String tag_name, String... default_blocks) {
    addOptionalBlockTag(tag_name, Arrays.stream(default_blocks).map(ResourceLocation::new).toList().toArray(new ResourceLocation[]{}));
  }

  public static void registerAll() {
    ModResources.ALARM_SIREN_SOUND = ModResources.createSoundEvent("alarm_siren_sound");

    sound_deferred_register.register();
    block_deferred_register.register();
    item_deferred_register.register();
    block_entity_deferred_register.register();
    entity_deferred_register.register();
    menu_deferred_register.register();
    CREATIVE_MODE_TAB.register();
  }
}