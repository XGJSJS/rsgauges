package wile.rsgauges;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class ModConfig {
    @ExpectPlatform
    public static boolean isWrench(final ItemStack stack) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean without_switch_linking() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isOptedOut(final Block block) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isOptedOut(final Item item) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static int max_switch_linking_distance() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static int gauge_update_interval() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean without_pulsetime_config() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean without_rightclick_item_switchconfig() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static int config_left_click_timeout() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean without_switch_nooutput() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static int autoswitch_linear_update_interval() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static int autoswitch_volumetric_update_interval() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static int comparator_switch_update_interval() {
        throw new AssertionError();
    }
}