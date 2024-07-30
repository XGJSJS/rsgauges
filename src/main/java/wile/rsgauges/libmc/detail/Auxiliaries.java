/*
 * @file Auxiliaries.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * General commonly used functionality.
 */
package wile.rsgauges.libmc.detail;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Auxiliaries {
  private static String modid;
  private static Logger logger;
  private static Supplier<CompoundTag> server_config_supplier = CompoundTag::new;

  public static void init(String modid, Logger logger, Supplier<CompoundTag> server_config_supplier) {
    Auxiliaries.modid = modid;
    Auxiliaries.logger = logger;
    Auxiliaries.server_config_supplier = server_config_supplier;
  }

  // -------------------------------------------------------------------------------------------------------------------
  // Mod specific exports
  // -------------------------------------------------------------------------------------------------------------------

  public static String modid() {
    return modid;
  }

  public static Logger logger() {
    return logger;
  }

  // -------------------------------------------------------------------------------------------------------------------
  // Sideness, system/environment, tagging interfaces
  // -------------------------------------------------------------------------------------------------------------------

  @OnlyIn(Dist.CLIENT)
  @SuppressWarnings("all")
  public static boolean isShiftDown() {
    return (InputConstants.isKeyDown(SidedProxy.mc().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT) ||
      InputConstants.isKeyDown(SidedProxy.mc().getWindow().getWindow(), GLFW.GLFW_KEY_RIGHT_SHIFT));
  }

  @OnlyIn(Dist.CLIENT)
  @SuppressWarnings("all")
  public static boolean isCtrlDown() {
    return (InputConstants.isKeyDown(SidedProxy.mc().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL) ||
      InputConstants.isKeyDown(SidedProxy.mc().getWindow().getWindow(), GLFW.GLFW_KEY_RIGHT_CONTROL));
  }

  // -------------------------------------------------------------------------------------------------------------------
  // Logging
  // -------------------------------------------------------------------------------------------------------------------

  public static void logInfo(final String msg)
  { logger.info(msg); }

  public static void logError(final String msg)
  { logger.error(msg); }

  // -------------------------------------------------------------------------------------------------------------------
  // Localization, text formatting
  // -------------------------------------------------------------------------------------------------------------------

  /**
   * Text localization wrapper, implicitly prepends `MODID` to the
   * translation keys. Forces formatting argument, nullable if no special formatting shall be applied..
   */
  public static MutableComponent localizable(String modtrkey, Object... args)
  { return Component.translatable((modtrkey.startsWith("block.") || (modtrkey.startsWith("item."))) ? (modtrkey) : (modid+"."+modtrkey), args); }

  public static MutableComponent localizable(String modtrkey, @Nullable ChatFormatting color, Object... args)
  {
    MutableComponent tr = Component.translatable(modid+"."+modtrkey, args);
    if(color!=null) tr.withStyle(color);
    return tr;
  }

  public static MutableComponent localizable(String modtrkey)
  { return localizable(modtrkey, new Object[]{}); }

  @OnlyIn(Dist.CLIENT)
  public static String localize(String translationKey, Object... args)
  {
    MutableComponent tr = Component.translatable(translationKey, args);
    tr.withStyle(ChatFormatting.RESET);
    final String ft = tr.getString();
    if(ft.contains("${")) {
      // Non-recursive, non-argument lang file entry cross referencing.
      Pattern pt = Pattern.compile("\\$\\{([^}]+)}");
      Matcher mt = pt.matcher(ft);
      StringBuilder sb = new StringBuilder();
      while(mt.find()) {
        String m = mt.group(1);
        if(m.contains("?")) {
          String[] kv = m.split("\\?", 2);
          String key = kv[0].trim();
          boolean not = key.startsWith("!");
          if(not) key = key.replaceFirst("!", "");
          m = kv[1].trim();
          if(!server_config_supplier.get().contains(key)) {
            m = "";
          } else {
            boolean r = server_config_supplier.get().getBoolean(key);
            if(not) r = !r;
            if(!r) m = "";
          }
        }
        mt.appendReplacement(sb, Matcher.quoteReplacement((Component.translatable(m)).getString().trim()));
      }
      mt.appendTail(sb);
      return sb.toString();
    } else {
      return ft;
    }
  }

  /**
   * Returns true if a given key is translated for the current language.
   */
  @OnlyIn(Dist.CLIENT)
  public static boolean hasTranslation(String key)
  { return net.minecraft.client.resources.language.I18n.exists(key); }

  public static final class Tooltip
  {
    @OnlyIn(Dist.CLIENT)
    public static boolean extendedTipCondition()
    { return isShiftDown(); }

    @OnlyIn(Dist.CLIENT)
    public static boolean helpCondition()
    { return isShiftDown() && isCtrlDown(); }

    /**
     * Adds an extended tooltip or help tooltip depending on the key states of CTRL and SHIFT.
     * Returns true if the localisable help/tip was added, false if not (either not CTL/SHIFT or
     * no translation found).
     */
    @OnlyIn(Dist.CLIENT)
    public static boolean addInformation(@Nullable String advancedTooltipTranslationKey, @Nullable String helpTranslationKey, List<Component> tooltip, boolean addAdvancedTooltipHints)
    {
      // Note: intentionally not using keybinding here, this must be `control` or `shift`.
      final boolean help_available = (helpTranslationKey != null) && Auxiliaries.hasTranslation(helpTranslationKey + ".help");
      final boolean tip_available = (advancedTooltipTranslationKey != null) && Auxiliaries.hasTranslation(helpTranslationKey + ".tip");
      if((!help_available) && (!tip_available)) return false;
      String tip_text = "";
      if(helpCondition()) {
        if(help_available) tip_text = localize(helpTranslationKey + ".help");
      } else if(extendedTipCondition()) {
        if(tip_available) tip_text = localize(advancedTooltipTranslationKey + ".tip");
      } else if(addAdvancedTooltipHints) {
        if(tip_available) tip_text += localize(modid + ".tooltip.hint.extended") + (help_available ? " " : "");
        if(help_available) tip_text += localize(modid + ".tooltip.hint.help");
      }
      if(tip_text.isEmpty()) return false;
      String[] tip_list = tip_text.split("\\r?\\n");
      for(String tip:tip_list) {
        tooltip.add(Component.literal(tip.replaceAll("\\s+$","").replaceAll("^\\s+", "")).withStyle(ChatFormatting.GRAY));
      }
      return true;
    }

    /**
     * Adds an extended tooltip or help tooltip for a given stack depending on the key states of CTRL and SHIFT.
     * Format in the lang file is (e.g. for items): "item.MODID.REGISTRYNAME.tip" and "item.MODID.REGISTRYNAME.help".
     * Return value see method pattern above.
     */
    @OnlyIn(Dist.CLIENT)
    public static boolean addInformation(ItemStack stack, List<Component> tooltip, boolean addAdvancedTooltipHints)
    { return addInformation(stack.getDescriptionId(), stack.getDescriptionId(), tooltip, addAdvancedTooltipHints); }
  }

  @SuppressWarnings("unused")
  public static void playerChatMessage(final Player player, final String message)
  {
    String s = message.trim();
    if(!s.isEmpty()) player.sendSystemMessage(Component.translatable(s));
  }

  // -------------------------------------------------------------------------------------------------------------------
  // Tag Handling
  // -------------------------------------------------------------------------------------------------------------------

  public static boolean isInBlockTag(Block block, ResourceLocation tag)
  { return ForgeRegistries.BLOCKS.tags().stream().filter(tg->tg.getKey().location().equals(tag)).anyMatch(tk->tk.contains(block)); }

  // -------------------------------------------------------------------------------------------------------------------
  // Block handling
  // -------------------------------------------------------------------------------------------------------------------

  public static AABB getPixeledAABB(double x0, double y0, double z0, double x1, double y1, double z1)
  { return new AABB(x0/16.0, y0/16.0, z0/16.0, x1/16.0, y1/16.0, z1/16.0); }

  // -------------------------------------------------------------------------------------------------------------------
  // JAR resource related
  // -------------------------------------------------------------------------------------------------------------------

  public static String loadResourceText(InputStream is)
  {
    try {
      if(is==null) return "";
      BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
      return br.lines().collect(Collectors.joining("\n"));
    } catch(Throwable e) {
      return "";
    }
  }

  public static String loadResourceText(String path)
  { return loadResourceText(Auxiliaries.class.getResourceAsStream(path)); }

  public static void logGitVersion(String mod_name)
  {
    try {
      // Done during construction to have an exact version in case of a crash while registering.
      String version = Auxiliaries.loadResourceText("/.gitversion-" + modid).trim();
      logInfo(mod_name+((version.isEmpty())?(" (dev build)"):(" GIT id #"+version)) + ".");
    } catch(Throwable e) {
      // (void)e; well, then not. Priority is not to get unneeded crashes because of version logging.
    }
  }
}
