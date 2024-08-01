/*
 * @file Overlay.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Renders status messages in one line.
 */
package wile.rsgauges.libmc.detail.forge;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import wile.rsgauges.libmc.detail.Networking;
import wile.rsgauges.libmc.detail.SidedProxy;

public class OverlayImpl {
  public static void register() {
    if (SidedProxy.mc() != null) {
      MinecraftForge.EVENT_BUS.register(new TextOverlayGui());
      Networking.OverlayTextMessage.setHandler(TextOverlayGui::show);
    }
  }

  public static void show(Player player, final Component message, int delay)
  { Networking.OverlayTextMessage.sendToPlayer(player, message, delay); }

  // -----------------------------------------------------------------------------
  // Client side handler
  // -----------------------------------------------------------------------------

  @Mod.EventBusSubscriber(value = Dist.CLIENT)
  @OnlyIn(Dist.CLIENT)
  public static class TextOverlayGui extends Screen {
    private static final Component EMPTY_TEXT = Component.empty();
    private static final double overlay_y_ = 0.75;
    private final Minecraft mc;
    private static long deadline_;
    private static Component text_;

    public static synchronized Component text()
    { return text_; }

    public static synchronized long deadline()
    { return deadline_; }

    public static synchronized void show(Component s, int displayTimeoutMs) {
      text_ = (s==null) ? (EMPTY_TEXT) : (s.copy());
      deadline_ = System.currentTimeMillis() + displayTimeoutMs;
    }

    public static synchronized void show(String s, int displayTimeoutMs) {
      text_ = ((s==null)||(s.isEmpty())) ? (EMPTY_TEXT) : (Component.literal(s));
      deadline_ = System.currentTimeMillis() + displayTimeoutMs;
    }

    TextOverlayGui() {
      super(EMPTY_TEXT);
      mc = SidedProxy.mc();
    }

    @SubscribeEvent
    public void render(RenderGuiOverlayEvent.Post event) {
      if (deadline() < System.currentTimeMillis() || text() == EMPTY_TEXT)
        return;
      String txt = text().getString();
      if (txt.isEmpty())
        return;
      GuiGraphics guiGraphics = event.getGuiGraphics();
      final Window win = mc.getWindow();
      final Font fr = mc.font;
      final int cx = win.getGuiScaledWidth() / 2;
      final int cy = (int)(win.getGuiScaledHeight() * overlay_y_);
      final int w = fr.width(txt);
      final int h = fr.lineHeight;
      guiGraphics.fillGradient(cx-(w/2)-3, cy-2, cx+(w/2)+2, cy+h+2, 0xaa333333, 0xaa444444);
      guiGraphics.hLine(cx-(w/2)-3, cx+(w/2)+2, cy-2, 0xaa333333);
      guiGraphics.hLine(cx-(w/2)-3, cx+(w/2)+2, cy+h+2, 0xaa333333);
      guiGraphics.vLine(cx-(w/2)-3, cy-2, cy+h+2, 0xaa333333);
      guiGraphics.vLine(cx+(w/2)+2, cy-2, cy+h+2, 0xaa333333);
      guiGraphics.drawCenteredString(fr, text(), cx , cy+1, 0x00ffaa00);
    }
  }
}