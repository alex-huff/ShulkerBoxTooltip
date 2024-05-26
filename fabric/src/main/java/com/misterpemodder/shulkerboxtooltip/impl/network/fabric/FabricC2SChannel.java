package com.misterpemodder.shulkerboxtooltip.impl.network.fabric;

import com.misterpemodder.shulkerboxtooltip.ShulkerBoxTooltip;
import com.misterpemodder.shulkerboxtooltip.impl.network.channel.C2SChannel;
import com.misterpemodder.shulkerboxtooltip.impl.network.context.C2SMessageContext;
import com.misterpemodder.shulkerboxtooltip.impl.network.context.MessageContext;
import com.misterpemodder.shulkerboxtooltip.impl.network.message.MessageType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

class FabricC2SChannel<T> extends FabricChannel<T> implements C2SChannel<T> {
  private boolean payloadTypeRegistered = false;

  @Environment(EnvType.CLIENT)
  private boolean serverRegistered;

  public FabricC2SChannel(Identifier id, MessageType<T> type) {
    super(id, type);
    if (ShulkerBoxTooltip.isClient())
      this.serverRegistered = false;
  }

  @Override
  public void registerPayloadType() {
    if (this.payloadTypeRegistered) {
      return;
    }
    PayloadTypeRegistry.playC2S().register(this.id, this.codec);
    PayloadTypeRegistry.playS2C().register(this.id, this.codec);
    this.payloadTypeRegistered = true;
  }

  @Override
  public void registerFor(ServerPlayerEntity player) {
    ServerPlayNetworkHandler handler = player.networkHandler;

    if (handler == null) {
      ShulkerBoxTooltip.LOGGER.error("Cannot register packet receiver for " + this.getId() + ", player is not in game");
      return;
    }
    ServerPlayNetworking.registerReceiver(handler, this.id, this::onReceive);
  }

  @Override
  public void unregisterFor(ServerPlayerEntity player) {
    ServerPlayNetworkHandler handler = player.networkHandler;

    if (handler != null) {
      ServerPlayNetworking.unregisterReceiver(handler, this.getId());
    }
  }

  @Override
  public void sendToServer(T message) {
    ClientPlayNetworking.send(new Payload<>(this.id, message));
  }

  @Override
  @Environment(EnvType.CLIENT)
  public boolean canSendToServer() {
    return this.serverRegistered && MinecraftClient.getInstance().getNetworkHandler() != null;
  }

  @Override
  public void onRegister(MessageContext<T> context) {
    if (context.getReceivingSide() == MessageContext.Side.CLIENT)
      this.serverRegistered = true;
    super.onRegister(context);
  }

  @Override
  public void onUnregister(MessageContext<T> context) {
    if (context.getReceivingSide() == MessageContext.Side.CLIENT)
      this.serverRegistered = false;
    super.onUnregister(context);
  }

  @Override
  @Environment(EnvType.CLIENT)
  public void onDisconnect() {
    this.serverRegistered = false;
  }

  private void onReceive(Payload<T> payload, ServerPlayNetworking.Context context) {
    this.type.onReceive(payload.value(), new C2SMessageContext<>(context.player(), this));
  }

}