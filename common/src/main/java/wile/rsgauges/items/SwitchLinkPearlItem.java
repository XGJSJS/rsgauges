package wile.rsgauges.items;

import com.google.common.collect.ImmutableList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wile.rsgauges.ModConfig;
import wile.rsgauges.detail.ModResources;
import wile.rsgauges.detail.SwitchLink;
import wile.rsgauges.detail.SwitchLink.ISwitchLinkable;
import wile.rsgauges.libmc.detail.Auxiliaries;
import wile.rsgauges.libmc.detail.Overlay;
import wile.rsgauges.libmc.detail.ModRegistries;

import java.util.List;

public class SwitchLinkPearlItem extends RsItem {
  public SwitchLinkPearlItem(Item.Properties properties) {
    super(properties);
  }

  @Override
  public boolean isFoil(@NotNull ItemStack stack) {
    return false;
  }

  @Override
  public boolean isEnchantable(@NotNull ItemStack stack) {
    return false;
  }

  @Override
  public boolean isBarVisible(@NotNull ItemStack stack) {
    return false;
  }

  @Override
  public boolean canAttackBlock(@NotNull BlockState state, @NotNull Level worldIn, @NotNull BlockPos pos, @NotNull Player player) {
    return false;
  }

  @Override
  public void appendHoverText(@NotNull ItemStack stack, @Nullable Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
    final SwitchLink link = SwitchLink.fromItemStack(stack);
    if (Auxiliaries.Tooltip.addInformation(stack, tooltip, (!link.valid))) return;
    if (!link.valid) return;
    final Block targetBlock = BuiltInRegistries.BLOCK.get(new ResourceLocation(link.block_name));
      tooltip.add(Auxiliaries.localizable(
              "switchlinking.switchlink_pearl.tooltip.linkedblock",
              ChatFormatting.GRAY,
              (Component.translatable(targetBlock.getDescriptionId()))
                      .withStyle(ChatFormatting.YELLOW)
                      .withStyle(ChatFormatting.ITALIC)));
      if (Minecraft.getInstance().player!=null) {
      final int distance = link.distance(Minecraft.getInstance().player.blockPosition());
      if (distance >= 0) {
        tooltip.add(Component.literal(Auxiliaries.localizable(
          "switchlinking.switchlink_pearl.tooltip.linkeddistance",
          ChatFormatting.GRAY, new Object[]{distance}).getString() + (
            (((distance <= ModConfig.max_switch_linking_distance()) || (ModConfig.max_switch_linking_distance()<=0))) ? ("")
            : (" " + Auxiliaries.localizable("switchlinking.switchlink_pearl.tooltip.toofaraway", ChatFormatting.DARK_RED).getString())
          )
        ));
      }
    }
    tooltip.add(Auxiliaries.localizable(
      "switchlinking.relayconfig.confval" + link.mode().index(),
      ChatFormatting.ITALIC
    ));
    super.appendHoverText(stack, world, tooltip, flag);
  }

  @Override
  public @NotNull InteractionResultHolder<ItemStack> use(Level world, @NotNull Player player, @NotNull InteractionHand hand) {
    if (world.isClientSide() || (!player.isShiftKeyDown())) {
      return new InteractionResultHolder<>(InteractionResult.PASS, player.getItemInHand(hand));
    } else {
      usePearl(world, player);
      return new InteractionResultHolder<>(InteractionResult.CONSUME, player.getItemInHand(hand));
    }
  }

  @Override
  public void inventoryTick(@NotNull ItemStack stack, @NotNull Level world, @NotNull Entity entity, int slot, boolean selected) {
    if((!selected) || (!entity.level().isClientSide()) || (world.getRandom().nextDouble() > 0.3)) return;
    final SwitchLink lnk = SwitchLink.fromItemStack(stack);
    if((!lnk.valid) || (lnk.target_position.distSqr(entity.blockPosition()) > 900)) return;
    Vec3 p = Vec3.atLowerCornerOf(lnk.target_position).add(
      ((world.getRandom().nextDouble()-0.5)*0.2),
      ((world.getRandom().nextDouble()-0.5)*0.2),
      ((world.getRandom().nextDouble()-0.5)*0.2)
    );
    Vec3 v = new Vec3(0, ((world.getRandom().nextDouble()-0.5)*0.001), 0);
    BlockState state = world.getBlockState(lnk.target_position);
    if (!(state.getBlock() instanceof ISwitchLinkable)) return;
    p = p.add(state.getShape(world, lnk.target_position).bounds().getCenter());
    int power = ((ISwitchLinkable)(state.getBlock())).switchLinkOutputPower(world, lnk.target_position).orElse(0);
    if(power > 0) {
      world.addParticle(ParticleTypes.INSTANT_EFFECT, false, p.x, p.y, p.z, v.x, v.y, v.z);
    } else {
      world.addParticle(ParticleTypes.WITCH, false, p.x, p.y, p.z, v.x, v.y, v.z);
    }
  }

