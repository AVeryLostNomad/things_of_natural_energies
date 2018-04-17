package com.thelostnomad.tone.block.gui;

import com.google.common.base.Predicate;
import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import com.thelostnomad.tone.block.container.ContainerFocusPusher;
import com.thelostnomad.tone.block.container.ContainerKeeper;
import com.thelostnomad.tone.block.tileentity.TEFocusPusher;
import com.thelostnomad.tone.block.tileentity.TEKeeper;
import com.thelostnomad.tone.network.GUIUpdatePacket;
import com.thelostnomad.tone.network.KindlyGiveMeAnUpdatedGUI;
import com.thelostnomad.tone.network.TonePacketHandler;
import com.thelostnomad.tone.util.gui.SyncableGui;
import com.thelostnomad.tone.util.gui.ToneCheckbox;
import com.thelostnomad.tone.util.gui.ToneTextfield;
import mezz.jei.config.Config;
import mezz.jei.config.SessionData;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import javax.annotation.Nullable;
import javax.xml.soap.Text;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiFocusPusher extends GuiContainer implements SyncableGui {

    // This is the resource location for the background image for the GUI
    private static final ResourceLocation texture = new ResourceLocation("thingsofnaturalenergies", "textures/gui/focus_pusher.png");
    private TEFocusPusher tileEntityFocusPusher;

    ToneTextfield slotField;
    ToneTextfield rateField;
    ToneCheckbox redstoneOn;
    ToneCheckbox exactItem;
    ToneCheckbox pushIntoOtherSlots;

    ToneCheckbox[] arrayOfThings;
    ToneTextfield[] arrayOfTextboxes;

    public GuiFocusPusher(InventoryPlayer invPlayer, TEFocusPusher tile) {
        super(new ContainerFocusPusher(invPlayer, tile));
        tileEntityFocusPusher = tile;
        // Set the width and height of the gui.  Should match the size of the texture!
        xSize = 176;
        ySize = 133;
    }

    @Override
    public void initGui() {
        super.initGui();
        List<String> linesRedstone = new ArrayList<>();
        linesRedstone.add(TextFormatting.ITALIC + "If enabled, this pusher will");
        linesRedstone.add(TextFormatting.ITALIC + "only work when powered by");
        linesRedstone.add(TextFormatting.ITALIC + "redstone.");
        redstoneOn = new ToneCheckbox(1, 45 + guiLeft, 35 + guiTop, "Redstone Required?", tileEntityFocusPusher.getRedstoneRequired(), linesRedstone);
        this.addButton(redstoneOn);

        List<String> linesExact = new ArrayList<>();
        linesExact.add(TextFormatting.ITALIC + "If enabled, this pusher will");
        linesExact.add(TextFormatting.ITALIC + "only produce exact copies of");
        linesExact.add(TextFormatting.ITALIC + "the left item. If disabled,");
        linesExact.add(TextFormatting.ITALIC + "NBT will not be considered.");
        exactItem = new ToneCheckbox(2, 27 + guiLeft, 35 + guiTop, "Exact Item?", tileEntityFocusPusher.getExactItem(), linesExact);
        this.addButton(exactItem);

        List<String> linesOtherSlot = new ArrayList<>();
        linesOtherSlot.add(TextFormatting.ITALIC + "If enabled, this focus pusher");
        linesOtherSlot.add(TextFormatting.ITALIC + "will push to other slots " + TextFormatting.BOLD + "after");
        linesOtherSlot.add(TextFormatting.ITALIC + "first trying to push to the target");
        linesOtherSlot.add(TextFormatting.ITALIC + "slot. If disabled, it will " + TextFormatting.BOLD + "only");
        linesOtherSlot.add(TextFormatting.ITALIC + "push to the target slot.");
        this.addButton(pushIntoOtherSlots);

        List<String> linesSlot = new ArrayList<>();
        linesSlot.add(TextFormatting.ITALIC + "What slot should this pusher");
        linesSlot.add(TextFormatting.ITALIC + "attempt to insert into?");
        slotField = new ToneTextfield(3, this.fontRenderer, 104, 10, 30, 10, tileEntityFocusPusher.getSlot().toString(), "Slot?", linesSlot);
        slotField.setValidator(new Predicate<String>() {
            @Override
            public boolean apply(@Nullable String input) {
                try{
                    Integer i = Integer.parseInt(input);
                    if(i >= 0 && i <= 64) return true;
                }catch(Exception e){
                    if(input.isEmpty()){
                        slotField.setText("0");
                        return true; // Allow for the string to be empty
                    }
                    return false;
                }
                return false;
            }
        });

        List<String> linesRate = new ArrayList<>();
        linesRate.add(TextFormatting.ITALIC + "What rate should this pusher");
        linesRate.add(TextFormatting.ITALIC + "attempt to push at?");
        rateField = new ToneTextfield(4, this.fontRenderer, 104, 35, 30, 10, tileEntityFocusPusher.getRate().toString(), "Rate?", linesRate);
        rateField.setValidator(new Predicate<String>() {
            @Override
            public boolean apply(@Nullable String input) {
                try{
                    Integer i = Integer.parseInt(input);
                    if(i >= 0 && i <= 64) return true;
                }catch(Exception e){
                    if(input.isEmpty()){
                        rateField.setText("0");
                        return true; // Allow for the string to be empty
                    }
                    return false;
                }
                return false;
            }
        });

        //82 18
        arrayOfThings = new ToneCheckbox[]{
            redstoneOn,
            exactItem,
            pushIntoOtherSlots
        };

        arrayOfTextboxes = new ToneTextfield[]{
            slotField,
            rateField
        };

        TonePacketHandler.sendToServer(new KindlyGiveMeAnUpdatedGUI());
    }

    public void handleKeyboardInput() throws IOException
    {
        char c0 = Keyboard.getEventCharacter();
        int eventKey = Keyboard.getEventKey();

        if (slotField.isFocused()){
            slotField.textboxKeyTyped(c0, eventKey);
        }

        if(rateField.isFocused()){
            rateField.textboxKeyTyped(c0, eventKey);
        }

        super.handleKeyboardInput();
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int i = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int j = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

        boolean slotClicked = this.slotField.inBounds(i - guiLeft, j - guiTop);
        this.slotField.setFocused(slotClicked);
        boolean rateClicked = this.rateField.inBounds(i - guiLeft, j - guiTop);
        this.rateField.setFocused(rateClicked);

        if(pushIntoOtherSlots.isChecked() != tileEntityFocusPusher.getPushIntoOtherSlots()){
            tileEntityFocusPusher.setPushIntoOtherSlots(pushIntoOtherSlots.isChecked());
            update();
        }
        if(redstoneOn.isChecked() != tileEntityFocusPusher.getRedstoneRequired()){
            tileEntityFocusPusher.setRedstoneRequired(redstoneOn.isChecked());
            update();
        }
        if(exactItem.isChecked() != tileEntityFocusPusher.getExactItem()){
            tileEntityFocusPusher.setExactItem(exactItem.isChecked());
            update();
        }
        if(!slotField.getText().equals(tileEntityFocusPusher.getSlot().toString())){
            if(slotField.getText().isEmpty()) slotField.setText("0");
            tileEntityFocusPusher.setSlot(Integer.parseInt(slotField.getText()));
            update();
        }
        if(!rateField.getText().equals(tileEntityFocusPusher.getRate().toString())){
            if(rateField.getText().isEmpty()) rateField.setText("0");
            tileEntityFocusPusher.setRate(Integer.parseInt(rateField.getText()));
            update();
        }
    }

    public void update(){
        ThingsOfNaturalEnergies.logger.error("Message occurred");
        tileEntityFocusPusher.getWorld().notifyBlockUpdate(tileEntityFocusPusher.getPos(), tileEntityFocusPusher.getWorld().getBlockState(tileEntityFocusPusher.getPos()), tileEntityFocusPusher.getWorld().getBlockState(tileEntityFocusPusher.getPos()), 3);
        tileEntityFocusPusher.getWorld().scheduleBlockUpdate(tileEntityFocusPusher.getPos(), tileEntityFocusPusher.getBlockType(), 0, 0);
        tileEntityFocusPusher.markDirty();
        TonePacketHandler.sendToServer(new GUIUpdatePacket(tileEntityFocusPusher));
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
        fontRenderer.drawString("Focus Pusher", LABEL_XPOS, LABEL_YPOS, Color.darkGray.getRGB());
        this.renderHoveredToolTip(mouseX - guiLeft, mouseY - guiTop);

        for(ToneTextfield tf : arrayOfTextboxes){
            tf.drawTextBox();

            List<String> lines = new ArrayList<>();
            lines.add(tf.getHoverText());

            if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)){
                lines.addAll(tf.getExtraInfo());
            }

            if(tf.inBounds(mouseX - guiLeft, mouseY - guiTop)){
                this.drawHoveringText(lines, mouseX + 5 - guiLeft, mouseY - 5 - guiTop, fontRenderer);
            }
        }

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
        if(!fromServer.getString("key").equals("focus_pusher")) return;

        NBTTagCompound coreLoc = fromServer.getCompoundTag("location");
        BlockPos loc = new BlockPos(coreLoc.getInteger("x"),
                coreLoc.getInteger("y"), coreLoc.getInteger("z"));
        if(!loc.equals(tileEntityFocusPusher.getPos())) return;

        slotField.setText(String.valueOf(fromServer.getInteger("Slot")));
        rateField.setText(String.valueOf(fromServer.getInteger("Rate")));
        exactItem.setIsChecked(fromServer.getBoolean("exact"));
        redstoneOn.setIsChecked(fromServer.getBoolean("redstone"));
        pushIntoOtherSlots.setIsChecked(fromServer.getBoolean("OtherSlots"));
    }
}
