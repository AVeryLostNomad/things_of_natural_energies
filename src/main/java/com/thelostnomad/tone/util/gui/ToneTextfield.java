package com.thelostnomad.tone.util.gui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

import java.util.List;

public class ToneTextfield extends GuiTextField {

    private String hoverText;
    private List<String> extraInfo;

    public ToneTextfield(int componentId, FontRenderer fontrendererObj, int x, int y, int width, int height, String defaultText, String tooltip, List<String> extraInfoLines) {
        super(componentId, fontrendererObj, x, y, width, height);
        hoverText = tooltip;
        this.extraInfo = extraInfoLines;
        super.setText(defaultText);
    }

    public String getHoverText() {
        return hoverText;
    }

    public List<String> getExtraInfo() {
        return this.extraInfo;
    }

    public boolean inBounds(int mouseX, int mouseY){
        return mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
    }

}
