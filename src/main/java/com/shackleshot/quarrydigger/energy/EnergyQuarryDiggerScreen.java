package com.shackleshot.quarrydigger.energy;

import com.shackleshot.quarrydigger.QuarryDiggerMod;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class EnergyQuarryDiggerScreen extends AbstractContainerScreen<EnergyQuarryDiggerMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(QuarryDiggerMod.MOD_ID,
                    "textures/gui/container/energy_quarry_digger.png");

    // Точные размеры твоей GUI-картинки
    private static final int TEX_WIDTH  = 168;
    private static final int TEX_HEIGHT = 168;

    public EnergyQuarryDiggerScreen(EnergyQuarryDiggerMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = TEX_WIDTH;
        this.imageHeight = TEX_HEIGHT;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        // Просто отрисовываем всю картинку, без ресайза
        g.blit(
                TEXTURE,
                leftPos, topPos,
                0, 0,
                TEX_WIDTH, TEX_HEIGHT,
                TEX_WIDTH, TEX_HEIGHT
        );
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        // Посередине экрана: координаты центра
        String text = menu.getEnergyStored() + " / " + menu.getCapacity();

        int textWidth = font.width(text);
        int x = (this.imageWidth - textWidth) / 2;
        int y = this.imageHeight / 2 - font.lineHeight / 2;

        g.drawString(font, text, x, y, 0x000000, false);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
        super.render(g, mouseX, mouseY, partialTicks);
        renderTooltip(g, mouseX, mouseY);
    }
}
