package com.thelostnomad.tone.block.gui;

import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import com.thelostnomad.tone.block.container.ContainerAcceptor;
import com.thelostnomad.tone.block.container.ContainerKeeper;
import com.thelostnomad.tone.block.tileentity.TEAcceptor;
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
import java.util.ArrayList;
import java.util.List;

public class GuiAcceptor extends GuiContainer implements SyncableGui {

    // This is the resource location for the background image for the GUI
    private static final ResourceLocation texture = new ResourceLocation("thingsofnaturalenergies", "textures/gui/acceptor.png");
    private TEAcceptor tileEntityAcceptor;

    ToneCheckbox redstoneOn;
    ToneCheckbox voidExcess;
    ToneCheckbox partialFit;

    ToneCheckbox[] arrayOfThings;

    public GuiAcceptor(InventoryPlayer invPlayer, TEAcceptor tile) {
        super(new ContainerAcceptor(invPlayer, tile));
        tileEntityAcceptor = tile;
        // Set the width and height of the gui.  Should match the size of the texture!
        xSize = 176;
        ySize = 133;
    }

    @Override
    public void initGui() {
        super.initGui();
        List<String> lines = new ArrayList<>();
        lines.add(TextFormatting.ITALIC + "If enabled, this acceptor");
        lines.add(TextFormatting.ITALIC + "will void items that do not");
        lines.add(TextFormatting.ITALIC + "fit into the tree.");
        voidExcess = new ToneCheckbox(0, 113 + guiLeft, 30 + guiTop, "Void Excess?", tileEntityAcceptor.getVoidExcess(), lines);
        this.addButton(voidExcess);
//
        List<String> linesRedstone = new ArrayList<>();
        linesRedstone.add(TextFormatting.ITALIC + "If enabled, this acceptor will");
        linesRedstone.add(TextFormatting.ITALIC + "only work when powered by");
        linesRedstone.add(TextFormatting.ITALIC + "redstone.");
        redstoneOn = new ToneCheckbox(1, 126 + guiLeft, 9 + guiTop, "Redstone Required?", tileEntityAcceptor.getRedstoneRequired(), linesRedstone);
        this.addButton(redstoneOn);
//
        List<String> linesExact = new ArrayList<>();
        linesExact.add(TextFormatting.ITALIC + "If enabled, this acceptor will");
        linesExact.add(TextFormatting.ITALIC + "only store items when the whole");
        linesExact.add(TextFormatting.ITALIC + "stack fits. If disabled,");
        linesExact.add(TextFormatting.ITALIC + "it will fit as many as possible.");
        partialFit = new ToneCheckbox(2, 44 + guiLeft, 27 + guiTop, "Partial Fit?", tileEntityAcceptor.getPartialFits(), linesExact);
        this.addButton(partialFit);
//
//        //82 18
        arrayOfThings = new ToneCheckbox[]{
                voidExcess,
                redstoneOn,
                partialFit
        };

        TonePacketHandler.sendToServer(new KindlyGiveMeAnUpdatedGUI());
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        if(partialFit.isChecked() != tileEntityAcceptor.getPartialFits()){
            tileEntityAcceptor.setPartialFits(partialFit.isChecked());
            update();
        }
        if(redstoneOn.isChecked() != tileEntityAcceptor.getRedstoneRequired()){
            tileEntityAcceptor.setRedstoneRequired(redstoneOn.isChecked());
            update();
        }
        if(voidExcess.isChecked() != tileEntityAcceptor.getVoidExcess()){
            tileEntityAcceptor.setVoidExcess(voidExcess.isChecked());
            update();
        }
    }

    public void update(){
        tileEntityAcceptor.getWorld().notifyBlockUpdate(tileEntityAcceptor.getPos(), tileEntityAcceptor.getWorld().getBlockState(tileEntityAcceptor.getPos()), tileEntityAcceptor.getWorld().getBlockState(tileEntityAcceptor.getPos()), 3);
        tileEntityAcceptor.getWorld().scheduleBlockUpdate(tileEntityAcceptor.getPos(), tileEntityAcceptor.getBlockType(), 0, 0);
        tileEntityAcceptor.markDirty();
        ThingsOfNaturalEnergies.logger.error("We're going here w/ " + tileEntityAcceptor.getVoidExcess());
        TonePacketHandler.sendToServer(new GUIUpdatePacket(tileEntityAcceptor));
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
        fontRenderer.drawString("Acceptor", LABEL_XPOS, LABEL_YPOS, Color.darkGray.getRGB());
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
        if(!fromServer.getString("key").equals("acceptor")) return;

        NBTTagCompound coreLoc = fromServer.getCompoundTag("location");
        BlockPos loc = new BlockPos(coreLoc.getInteger("x"),
                coreLoc.getInteger("y"), coreLoc.getInteger("z"));
        if(!loc.equals(tileEntityAcceptor.getPos())) return;

        voidExcess.setIsChecked(fromServer.getBoolean("void"));
        partialFit.setIsChecked(fromServer.getBoolean("partial"));
        redstoneOn.setIsChecked(fromServer.getBoolean("redstone"));
    }
}
