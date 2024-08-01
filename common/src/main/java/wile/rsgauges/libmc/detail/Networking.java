/*
 * @file Networking.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Main client/server message handling.
 */
package wile.rsgauges.libmc.detail;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import wile.rsgauges.RsGaugesMod;

import java.util.function.BiConsumer;

public class Networking {
  public static void init(String modid) {
    NetworkManager.registerReceiver(NetworkManager.Side.C2S, new ResourceLocation(modid, "1"), (buf, context) -> PacketTileNotifyClientToServer.Handler.handle(PacketTileNotifyClientToServer.parse(buf), context.getPlayer()));
    NetworkManager.registerReceiver(NetworkManager.Side.C2S, new ResourceLocation(modid, "3"), (buf, context) -> PacketContainerSyncClientToServer.Handler.handle(PacketContainerSyncClientToServer.parse(buf), context.getPlayer()));
  }

  public static void initClient(String modid) {
    NetworkManager.registerReceiver(NetworkManager.Side.S2C, new ResourceLocation(modid, "2"), (buf, context) -> PacketTileNotifyServerToClient.Handler.handle(PacketTileNotifyServerToClient.parse(buf)));
    NetworkManager.registerReceiver(NetworkManager.Side.S2C, new ResourceLocation(modid, "4"), (buf, context) -> PacketContainerSyncServerToClient.Handler.handle(PacketContainerSyncServerToClient.parse(buf)));
    NetworkManager.registerReceiver(NetworkManager.Side.S2C, new ResourceLocation(modid, "5"), (buf, context) -> OverlayTextMessage.Handler.handle(OverlayTextMessage.parse(buf)));
  }

  //--------------------------------------------------------------------------------------------------------------------
  // Tile entity notifications
  //--------------------------------------------------------------------------------------------------------------------

  public interface IPacketTileNotifyReceiver {
    default void onServerPacketReceived(CompoundTag nbt) {}
    default void onClientPacketReceived(Player player, CompoundTag nbt) {}
  }

  public static class PacketTileNotifyClientToServer {
    CompoundTag nbt;
    BlockPos pos;

    public PacketTileNotifyClientToServer(BlockPos pos, CompoundTag nbt) {
      this.nbt = nbt; this.pos = pos;
    }

    public static PacketTileNotifyClientToServer parse(final FriendlyByteBuf buf)
    { return new PacketTileNotifyClientToServer(buf.readBlockPos(), buf.readNbt()); }

    public static void compose(final PacketTileNotifyClientToServer pkt, final FriendlyByteBuf buf)
    { buf.writeBlockPos(pkt.pos); buf.writeNbt(pkt.nbt); }

    public static class Handler {
      public static void handle(final PacketTileNotifyClientToServer pkt, final Player player) {
          if(player==null) return;
          Level world = player.level();
          final BlockEntity te = world.getBlockEntity(pkt.pos);
          if(!(te instanceof IPacketTileNotifyReceiver)) return;
          ((IPacketTileNotifyReceiver)te).onClientPacketReceived(player, pkt.nbt);
      }
    }
  }

  public static class PacketTileNotifyServerToClient {
    CompoundTag nbt;
    BlockPos pos;

    public static void sendToPlayer(Player player, BlockEntity te, CompoundTag nbt) {
      if((!(player instanceof ServerPlayer)) || (te==null) || (nbt==null)) return;
      FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
      PacketTileNotifyServerToClient.compose(new PacketTileNotifyServerToClient(te, nbt), buf);
      NetworkManager.sendToPlayer((ServerPlayer) player, new ResourceLocation(RsGaugesMod.MODID, "5"), buf);
    }

    public static void sendToPlayers(BlockEntity te, CompoundTag nbt) {
      if(te==null || te.getLevel()==null) return;
      for(Player player: te.getLevel().players()) sendToPlayer(player, te, nbt);
    }

    public PacketTileNotifyServerToClient(BlockPos pos, CompoundTag nbt)
    { this.nbt=nbt; this.pos=pos; }

    public PacketTileNotifyServerToClient(BlockEntity te, CompoundTag nbt)
    { this.nbt=nbt; pos=te.getBlockPos(); }

    public static PacketTileNotifyServerToClient parse(final FriendlyByteBuf buf)
    { return new PacketTileNotifyServerToClient(buf.readBlockPos(), buf.readNbt()); }

    public static void compose(final PacketTileNotifyServerToClient pkt, final FriendlyByteBuf buf)
    { buf.writeBlockPos(pkt.pos); buf.writeNbt(pkt.nbt); }

