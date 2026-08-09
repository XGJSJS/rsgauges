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
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import wile.rsgauges.RsGaugesMod;

import java.util.function.BiConsumer;

public class Networking {
  public static void init() {
    NetworkManager.registerReceiver(NetworkManager.Side.C2S, PacketTileNotifyClientToServer.TYPE, PacketTileNotifyClientToServer.STREAM_CODEC, (message, context) -> PacketTileNotifyClientToServer.Handler.handle(message, context.getPlayer()));
    NetworkManager.registerReceiver(NetworkManager.Side.C2S, PacketContainerSyncClientToServer.TYPE, PacketContainerSyncClientToServer.STREAM_CODEC, (message, context) -> PacketContainerSyncClientToServer.Handler.handle(message, context.getPlayer()));
  }

  public static void initClient() {
    NetworkManager.registerReceiver(NetworkManager.Side.S2C, PacketTileNotifyServerToClient.TYPE, PacketTileNotifyServerToClient.STREAM_CODEC, (message, context) -> PacketTileNotifyServerToClient.Handler.handle(message));
    NetworkManager.registerReceiver(NetworkManager.Side.S2C, PacketContainerSyncServerToClient.TYPE, PacketContainerSyncServerToClient.STREAM_CODEC, (message, context) -> PacketContainerSyncServerToClient.Handler.handle(message));
    NetworkManager.registerReceiver(NetworkManager.Side.S2C, OverlayTextMessage.TYPE, OverlayTextMessage.STREAM_CODEC, (message, context) -> OverlayTextMessage.Handler.handle(message));
  }

  //--------------------------------------------------------------------------------------------------------------------
  // Tile entity notifications
  //--------------------------------------------------------------------------------------------------------------------

  public interface IPacketTileNotifyReceiver {
    default void onServerPacketReceived(CompoundTag nbt) {}
    default void onClientPacketReceived(Player player, CompoundTag nbt) {}
  }

  public record PacketTileNotifyClientToServer(BlockPos pos, CompoundTag nbt) implements CustomPacketPayload {
    public static final Type<PacketTileNotifyClientToServer> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(RsGaugesMod.MODID, "tile_notify_c2s"));
    public static final StreamCodec<ByteBuf, PacketTileNotifyClientToServer> STREAM_CODEC = StreamCodec.composite(BlockPos.STREAM_CODEC, PacketTileNotifyClientToServer::pos, ByteBufCodecs.TRUSTED_COMPOUND_TAG, PacketTileNotifyClientToServer::nbt, PacketTileNotifyClientToServer::new);

    public static PacketTileNotifyClientToServer parse(final FriendlyByteBuf buf)
    { return new PacketTileNotifyClientToServer(buf.readBlockPos(), buf.readNbt()); }

