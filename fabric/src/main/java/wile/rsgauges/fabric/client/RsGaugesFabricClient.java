package wile.rsgauges.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.renderer.RenderType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wile.rsgauges.RsGaugesMod;
import wile.rsgauges.libmc.detail.ModRegistries;
import wile.rsgauges.libmc.detail.fabric.OverlayImpl;

import java.util.ArrayList;
import java.util.List;

public final class RsGaugesFabricClient implements ClientModInitializer {
    public static final Logger LOGGER = LogManager.getLogger();
    private static final List<String> BLOCKS_CUTOUT = new ArrayList<>();

    @Override
    public void onInitializeClient() {
        RsGaugesMod.initClient();
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> OverlayImpl.TextOverlayGui.render(drawContext));
        for (String s : BLOCKS_CUTOUT) {
            try {
                BlockRenderLayerMap.INSTANCE.putBlock(ModRegistries.getBlock(s), RenderType.cutout());
            } catch (NullPointerException ignored) {
                LOGGER.warn("Block Not Found: {}", s);
            }
        }
    }

    static {
        BLOCKS_CUTOUT.add("industrial_analog_angular_gauge");
    }
}