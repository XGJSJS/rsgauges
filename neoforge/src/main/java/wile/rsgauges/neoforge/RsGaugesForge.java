/*
 * @file ModRsGauges.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2018 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Main mod class.
 */
package wile.rsgauges.neoforge;

import com.mojang.serialization.MapCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wile.rsgauges.RsGaugesMod;
import wile.rsgauges.libmc.detail.Auxiliaries;
import wile.rsgauges.neoforge.libmc.detail.OptionalRecipeCondition;
import wile.rsgauges.neoforge.libmc.detail.PlayerBlockInteraction;

import java.util.function.Supplier;

import static wile.rsgauges.RsGaugesMod.MODID;

@Mod(MODID)
public class RsGaugesForge {
  private static final Logger LOGGER = LogManager.getLogger();

  public static final DeferredRegister<MapCodec<? extends ICondition>> CONDITION_CODECS =
          DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, MODID);
  public static final Supplier<MapCodec<OptionalRecipeCondition>> OPTIONAL_CONDITION =
          CONDITION_CODECS.register("optional", () -> OptionalRecipeCondition.Serializer.CODEC);

  // -------------------------------------------------------------------------------------------------------------------

  public RsGaugesForge(IEventBus eventBus, ModContainer modContainer) {
    RsGaugesMod.init();
    Auxiliaries.init(MODID, LOGGER, ModConfigImpl::getServerConfig);
    modContainer.registerConfig(ModConfig.Type.COMMON, ModConfigImpl.COMMON_CONFIG_SPEC);
    eventBus.addListener(ForgeEvents::onClientSetup);
    PlayerBlockInteraction.init();
    CONDITION_CODECS.register(eventBus);
  }

  // -------------------------------------------------------------------------------------------------------------------
  // Events
  // -------------------------------------------------------------------------------------------------------------------

  @EventBusSubscriber
  public static final class ForgeEvents {
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