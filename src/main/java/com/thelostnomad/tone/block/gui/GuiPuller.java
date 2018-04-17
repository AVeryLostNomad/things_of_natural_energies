package com.thelostnomad.tone.block.gui;

import com.thelostnomad.tone.block.container.ContainerPuller;
import com.thelostnomad.tone.block.tileentity.TEPuller;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiConfig;

import java.awt.*;

public class GuiPuller extends GuiContainer {

    // This is the resource location for the background image for the GUI
    private static final ResourceLocation texture = new ResourceLocation("thingsofnaturalenergies", "textures/gui/puller.png");
    private TEPuller tileEntityPuller;

    public GuiPuller(InventoryPlayer invPlayer, TEPuller tile) {
        super(new ContainerPuller(invPlayer, tile));
        tileEntityPuller = tile;
        // Set the width and height of the gui.  Should match the size of the texture!
        xSize = 176;
        ySize = 133;
    }

    // draw the background for the GUI - rendered first
    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int x, int y) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        // Bind the image texture of our custom container
        this.drawDefaultBackground();
        // Draw the image
        this.mc.getTextureManager().bindTexture(texture);
        drawModalRectWithCustomSizedTexture(guiLeft, guiTop, 1.0f, 1.0f, 175, 132, 256, 256);
    }

    // draw the foreground for the GUI - rendered after the slots, but before the dragged items and tooltips
    // renders relative to the top left corner of the background
    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        final int LABEL_XPOS = 5;
        final int LABEL_YPOS = 5;
        fontRenderer.drawString("Puller", LABEL_XPOS, LABEL_YPOS, Color.darkGray.getRGB());
        this.renderHoveredToolTip(mouseX - guiLeft, mouseY - guiTop);
    }
}
