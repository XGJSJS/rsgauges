/*
 * @file Registries.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Common game registry handling.
 */
package wile.rsgauges.libmc.detail;

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
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.commons.lang3.tuple.Pair;
import wile.rsgauges.ModRsGauges;
import wile.rsgauges.detail.ModResources;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Registries {
  private static String modid = null;
  private static String creative_tab_icon = "";

  private static final DeferredRegister<Block> block_deferred_register = DeferredRegister.create(ForgeRegistries.BLOCKS, ModRsGauges.MODID);
  private static final DeferredRegister<Item> item_deferred_register = DeferredRegister.create(ForgeRegistries.ITEMS, ModRsGauges.MODID);
  private static final DeferredRegister<BlockEntityType<?>> block_entity_deferred_register = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ModRsGauges.MODID);
  private static final DeferredRegister<EntityType<?>> entity_deferred_register = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ModRsGauges.MODID);
  private static final DeferredRegister<MenuType<?>> menu_deferred_register = DeferredRegister.create(ForgeRegistries.MENU_TYPES, ModRsGauges.MODID);
  public static final DeferredRegister<SoundEvent> sound_deferred_register = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ModRsGauges.MODID);
  public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB = DeferredRegister.create(net.minecraft.core.registries.Registries.CREATIVE_MODE_TAB, ModRsGauges.MODID);

  private static final Map<String, RegistryObject<Block>> registered_blocks = new HashMap<>();
  private static final Map<String, RegistryObject<Item>> registered_items = new HashMap<>();
  private static final Map<String, RegistryObject<BlockEntityType<?>>> registered_block_entity_types = new HashMap<>();
  private static final Map<String, RegistryObject<EntityType<?>>> registered_entity_types = new HashMap<>();
  private static final Map<String, RegistryObject<MenuType<?>>> registered_menu_types = new HashMap<>();
  private static final Map<String, TagKey<Block>> registered_block_tag_keys = new HashMap<>();
  private static final ArrayList<Pair<Class<?>, RegistryObject<Block>>> registered_block_classes = new ArrayList<>();
  public static final List<Supplier<? extends ItemLike>> TAB_ITEMS = new LinkedList<>();

  public static final RegistryObject<CreativeModeTab> TAB = CREATIVE_MODE_TAB.register("industrial_small_lever", () -> CreativeModeTab.builder().title(Component.translatable("itemGroup.tabrsgauges")).icon(() -> new ItemStack(registered_items.get(creative_tab_icon).get())).build());

  public static void init(String mod_id, String creative_tab_icon_item_name)
  { modid = mod_id; creative_tab_icon=creative_tab_icon_item_name; }

  // -------------------------------------------------------------------------------------------------------------

  public static Block getBlock(String block_name) {
    return registered_blocks.get(block_name).get();
  }

  public static Item getItem(String name) {
    return registered_items.get(name).get();
  }

  public static EntityType<?> getEntityType(String name)
  { return registered_entity_types.get(name).get(); }

  public static BlockEntityType<?> getBlockEntityType(String block_name)
  { return registered_block_entity_types.get(block_name).get(); }

  public static MenuType<?> getMenuType(String name)
  { return registered_menu_types.get(name).get(); }

  // -------------------------------------------------------------------------------------------------------------

  @Nonnull
  public static List<Block> getRegisteredBlocks()
  { return registered_blocks.values().stream().map(RegistryObject::get).collect(Collectors.toList()); }

  @Nonnull
  public static List<Item> getRegisteredItems()
  { return registered_items.values().stream().map(RegistryObject::get).collect(Collectors.toList()); }

  // -------------------------------------------------------------------------------------------------------------

  public static <T extends Item> void addItem(String registry_name, Supplier<T> supplier)
  {
    RegistryObject<Item> item = item_deferred_register.register(registry_name, supplier);
    registered_items.put(registry_name, item);
  }

  public static <T extends Block> void addBlock(String registry_name, Supplier<T> block_supplier, Class<?> clazz)
  {
    RegistryObject<Block> block = block_deferred_register.register(registry_name, block_supplier);
    RegistryObject<Item> blockItem = item_deferred_register.register(registry_name,
            () -> new BlockItem(block.get(), (new Item.Properties())));
    TAB_ITEMS.add(blockItem);
    registered_blocks.put(registry_name, block);
    registered_items.put(registry_name, blockItem);
    registered_block_classes.add(Pair.of(clazz, block));
  }

  public static <T extends BlockEntity> void addBlockEntityType(String registry_name, BlockEntityType.BlockEntitySupplier<T> ctor, Class<? extends Block> block_clazz)
  {
    ArrayList<RegistryObject<Block>> blocks = new ArrayList<>();
    for (Pair<Class<?>, RegistryObject<Block>> block : registered_block_classes)
    {
      if (block_clazz.isAssignableFrom(block.getLeft())) blocks.add(block.getRight());
    }

    RegistryObject<BlockEntityType<?>> blockEntityType = block_entity_deferred_register.register(registry_name,
            () -> BlockEntityType.Builder.of(ctor, blocks.stream().map(RegistryObject::get).toList().toArray(new Block[]{})).build(null));
    registered_block_entity_types.put(registry_name, blockEntityType);
  }

  public static void addOptionalBlockTag(String tag_name, ResourceLocation... default_blocks)
  {
    final Set<Supplier<Block>> default_suppliers = new HashSet<>();
    for(ResourceLocation rl: default_blocks) default_suppliers.add(()->ForgeRegistries.BLOCKS.getValue(rl));
    final TagKey<Block> key = ForgeRegistries.BLOCKS.tags().createOptionalTagKey(new ResourceLocation(modid, tag_name), default_suppliers);
    registered_block_tag_keys.put(tag_name, key);
  }

  public static void addOptionalBlockTag(String tag_name, String... default_blocks)
  {
    addOptionalBlockTag(tag_name, Arrays.stream(default_blocks).map(ResourceLocation::new).toList().toArray(new ResourceLocation[]{}));
  }

  public static void registerAll(IEventBus eventBus) {
    ModResources.ALARM_SIREN_SOUND = ModResources.createSoundEvent("alarm_siren_sound");

    block_deferred_register.register(eventBus);
    item_deferred_register.register(eventBus);
    block_entity_deferred_register.register(eventBus);
    entity_deferred_register.register(eventBus);
    menu_deferred_register.register(eventBus);
    sound_deferred_register.register(eventBus);
    CREATIVE_MODE_TAB.register(eventBus);
  }
}