    public static void compose(final PacketTileNotifyClientToServer pkt, final FriendlyByteBuf buf)
    { buf.writeBlockPos(pkt.pos); buf.writeNbt(pkt.nbt); }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
      return TYPE;
    }

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

  public record PacketTileNotifyServerToClient(CompoundTag nbt, BlockPos pos) implements CustomPacketPayload {
    public static final Type<PacketTileNotifyServerToClient> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(RsGaugesMod.MODID, "tile_notify_s2c"));
    public static final StreamCodec<ByteBuf, PacketTileNotifyServerToClient> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.TRUSTED_COMPOUND_TAG, PacketTileNotifyServerToClient::nbt, BlockPos.STREAM_CODEC, PacketTileNotifyServerToClient::pos, PacketTileNotifyServerToClient::new);

    public static void sendToPlayer(Player player, BlockEntity te, CompoundTag nbt) {
      if((!(player instanceof ServerPlayer)) || (te==null) || (nbt==null)) return;
      NetworkManager.sendToPlayer((ServerPlayer) player, new PacketTileNotifyServerToClient(te, nbt));
    }

    public static void sendToPlayers(BlockEntity te, CompoundTag nbt) {
      if(te==null || te.getLevel()==null) return;
      for(Player player: te.getLevel().players()) sendToPlayer(player, te, nbt);
    }

    public PacketTileNotifyServerToClient(BlockEntity te, CompoundTag nbt) {
      this(nbt, te.getBlockPos());
    }

    public static PacketTileNotifyServerToClient parse(final FriendlyByteBuf buf)
    { return new PacketTileNotifyServerToClient(ByteBufCodecs.TRUSTED_COMPOUND_TAG.decode(buf), BlockPos.STREAM_CODEC.decode(buf)); }

    public static void compose(final PacketTileNotifyServerToClient pkt, final FriendlyByteBuf buf)
    { buf.writeBlockPos(pkt.pos); buf.writeNbt(pkt.nbt); }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
      return TYPE;
    }

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

  public record PacketContainerSyncClientToServer(int id, CompoundTag nbt) implements CustomPacketPayload {
    public static final Type<PacketContainerSyncClientToServer> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(RsGaugesMod.MODID, "container_sync_c2s"));
    public static final StreamCodec<ByteBuf, PacketContainerSyncClientToServer> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.INT, PacketContainerSyncClientToServer::id, ByteBufCodecs.TRUSTED_COMPOUND_TAG, PacketContainerSyncClientToServer::nbt, PacketContainerSyncClientToServer::new);

    public static PacketContainerSyncClientToServer parse(final FriendlyByteBuf buf) {
      return new PacketContainerSyncClientToServer(buf.readInt(), buf.readNbt());
    }

    public static void compose(final PacketContainerSyncClientToServer pkt, final FriendlyByteBuf buf) {
      buf.writeInt(pkt.id);
      buf.writeNbt(pkt.nbt);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
      return TYPE;
    }

    public static class Handler {
      public static void handle(final PacketContainerSyncClientToServer pkt, Player player) {
          if((player==null) || !(player.containerMenu instanceof INetworkSynchronisableContainer)) return;
          if(player.containerMenu.containerId != pkt.id) return;
          ((INetworkSynchronisableContainer)player.containerMenu).onClientPacketReceived(pkt.id, player,pkt.nbt);
      }
    }
  }

  public record PacketContainerSyncServerToClient(int id, CompoundTag nbt) implements CustomPacketPayload {
    public static final Type<PacketContainerSyncServerToClient> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(RsGaugesMod.MODID, "container_sync_s2c"));
    public static final StreamCodec<ByteBuf, PacketContainerSyncServerToClient> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.INT, PacketContainerSyncServerToClient::id, ByteBufCodecs.TRUSTED_COMPOUND_TAG, PacketContainerSyncServerToClient::nbt, PacketContainerSyncServerToClient::new);

    public static PacketContainerSyncServerToClient parse(final FriendlyByteBuf buf) {
      return new PacketContainerSyncServerToClient(buf.readInt(), buf.readNbt());
    }

    public static void compose(final PacketContainerSyncServerToClient pkt, final FriendlyByteBuf buf) {
      buf.writeInt(pkt.id);
      buf.writeNbt(pkt.nbt);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
      return TYPE;
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

  public static class OverlayTextMessage implements CustomPacketPayload {
    public static final int DISPLAY_TIME_MS = 3000;
    private static BiConsumer<Component, Integer> handler_ = null;
    private final Component data_;
    private final int delay_;
    private Component data() { return data_; }
    private int delay() { return delay_; }
    public static final Type<OverlayTextMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(RsGaugesMod.MODID, "overlay_text"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OverlayTextMessage> STREAM_CODEC = StreamCodec.composite(ComponentSerialization.TRUSTED_STREAM_CODEC, OverlayTextMessage::data, component -> new OverlayTextMessage(component, DISPLAY_TIME_MS));

    public static void setHandler(BiConsumer<Component, Integer> handler) {
      if (handler_==null)
        handler_ = handler;
    }

    public static void sendToPlayer(Player player, Component message, int delay) {
      if (!(player instanceof ServerPlayer serverPlayer)) return;
      NetworkManager.sendToPlayer(serverPlayer, new OverlayTextMessage(message, delay));
    }

    public OverlayTextMessage(final Component tct, int delay) {
      data_ = tct.copy();
      delay_ = delay;
    }

    public static OverlayTextMessage parse(final RegistryFriendlyByteBuf buf) {
      try {
        return new OverlayTextMessage(ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buf), DISPLAY_TIME_MS);
      } catch(Throwable e) {
        return new OverlayTextMessage(Component.literal("[incorrect translation]"), DISPLAY_TIME_MS);
      }
    }

    public static void compose(final OverlayTextMessage pkt, final RegistryFriendlyByteBuf buf) {
      try {
        ComponentSerialization.TRUSTED_STREAM_CODEC.encode(buf, pkt.data());
      } catch(Throwable e) {
          Auxiliaries.logger().error("OverlayTextMessage.toBytes() failed: {}", e.getMessage());
      }
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
      return TYPE;
    }

    public static class Handler {
      public static void handle(final OverlayTextMessage pkt) {
        if (handler_ != null)
          handler_.accept(pkt.data(), pkt.delay());
      }
    }
  }
}