package wile.rsgauges;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import wile.rsgauges.detail.BlockCategories;
import wile.rsgauges.libmc.detail.Auxiliaries;
import wile.rsgauges.libmc.detail.ModRegistries;
import wile.rsgauges.libmc.detail.Networking;
import wile.rsgauges.libmc.detail.Overlay;

public final class RsGaugesMod {
    public static final String MODID = "rsgauges";
    public static final String MODNAME = "Gauges and Switches";

    public static void init() {
        Auxiliaries.logGitVersion(MODNAME);
        ModRegistries.init();
        ModContent.init();
        Networking.init(MODID);
        Overlay.register();
        BlockCategories.update();
    }

    @Environment(EnvType.CLIENT)
    public static void initClient() {
        Networking.initClient(MODID);
    }
}