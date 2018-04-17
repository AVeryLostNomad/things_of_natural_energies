package com.thelostnomad.tone.block.gui;

import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import com.thelostnomad.tone.block.container.ContainerLivingCraftingStation;
import com.thelostnomad.tone.block.tileentity.TELivingCraftingStation;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiLivingCraftingStation extends GuiContainer {
    protected ResourceLocation background = new ResourceLocation(ThingsOfNaturalEnergies.MODID, "textures/gui/living_crafting_station.png");

    private ContainerLivingCraftingStation container;

    public GuiLivingCraftingStation(InventoryPlayer inv, TELivingCraftingStation tile) {
        super(new ContainerLivingCraftingStation(inv, tile));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        this.mc.getTextureManager().bindTexture(background);
        drawModalRectWithCustomSizedTexture(guiLeft, guiTop, 1.0f, 1.0f, 264, 216, 512, 512);
        //this.drawTexturedModalRect(20, 20, 0, 0, 264, 216);
    }

    public ContainerLivingCraftingStation getContainer() {
        return (ContainerLivingCraftingStation) this.inventorySlots;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y){
        this.fontRenderer.drawString(I18n.format("container.crafting"), 28, 6, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
    }

}
