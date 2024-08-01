/*
 * @file ModRsGauges.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2018 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Main mod class.
 */
package wile.rsgauges.forge;

import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wile.rsgauges.RsGaugesMod;
import wile.rsgauges.libmc.detail.Auxiliaries;
import wile.rsgauges.forge.libmc.detail.OptionalRecipeCondition;
import wile.rsgauges.forge.libmc.detail.PlayerBlockInteraction;

import static wile.rsgauges.RsGaugesMod.MODID;

@Mod(MODID)
public class RsGaugesForge {
  private static final Logger LOGGER = LogManager.getLogger();

  // -------------------------------------------------------------------------------------------------------------------

  public RsGaugesForge() {
    IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
    EventBuses.registerModEventBus(RsGaugesMod.MODID, eventBus);
    RsGaugesMod.init();
    Auxiliaries.init(MODID, LOGGER, ModConfigImpl::getServerConfig);
    OptionalRecipeCondition.init(MODID);
    ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, ModConfigImpl.COMMON_CONFIG_SPEC);
    eventBus.addListener(ForgeEvents::onSetup);
    eventBus.addListener(ForgeEvents::onClientSetup);
    MinecraftForge.EVENT_BUS.register(this);
    PlayerBlockInteraction.init();
  }

  // -------------------------------------------------------------------------------------------------------------------
  // Events
  // -------------------------------------------------------------------------------------------------------------------

  @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
  public static final class ForgeEvents {
    public static void onSetup(final FMLCommonSetupEvent event) {
      CraftingHelper.register(OptionalRecipeCondition.Serializer.INSTANCE);
    }

    public static void onClientSetup(final FMLClientSetupEvent event) {
      RsGaugesMod.initClient();
    }

    @SubscribeEvent
    public static void onConfigLoad(final ModConfigEvent.Loading event) {
      ModConfigImpl.apply();
    }

    @SubscribeEvent
    public static void onConfigReload(final ModConfigEvent.Reloading event) {
      try {
        Auxiliaries.logger().info("Config file changed {}", event.getConfig().getFileName());
        ModConfigImpl.apply();
      } catch (Throwable e) {
          Auxiliaries.logger().error("Failed to load changed config: {}", e.getMessage());
      }
    }
  }
}