package com.thelostnomad.tone;

import com.thelostnomad.tone.registry.ModItems;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CreativeTabTone extends CreativeTabs {
    private final ItemStack sword;

    public CreativeTabTone() {
        super(ThingsOfNaturalEnergies.MODID);
        sword = new ItemStack(ModItems.glutoBerryItem, 1);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ItemStack getTabIconItem() {
        return sword;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void displayAllRelevantItems(final NonNullList<ItemStack> items) {
        items.add(sword.copy());
        super.displayAllRelevantItems(items);
    }
}