/*
 * @file Overlay.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Renders status messages in one line.
 */
package wile.rsgauges.libmc.detail;

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


public class Overlay {
  public static void register() {
    if(SidedProxy.mc() != null) {
      MinecraftForge.EVENT_BUS.register(new TextOverlayGui());
      Networking.OverlayTextMessage.setHandler(TextOverlayGui::show);
    }
  }

  public static void show(Player player, final Component message)
  { Networking.OverlayTextMessage.sendToPlayer(player, message, 3000); }

  public static void show(Player player, final Component message, int delay)
  { Networking.OverlayTextMessage.sendToPlayer(player, message, delay); }

  // -----------------------------------------------------------------------------
  // Client side handler
  // -----------------------------------------------------------------------------

  @Mod.EventBusSubscriber(Dist.CLIENT)
  @OnlyIn(Dist.CLIENT)
  public static class TextOverlayGui extends Screen
  {
    private static final Component EMPTY_TEXT = Component.literal("");
    private static final double overlay_y_ = 0.75;
      private final Minecraft mc;
    private static long deadline_;
    private static Component text_;

    public static synchronized Component text()
    { return text_; }

    public static synchronized long deadline()
    { return deadline_; }

    public static synchronized void show(Component s, int displayTimeoutMs)
    { text_ = (s==null)?(EMPTY_TEXT):(s.copy()); deadline_ = System.currentTimeMillis() + displayTimeoutMs; }

    public static synchronized void show(String s, int displayTimeoutMs)
    { text_ = ((s==null)||(s.isEmpty()))?(EMPTY_TEXT):(Component.literal(s)); deadline_ = System.currentTimeMillis() + displayTimeoutMs; }

    TextOverlayGui()
    { super(Component.literal("")); mc = SidedProxy.mc(); }

    @SubscribeEvent
    public void onRenderGui(RenderGuiOverlayEvent.Post event)
    {
      //if(event.getType() != RenderGameOverlayEvent.ElementType.CHAT) return;
      if(deadline() < System.currentTimeMillis()) return;
      if(text()==EMPTY_TEXT) return;
      String txt = text().getString();
      if(txt.isEmpty()) return;
      GuiGraphics mxs = event.getGuiGraphics();
      final Window win = mc.getWindow();
      final Font fr = mc.font;
      final int cx = win.getGuiScaledWidth() / 2;
      final int cy = (int)(win.getGuiScaledHeight() * overlay_y_);
      final int w = fr.width(txt);
      final int h = fr.lineHeight;
      mxs.fillGradient(cx-(w/2)-3, cy-2, cx+(w/2)+2, cy+h+2, 0xaa333333, 0xaa444444);
      mxs.hLine(cx-(w/2)-3, cx+(w/2)+2, cy-2, 0xaa333333);
      mxs.hLine(cx-(w/2)-3, cx+(w/2)+2, cy+h+2, 0xaa333333);
      mxs.vLine(cx-(w/2)-3, cy-2, cy+h+2, 0xaa333333);
      mxs.vLine(cx+(w/2)+2, cy-2, cy+h+2, 0xaa333333);
      mxs.drawCenteredString(fr, text(), cx , cy+1, 0x00ffaa00);
    }
  }

}
