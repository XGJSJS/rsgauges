/*
 * @file SidedProxy.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * General client/server sideness selection proxy.
 */
package wile.rsgauges.libmc.detail;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.DistExecutor;

import javax.annotation.Nullable;

public class SidedProxy {
  @Nullable
  public static Player getPlayerClientSide()
  { return proxy.getPlayerClientSide(); }

  @Nullable
  public static Level getWorldClientSide()
  { return proxy.getWorldClientSide(); }

  @Nullable
  public static Minecraft mc()
  { return proxy.mc(); }

  // --------------------------------------------------------------------------------------------------------

  private static final ISidedProxy proxy = DistExecutor.unsafeRunForDist(()->ClientProxy::new, ()->ServerProxy::new);

  private interface ISidedProxy {
    default @Nullable Player getPlayerClientSide() { return null; }
    default @Nullable Level getWorldClientSide() { return null; }
    default @Nullable Minecraft mc() { return null; }
  }

  private static final class ClientProxy implements ISidedProxy {
    public @Nullable Player getPlayerClientSide() { return Minecraft.getInstance().player; }
    public @Nullable Level getWorldClientSide() { return Minecraft.getInstance().level; }
    public Minecraft mc() { return Minecraft.getInstance(); }
  }

  private static final class ServerProxy implements ISidedProxy { }
}