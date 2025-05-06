package com.shackleshot.quarrydigger;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class QuarryDiggerScreen extends AbstractContainerScreen<QuarryDiggerMenu> {
    private static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(QuarryDiggerMod.MOD_ID, "textures/gui/container/quarry_digger.png");

    public QuarryDiggerScreen(QuarryDiggerMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);
        guiGraphics.blit(
                GUI_TEXTURE,         // ResourceLocation
                this.leftPos,        // x на экране
                this.topPos,         // y на экране
                0,                   // u начала в текстуре
                0,                   // v начала в текстуре
                this.imageWidth,     // ширина к копированию
                this.imageHeight,    // высота к копированию
                this.imageWidth,     // полная ширина вашей PNG
                this.imageHeight     // полная высота вашей PNG
        );
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
