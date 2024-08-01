package wile.rsgauges.libmc.detail;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class Overlay {
    @ExpectPlatform
    public static void register() {
        throw new AssertionError();
    }

    public static void show(Player player, final Component message) {
        show(player, message, 3000);
    }

    @ExpectPlatform
    public static void show(Player player, final Component message, int delay) {
        throw new AssertionError();
    }
}