package com.misterpemodder.shulkerboxtooltip.impl.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientBundleTooltip;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class VanillaPreviewRenderer extends BasePreviewRenderer {
  public static final ResourceLocation DEFAULT_TEXTURE = ResourceLocation.withDefaultNamespace("container/bundle/background");
  public static final VanillaPreviewRenderer INSTANCE = new VanillaPreviewRenderer();

  VanillaPreviewRenderer() {
    super(18, 20, 2, 2);
  }

  @Override
  public int getWidth() {
    return this.getMaxRowSize() * 18;
  }

  private int getColumnsWidth() {
    return this.getColumnCount() * 18 + 2;
  }

  @Override
  public int getHeight() {
    return this.getRowCount() * 20 + 3;
  }

  private int getColumnCount() {
    return Math.min(this.getMaxRowSize(), this.getInvSize());
  }

  private int getRowCount() {
    return (int) Math.ceil(((double) getInvSize()) / (double) this.getMaxRowSize());
  }

  @Override
  public void draw(int x, int y, GuiGraphics graphics, Font font, int mouseX, int mouseY) {
    ++y;
    RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    RenderSystem.enableDepthTest();

    ResourceLocation texture = this.getTexture();

    graphics.blitSprite(texture, x, y, this.getColumnsWidth(), this.getHeight());

    var sprite = ClientBundleTooltip.Texture.SLOT;
    for (int slotY = 0; slotY < this.getRowCount(); ++slotY) {
      for (int slotX = 0; slotX < this.getColumnCount(); ++slotX) {
        int px = x + slotX * 18 + 1;
        int py = y + slotY * 20 + 1;
        graphics.blitSprite(sprite.sprite, px, py, 0, sprite.w, sprite.h);
      }
    }

    this.drawSlotHighlight(x, y, graphics, mouseX, mouseY);
    this.drawItems(x, y, graphics, font);
    this.drawInnerTooltip(x, y, graphics, font, mouseX, mouseY);
  }

  private ResourceLocation getTexture() {
    if (this.textureOverride == null)
      return DEFAULT_TEXTURE;
    else
      return this.textureOverride;
  }
}
