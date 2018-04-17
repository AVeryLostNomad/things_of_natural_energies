package com.thelostnomad.tone.integration.jei;

import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import com.thelostnomad.tone.block.gui.GuiLivingCraftingStation;
import mezz.jei.api.gui.IAdvancedGuiHandler;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MoveOutDaWayJEI implements IAdvancedGuiHandler<GuiLivingCraftingStation> {

    @Override
    public Class<GuiLivingCraftingStation> getGuiContainerClass() {
        return GuiLivingCraftingStation.class;
    }

    @Override
    public List<Rectangle> getGuiExtraAreas(GuiLivingCraftingStation guiContainer) {
        List<Rectangle> tabBoxes = new ArrayList<>();

        int leftOffset = guiContainer.getGuiLeft();
        int upOffset = guiContainer.getGuiTop();

        Rectangle toReturn = new Rectangle(0 + leftOffset, 0 + upOffset, 175, 165);
        tabBoxes.add(toReturn);
        Rectangle rectangle = new Rectangle(175 + leftOffset, 16 + upOffset, 86, 126);
        tabBoxes.add(rectangle);

        return tabBoxes;
    }

    @Nullable
    @Override
    public Object getIngredientUnderMouse(GuiLivingCraftingStation guiContainer, int mouseX, int mouseY) {
        return null;
    }

}
