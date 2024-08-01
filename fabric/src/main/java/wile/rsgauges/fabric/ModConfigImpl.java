package wile.rsgauges.fabric;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import java.util.Objects;

//TODO: implement configs
/**
 * Now use default values
 */
public class ModConfigImpl {
    public static boolean isWrench(final ItemStack stack) {
        return Objects.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()), BuiltInRegistries.ITEM.getKey(Items.REDSTONE_TORCH));
    }

    public static boolean without_switch_linking() {
        return false;
    }

    public static boolean isOptedOut(final Block block) {
        return (block == null) || isOptedOut(block.asItem());
    }

    public static boolean isOptedOut(final Item item) {
        return false;
    }

    public static int max_switch_linking_distance() {
        return 48;
    }

    public static int gauge_update_interval() {
        return 8;
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
        return 4;
    }

    public static int autoswitch_volumetric_update_interval() {
        return 10;
    }

    public static int comparator_switch_update_interval() {
        return 4;
    }
}