package wile.rsgauges.fabric.integration;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfig;
import wile.rsgauges.fabric.config.ModConfigs;

public class RsGaugesModMenuIntegration implements ModMenuApi {
    @Override
    @SuppressWarnings("all")
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            return AutoConfig.getConfigScreen(ModConfigs.class, parent).get();
        };
    }
}