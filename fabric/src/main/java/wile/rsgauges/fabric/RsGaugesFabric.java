package wile.rsgauges.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.nbt.CompoundTag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wile.rsgauges.RsGaugesMod;
import wile.rsgauges.fabric.libmc.detail.PlayerBlockInteraction;
import wile.rsgauges.libmc.detail.Auxiliaries;

public final class RsGaugesFabric implements ModInitializer {
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void onInitialize() {
        RsGaugesMod.init();
        Auxiliaries.init(RsGaugesMod.MODID, LOGGER, CompoundTag::new);
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> PlayerBlockInteraction.onPlayerInteract(world, player, pos));
    }
}