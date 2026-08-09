package wile.rsgauges.fabric.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import net.minecraft.nbt.CompoundTag;
import wile.rsgauges.RsGaugesMod;

@Config(name = RsGaugesMod.MODID)
public class ModConfigs implements ConfigData {
    @ConfigEntry.Gui.CollapsibleObject
    PatternExcludes pattern_excludes = new PatternExcludes();

    static class PatternExcludes {
        String pattern_excludes = "";
        String pattern_includes = "";
        boolean without_switch_linking = false;
    }

    @ConfigEntry.Gui.CollapsibleObject
    Miscellaneous miscellaneous = new Miscellaneous();

    static class Miscellaneous {
        boolean with_experimental = false;
        int max_switch_linking_distance = 48;
        String accepted_wrenches = "minecraft:redstone_torch,immersiveengineering:screwdriver,immersiveengineering:hammer,#c:tools/wrench";
        boolean with_config_logging = false;
    }

    @ConfigEntry.Gui.CollapsibleObject
    Tweaks tweaks = new Tweaks();

    static class Tweaks {
        boolean without_gauge_weak_power_measurement = false;
        int gauge_update_interval = 8;
        int autoswitch_volumetric_update_interval = 10;
        int autoswitch_linear_update_interval = 4;
        int comparator_switch_update_interval = 4;
    }

    private static ValidationException v(String message) {
        return new ValidationException(message);
    }

    private static void checkRange(String name, int value, int min, int max) throws ValidationException {
        if (value < min || value > max)
            throw v(String.format("%s must be in range [%s,%s]", name, min, max));
    }

    @Override
    public void validatePostLoad() throws ValidationException {
        checkRange("max_switch_linking_distance", miscellaneous.max_switch_linking_distance, 0, 64);
        checkRange("gauge_update_interval", tweaks.gauge_update_interval, 2, 100);
        checkRange("autoswitch_volumetric_update_interval", tweaks.autoswitch_volumetric_update_interval, 5, 50);
        checkRange("autoswitch_linear_update_interval", tweaks.autoswitch_linear_update_interval, 1, 50);
        checkRange("comparator_switch_update_interval", tweaks.comparator_switch_update_interval, 1, 50);
    }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("pattern_excludes", pattern_excludes.pattern_excludes);
        tag.putString("pattern_includes", pattern_excludes.pattern_includes);
        tag.putBoolean("without_switch_linking", pattern_excludes.without_switch_linking);
        tag.putBoolean("with_experimental", miscellaneous.with_experimental);
        tag.putInt("max_switch_linking_distance", miscellaneous.max_switch_linking_distance);
        tag.putString("accepted_wrenches", miscellaneous.accepted_wrenches);
        tag.putBoolean("with_config_logging", miscellaneous.with_config_logging);
        tag.putBoolean("without_gauge_weak_power_measurement", tweaks.without_gauge_weak_power_measurement);
        tag.putInt("gauge_update_interval", tweaks.gauge_update_interval);
        tag.putInt("autoswitch_volumetric_update_interval", tweaks.autoswitch_volumetric_update_interval);
        tag.putInt("autoswitch_linear_update_interval", tweaks.autoswitch_linear_update_interval);
        tag.putInt("comparator_switch_update_interval", tweaks.comparator_switch_update_interval);
        return tag;
    }
}