package wile.rsgauges.fabric;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import wile.rsgauges.RsGaugesMod;
import wile.rsgauges.fabric.config.ModConfigs;
import wile.rsgauges.libmc.detail.Auxiliaries;
import wile.rsgauges.libmc.detail.ModRegistries;

import java.util.*;
import java.util.stream.Collectors;

public class ModConfigImpl {
    public static CompoundTag configs = new ModConfigs().toNBT();
    private static HashSet<String> optouts_ = new HashSet<>();
    public static final Set<ResourceLocation> accepted_wrenches = new HashSet<>(List.of(ResourceLocation.fromNamespaceAndPath("minecraft", "redstone_torch")));

    public static void apply() {
        final ArrayList<String> includes = new ArrayList<>();
        final ArrayList<String> excludes = new ArrayList<>();
        {
            String inc = configs.getString("pattern_includes").toLowerCase().replaceAll(RsGaugesMod.MODID+":", "").replaceAll("[^*_,a-z0-9]", "");
            if (!Objects.equals(configs.getString("pattern_includes"), inc))
                configs.putString("pattern_includes", inc);
            String[] incl = inc.split(",");
            for(int i=0; i< incl.length; ++i) {
                incl[i] = incl[i].replaceAll("[*]", ".*?");
                if(!incl[i].isEmpty()) includes.add(incl[i]);
            }
        }
        {
            String exc = configs.getString("pattern_excludes").toLowerCase().replaceAll(RsGaugesMod.MODID+":", "").replaceAll("[^*_,a-z0-9]", "");
            String[] excl = exc.split(",");
            for(int i=0; i< excl.length; ++i) {
                excl[i] = excl[i].replaceAll("[*]", ".*?");
                if(!excl[i].isEmpty()) excludes.add(excl[i]);
            }
        }
        if(!excludes.isEmpty())
            RsGaugesFabric.LOGGER.info("Config pattern excludes: '{}'", String.join(",", excludes));
        if(!includes.isEmpty())
            RsGaugesFabric.LOGGER.info("Config pattern includes: '{}'", String.join(",", includes));
        HashSet<String> optouts = new HashSet<>();
        ModRegistries.getRegisteredBlocks().stream().filter((Block block) -> {
            try {
                // Force-include/exclude pattern matching
                final String rn = BuiltInRegistries.BLOCK.getKey(block).getPath();
                try {
                    for(String e : includes) {
                        if(rn.matches(e)) {
                            return false;
                        }
                    }
                    for(String e : excludes) {
                        if(rn.matches(e)) {
                            return true;
                        }
                    }
                } catch(Throwable ex) {
                    Auxiliaries.logError("optout include pattern failed, disabling.");
                    includes.clear();
                    excludes.clear();
                }
            } catch(Exception ex) {
                Auxiliaries.logError("Exception evaluating the optout config: '"+ex.getMessage()+"'");
            }
            return false;
        }).forEach(
                e -> optouts.add(BuiltInRegistries.BLOCK.getKey(e).getPath())
        );
        optouts_ = optouts;
        // Wrenches
        {
            String cfg_wrenches = configs.getString("accepted_wrenches").toLowerCase().replaceAll("[\\s,]"," ").trim();
            List<ResourceLocation> wrenches = Arrays.stream(cfg_wrenches.split(" "))
                    .filter(e->!e.trim().isEmpty())
                    .map(ResourceLocation::tryParse)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            wrenches.add(ResourceLocation.fromNamespaceAndPath("minecraft", "redstone_torch"));
            wrenches.remove(ResourceLocation.fromNamespaceAndPath("minecraft", "air"));
            accepted_wrenches.clear();
            accepted_wrenches.addAll(wrenches);
        }
        RsGaugesFabric.LOGGER.info("Accepted wrenches: {}", accepted_wrenches.stream().map(ResourceLocation::toString).collect(Collectors.joining(",")));
    }

    public static boolean isWrench(final ItemStack stack) {
        return accepted_wrenches.contains(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    public static boolean without_switch_linking() {
        return configs.getBoolean("without_switch_linking");
    }

    public static boolean isOptedOut(final Block block) {
        return (block == null) || isOptedOut(block.asItem());
    }

    public static boolean isOptedOut(final Item item) {
        if (item == null)
            return false;
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
        return key == BuiltInRegistries.ITEM.getDefaultKey() || optouts_.contains(key.getPath());
    }

    public static int max_switch_linking_distance() {
        return configs.getInt("max_switch_linking_distance");
    }

    public static int gauge_update_interval() {
        return configs.getInt("gauge_update_interval");
    }

    public static boolean without_pulsetime_config() {
        return false;
    }

    public static boolean without_rightclick_item_switchconfig() {
        return false;
    }

    public static int config_left_click_timeout() {
        return 600;
    }

    public static boolean without_switch_nooutput() {
        return false;
    }

    public static int autoswitch_linear_update_interval() {
        return configs.getInt("autoswitch_linear_update_interval");
    }

    public static int autoswitch_volumetric_update_interval() {
        return configs.getInt("autoswitch_volumetric_update_interval");
    }

    public static int comparator_switch_update_interval() {
        return configs.getInt("comparator_switch_update_interval");
    }
}