  public static void usePearl(Level world, Player player) {
    switch (SwitchLink.fromPlayerActiveItem(world, player).trigger(world, player.blockPosition(), player)) {
      case OK:
        ModResources.BlockSoundEvents.SWITCHLINK_LINK_PEAL_USE_SUCCESS.play(world, player.blockPosition());
        return;
      case TOO_FAR:
        Overlay.show(player, Auxiliaries.localizable("switchlinking.switchlink_pearl.use.toofaraway", ChatFormatting.DARK_RED));
        break;
      case TARGET_GONE:
        Overlay.show(player, Auxiliaries.localizable("switchlinking.switchlink_pearl.use.targetgone", ChatFormatting.DARK_RED));
        break;
      case NOT_MATCHED: // Can't happen here
      case INVALID_LINKDATA:
      case REJECTED:
        break;
    }
    ModResources.BlockSoundEvents.SWITCHLINK_LINK_PEAL_USE_FAILED.play(world, player.blockPosition());
  }


  public static ItemStack createFromPearl(Level world, BlockPos pos, Player player) {
    final ItemStack stack_held = player.getInventory().getSelected();
    if(stack_held.isEmpty()) return ItemStack.EMPTY;
    final ItemStack link_pearl = createForTarget(world, pos);
    if(link_pearl.isEmpty()) return ItemStack.EMPTY;
    link_pearl.getOrCreateTag().putLong("cdtime", world.getGameTime());
    if(stack_held.getCount() > 1) {
      // @todo: move shrinked ender pearl stack into another slot
      link_pearl.setCount(stack_held.getCount());
    }
    return link_pearl;
  }

  public static ItemStack createForTarget(Level world, BlockPos pos) {
    final BlockState state = world.getBlockState(pos);
    if(!(state.getBlock() instanceof SwitchLink.ISwitchLinkable)) return ItemStack.EMPTY;
    ItemStack stack = new ItemStack(ModRegistries.getItem("switchlink_pearl"));
    final SwitchLink.LinkMode mode = ((SwitchLink.ISwitchLinkable)state.getBlock()).switchLinkGetSupportedTargetModes().get(0);
    stack.setTag(SwitchLink.fromTargetPosition(world, pos).mode(mode).toNbt());
    return stack;
  }

  public static boolean cycleLinkMode(ItemStack stack, Level world, BlockPos target_pos, boolean with_click_time) {
    SwitchLink lnk = SwitchLink.fromItemStack(stack);
    if(!target_pos.equals(lnk.target_position)) return false;
    final BlockState state = world.getBlockState(lnk.target_position);
    if((!(state.getBlock() instanceof SwitchLink.ISwitchLinkable))) return false;
    final long t = world.getGameTime();
    if(with_click_time) {
      final long dt = Math.abs(t-stack.getOrCreateTag().getLong("cdtime"));
      if(dt < 7) return true;
      if(dt > 40) { stack.getOrCreateTag().putLong("cdtime", t); return true; }
    }
    final SwitchLink.ISwitchLinkable target = (SwitchLink.ISwitchLinkable)(state.getBlock());
    ImmutableList<SwitchLink.LinkMode> modes = target.switchLinkGetSupportedTargetModes();
    int index = modes.indexOf(lnk.mode()) + 1;
    SwitchLink.LinkMode next = modes.get((index < 0) || (index>=modes.size()) ? 0 : index);
    lnk.mode(next);
    if(!lnk.valid) return false;
    stack.setTag(lnk.toNbt());
    stack.getOrCreateTag().putLong("cdtime", t);
    return true;
  }
}