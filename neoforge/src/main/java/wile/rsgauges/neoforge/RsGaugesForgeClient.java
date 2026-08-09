package wile.rsgauges.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import wile.rsgauges.RsGaugesMod;

@Mod(value = RsGaugesMod.MODID, dist = Dist.CLIENT)
public class RsGaugesForgeClient {
    public RsGaugesForgeClient(ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}