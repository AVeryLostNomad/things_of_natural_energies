package com.thelostnomad.tone.integration.jei;

import mezz.jei.api.*;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferRegistry;

@JEIPlugin
public class ToneJEIIntegration implements IModPlugin {

    public static IIngredientListOverlay jeiOverlay;
    public static IJeiHelpers jeiHelpers;
    public static IGuiHelper guiHelper;

    @Override
    public void register(IModRegistry registry) {
        jeiHelpers = registry.getJeiHelpers();
        guiHelper = jeiHelpers.getGuiHelper();

        IRecipeTransferRegistry recipeTransferRegistry = registry.getRecipeTransferRegistry();
        recipeTransferRegistry.addRecipeTransferHandler(new LivingCraftingRecipeTransferHandler(), VanillaRecipeCategoryUid.CRAFTING);
        registry.addAdvancedGuiHandlers(new MoveOutDaWayJEI());
    }

}
