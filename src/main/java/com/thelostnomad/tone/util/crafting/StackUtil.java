package com.thelostnomad.tone.util.crafting;

import net.minecraft.item.ItemStack;

public class StackUtil {

    public static boolean stacksEqual(ItemStack a, ItemStack b){
        ItemStack one = a.copy();
        one.setCount(1);
        ItemStack two = b.copy();
        two.setCount(1);
        return ItemStack.areItemStacksEqual(one, two);
    }

    public static boolean stacksShallowEqual(ItemStack a, ItemStack b){
        return a.getItem() == b.getItem();
    }

    public static ItemStack templateStack(ItemStack in){
        ItemStack toReturn = in.copy();
        toReturn.setCount(1);
        return toReturn;
    }

}
