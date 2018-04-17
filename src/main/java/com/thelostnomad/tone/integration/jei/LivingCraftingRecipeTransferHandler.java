package com.thelostnomad.tone.integration.jei;

import com.thelostnomad.tone.block.container.ContainerLivingCraftingStation;
import com.thelostnomad.tone.network.MessageRecipeSync;
import com.thelostnomad.tone.network.TonePacketHandler;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class LivingCraftingRecipeTransferHandler implements IRecipeTransferHandler<ContainerLivingCraftingStation> {

    @Override
    public Class<ContainerLivingCraftingStation> getContainerClass() {
        return ContainerLivingCraftingStation.class;
    }

    public String getRecipeCategoryUid() {
        return VanillaRecipeCategoryUid.CRAFTING;
    }


    @Nullable
    @Override
    /**
     * @param container    the container to act on
     * @param recipeLayout the layout of the recipe, with information about the ingredients
     * @param player       the player, to do the slot manipulation
     * @param maxTransfer  if true, transfer as many items as possible. if false, transfer one set
     * @param doTransfer   if true, do the transfer. if false, check for errors but do not actually transfer the items
     * @return a recipe transfer error if the recipe can't be transferred. Return null on success.
     * @since JEI 2.20.0
     */
    public IRecipeTransferError transferRecipe(ContainerLivingCraftingStation container, IRecipeLayout recipeLayout, EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
        if(doTransfer){
            Map<Integer, IGuiIngredient<ItemStack>> inputs = (Map<Integer, IGuiIngredient<ItemStack>>) recipeLayout.getItemStacks().getGuiIngredients();
            NBTTagCompound recipe = new NBTTagCompound();
            JEIStackHelper helper = new JEIStackHelper();
            // Go through every slot, finding ones that are part of the craft matrix.
            for(Slot slot : container.inventorySlots){
                if (slot.inventory instanceof InventoryCrafting) {
                    IGuiIngredient<ItemStack> ingredient = inputs.get(slot.getSlotIndex() + 1);
                    if (ingredient != null) {
                        List<ItemStack> possibleItems = ingredient.getAllIngredients();
                        NBTTagList tags = new NBTTagList();
                        String ore = null;
                        if ((ore = helper.getOreDictEquivalent(possibleItems)) != null) {
                            NBTTagCompound tag = new NBTTagCompound();
                            tag.setString("ore", ore);
                            tags.appendTag(tag);
                        } else {
                            for (ItemStack is : possibleItems) {
                                NBTTagCompound tag = new NBTTagCompound();
                                is.writeToNBT(tag);
                                tags.appendTag(tag);
                            }
                        }
                        recipe.setTag("#" + slot.getSlotIndex(), tags);
                    }
                }
            }

            // update the recipe serverside
            TonePacketHandler.sendToServer(new MessageRecipeSync(recipe));

        }

        return null;
    }

}
