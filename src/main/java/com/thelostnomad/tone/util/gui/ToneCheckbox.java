package com.thelostnomad.tone.util.gui;

import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.fml.client.config.GuiCheckBox;
import net.minecraftforge.fml.client.config.GuiUtils;

import java.util.List;

public class ToneCheckbox extends GuiCheckBox {

    private String hoverText;
    private List<String> extraInfo;

    public ToneCheckbox(int id, int xPos, int yPos, String displayString, boolean isChecked, List<String> extraInfoLines) {
        super(id, xPos, yPos, "", isChecked);
        hoverText = displayString;
        this.extraInfo = extraInfoLines;
        this.setIsChecked(isChecked);
    }

    public String getText() {
        return hoverText;
    }

    public List<String> getExtraInfo() {
        return this.extraInfo;
    }

    public boolean inBounds(int mouseX, int mouseY){
        return mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
    }

    public boolean isHovered(){
        return this.hovered;
    }

}
