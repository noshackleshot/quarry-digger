// src/main/java/com/shackleshot/quarrydigger/energy/EnergyQuarryDiggerScreen.java
package com.shackleshot.quarrydigger.energy;

import com.shackleshot.quarrydigger.QuarryDiggerMod;
import com.shackleshot.quarrydigger.energy.EnergyQuarryDiggerMenu;
import com.shackleshot.quarrydigger.energy.EnergyQuarryDiggerBlockEntity;
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

    public EnergyQuarryDiggerScreen(EnergyQuarryDiggerMenu menu,
                                    Inventory inv,
                                    Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        g.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        int stored = menu.getEnergyStored();
        int cap    = menu.getCapacity();
        int h      = (int) (50 * (stored / (float) cap));
        g.blit(TEXTURE,
                leftPos + 10,
                topPos + imageHeight - 60 + (50 - h),
                imageWidth, imageHeight,
                14, h,
                imageWidth, imageHeight + 50
        );
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        super.renderLabels(g, mouseX, mouseY);
        int stored = menu.getEnergyStored();
        int cap    = menu.getCapacity();
        String text = stored + " / " + cap;
        g.drawString(this.font, Component.literal(text), 10, 7, 0xFFFFFF, false);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
        super.render(g, mouseX, mouseY, partialTicks);
        renderTooltip(g, mouseX, mouseY);
    }
}