    public static class Handler {
      public static void handle(final PacketTileNotifyServerToClient pkt) {
          if((pkt.nbt==null) || (pkt.pos==null)) return;
          Level world = SidedProxy.getWorldClientSide();
          if(world == null) return;
          final BlockEntity te = world.getBlockEntity(pkt.pos);
          if(!(te instanceof IPacketTileNotifyReceiver)) return;
          ((IPacketTileNotifyReceiver)te).onServerPacketReceived(pkt.nbt);
      }
    }
  }

  //--------------------------------------------------------------------------------------------------------------------
  // (GUI) Container synchronization
  //--------------------------------------------------------------------------------------------------------------------

  public interface INetworkSynchronisableContainer {
    void onServerPacketReceived(int windowId, CompoundTag nbt);
    void onClientPacketReceived(int windowId, Player player, CompoundTag nbt);
  }

  public static class PacketContainerSyncClientToServer {
    int id;
    CompoundTag nbt;

    public PacketContainerSyncClientToServer(int id, CompoundTag nbt) {
      this.nbt = nbt;
      this.id = id;
    }

    public static PacketContainerSyncClientToServer parse(final FriendlyByteBuf buf) {
      return new PacketContainerSyncClientToServer(buf.readInt(), buf.readNbt());
    }

    public static void compose(final PacketContainerSyncClientToServer pkt, final FriendlyByteBuf buf) {
      buf.writeInt(pkt.id);
      buf.writeNbt(pkt.nbt);
    }

    public static class Handler {
      public static void handle(final PacketContainerSyncClientToServer pkt, Player player) {
          if((player==null) || !(player.containerMenu instanceof INetworkSynchronisableContainer)) return;
          if(player.containerMenu.containerId != pkt.id) return;
          ((INetworkSynchronisableContainer)player.containerMenu).onClientPacketReceived(pkt.id, player,pkt.nbt);
      }
    }
  }

  public static class PacketContainerSyncServerToClient {
    int id;
    CompoundTag nbt;

    public PacketContainerSyncServerToClient(int id, CompoundTag nbt) {
      this.nbt = nbt;
      this.id = id;
    }

    public static PacketContainerSyncServerToClient parse(final FriendlyByteBuf buf) {
      return new PacketContainerSyncServerToClient(buf.readInt(), buf.readNbt());
    }

    public static void compose(final PacketContainerSyncServerToClient pkt, final FriendlyByteBuf buf) {
      buf.writeInt(pkt.id);
      buf.writeNbt(pkt.nbt);
    }

    public static class Handler {
      public static void handle(final PacketContainerSyncServerToClient pkt) {
          Player player = SidedProxy.getPlayerClientSide();
          if((player==null) || !(player.containerMenu instanceof INetworkSynchronisableContainer)) return;
          if(player.containerMenu.containerId != pkt.id) return;
          ((INetworkSynchronisableContainer)player.containerMenu).onServerPacketReceived(pkt.id,pkt.nbt);
      }
    }
  }

  //--------------------------------------------------------------------------------------------------------------------
  // Main window GUI text message
  //--------------------------------------------------------------------------------------------------------------------

  public static class OverlayTextMessage {
    public static final int DISPLAY_TIME_MS = 3000;
    private static BiConsumer<Component, Integer> handler_ = null;
    private final Component data_;
    private final int delay_;
    private Component data() { return data_; }
    private int delay() { return delay_; }

    public static void setHandler(BiConsumer<Component, Integer> handler) {
      if (handler_==null)
        handler_ = handler;
    }

    public static void sendToPlayer(Player player, Component message, int delay) {
      if (!(player instanceof ServerPlayer serverPlayer)) return;
      FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
      OverlayTextMessage.compose(new OverlayTextMessage(message, delay), buf);
      NetworkManager.sendToPlayer(serverPlayer, new ResourceLocation(RsGaugesMod.MODID, "5"), buf);
    }

    public OverlayTextMessage(final Component tct, int delay) {
      data_ = tct.copy();
      delay_ = delay;
    }

    public static OverlayTextMessage parse(final FriendlyByteBuf buf) {
      try {
        return new OverlayTextMessage(buf.readComponent(), DISPLAY_TIME_MS);
      } catch(Throwable e) {
        return new OverlayTextMessage(Component.literal("[incorrect translation]"), DISPLAY_TIME_MS);
      }
    }

    public static void compose(final OverlayTextMessage pkt, final FriendlyByteBuf buf) {
      try {
        buf.writeComponent(pkt.data());
      } catch(Throwable e) {
          Auxiliaries.logger().error("OverlayTextMessage.toBytes() failed: {}", e.getMessage());
      }
    }

    public static class Handler {
      public static void handle(final OverlayTextMessage pkt) {
        if (handler_ != null)
          handler_.accept(pkt.data(), pkt.delay());
      }
    }
  }
}