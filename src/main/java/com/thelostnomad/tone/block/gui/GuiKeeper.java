package com.thelostnomad.tone.block.gui;

import com.sun.corba.se.impl.orbutil.concurrent.Sync;
import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import com.thelostnomad.tone.block.container.ContainerKeeper;
import com.thelostnomad.tone.block.tileentity.TEKeeper;
import com.thelostnomad.tone.network.GUIUpdatePacket;
import com.thelostnomad.tone.network.KindlyGiveMeAnUpdatedGUI;
import com.thelostnomad.tone.network.TonePacketHandler;
import com.thelostnomad.tone.util.gui.SyncableGui;
import com.thelostnomad.tone.util.gui.ToneCheckbox;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class GuiKeeper extends GuiContainer implements SyncableGui {

    // This is the resource location for the background image for the GUI
    private static final ResourceLocation texture = new ResourceLocation("thingsofnaturalenergies", "textures/gui/keeper.png");
    private TEKeeper tileEntityKeeper;

    ToneCheckbox includeInInventory;
    ToneCheckbox redstoneOn;
    ToneCheckbox exactItem;

    ToneCheckbox[] arrayOfThings;

    public GuiKeeper(InventoryPlayer invPlayer, TEKeeper tile) {
        super(new ContainerKeeper(invPlayer, tile));
        tileEntityKeeper = tile;
        // Set the width and height of the gui.  Should match the size of the texture!
        xSize = 176;
        ySize = 133;
    }

    @Override
    public void initGui() {
        super.initGui();
        List<String> lines = new ArrayList<>();
        lines.add(TextFormatting.ITALIC + "Useful for keeping x amount");
        lines.add(TextFormatting.ITALIC + "of an item crafted. Checked");
        lines.add(TextFormatting.ITALIC + "includes this keeper's stack");
        lines.add(TextFormatting.ITALIC + "in the tree's inventory.");
        includeInInventory = new ToneCheckbox(0, 66 + guiLeft, 6 + guiTop, "Include Result in Inventory?", tileEntityKeeper.isIncludeInInventory(), lines);
        this.addButton(includeInInventory);

        List<String> linesRedstone = new ArrayList<>();
        linesRedstone.add(TextFormatting.ITALIC + "If enabled, this keeper will");
        linesRedstone.add(TextFormatting.ITALIC + "only work when powered by");
        linesRedstone.add(TextFormatting.ITALIC + "redstone.");
        redstoneOn = new ToneCheckbox(1, 98 + guiLeft, 31 + guiTop, "Redstone Required?", tileEntityKeeper.isRedstoneRequired(), linesRedstone);
        this.addButton(redstoneOn);

        List<String> linesExact = new ArrayList<>();
        linesExact.add(TextFormatting.ITALIC + "If enabled, this keeper will");
        linesExact.add(TextFormatting.ITALIC + "only produce exact copies of");
        linesExact.add(TextFormatting.ITALIC + "the left item. If disabled,");
        linesExact.add(TextFormatting.ITALIC + "NBT will not be considered.");
        exactItem = new ToneCheckbox(2, 82 + guiLeft, 18 + guiTop, "Exact Item?", tileEntityKeeper.isExactItem(), linesExact);
        this.addButton(exactItem);

        //82 18
        arrayOfThings = new ToneCheckbox[]{
            includeInInventory,
            redstoneOn,
            exactItem
        };

        TonePacketHandler.sendToServer(new KindlyGiveMeAnUpdatedGUI());
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        if(includeInInventory.isChecked() != tileEntityKeeper.isIncludeInInventory()){
            tileEntityKeeper.setIncludeInInventory(includeInInventory.isChecked());
            update();
        }
        if(redstoneOn.isChecked() != tileEntityKeeper.isRedstoneRequired()){
            tileEntityKeeper.setRedstoneRequired(redstoneOn.isChecked());
            update();
        }
        if(exactItem.isChecked() != tileEntityKeeper.isExactItem()){
            tileEntityKeeper.setExactItem(exactItem.isChecked());
            update();
        }
    }

    public void update(){
        ThingsOfNaturalEnergies.logger.error("Message occurred");
        tileEntityKeeper.getWorld().notifyBlockUpdate(tileEntityKeeper.getPos(), tileEntityKeeper.getWorld().getBlockState(tileEntityKeeper.getPos()), tileEntityKeeper.getWorld().getBlockState(tileEntityKeeper.getPos()), 3);
        tileEntityKeeper.getWorld().scheduleBlockUpdate(tileEntityKeeper.getPos(), tileEntityKeeper.getBlockType(), 0, 0);
        tileEntityKeeper.markDirty();
        TonePacketHandler.sendToServer(new GUIUpdatePacket(tileEntityKeeper));
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
        fontRenderer.drawString("Keeper", LABEL_XPOS, LABEL_YPOS, Color.darkGray.getRGB());
        this.renderHoveredToolTip(mouseX - guiLeft, mouseY - guiTop);

        for(ToneCheckbox tc : arrayOfThings){
            List<String> lines = new ArrayList<>();
            lines.add(tc.getText());


            if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)){
                lines.addAll(tc.getExtraInfo());
            }

            if(tc.isHovered()){
                this.drawHoveringText(lines, mouseX + 5 - guiLeft, mouseY - 5 - guiTop, fontRenderer);
            }
        }
    }

    @Override
    public void doSync(NBTTagCompound fromServer) {
        if(!fromServer.getString("key").equals("keeper")) return;

        NBTTagCompound coreLoc = fromServer.getCompoundTag("location");
        BlockPos loc = new BlockPos(coreLoc.getInteger("x"),
                coreLoc.getInteger("y"), coreLoc.getInteger("z"));
        if(!loc.equals(tileEntityKeeper.getPos())) return;

        includeInInventory.setIsChecked(fromServer.getBoolean("include"));
        exactItem.setIsChecked(fromServer.getBoolean("exact"));
        redstoneOn.setIsChecked(fromServer.getBoolean("redstone"));
        ThingsOfNaturalEnergies.logger.error("Updating info to have redstone: " + redstoneOn.isChecked());
    }
}
