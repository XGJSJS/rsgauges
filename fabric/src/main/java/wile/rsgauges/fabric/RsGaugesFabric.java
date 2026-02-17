package wile.rsgauges.fabric;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wile.rsgauges.RsGaugesMod;
import wile.rsgauges.fabric.config.ModConfigs;
import wile.rsgauges.fabric.libmc.detail.PlayerBlockInteraction;
import wile.rsgauges.libmc.detail.Auxiliaries;

public final class RsGaugesFabric implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void onInitialize() {
        RsGaugesMod.init();
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> PlayerBlockInteraction.onPlayerInteract(world, player, pos));
        ConfigHolder<ModConfigs> configsHolder = AutoConfig.register(ModConfigs.class, GsonConfigSerializer::new);
        ModConfigImpl.configs = configsHolder.get().toNBT();
        ModConfigImpl.apply();
        Auxiliaries.init(RsGaugesMod.MODID, LOGGER, () -> ModConfigImpl.configs);
    }
}