package com.thelostnomad.tone.integration;

import net.minecraft.item.ItemStack;

public interface IToneInventoryable {

    ItemStack pullItem(ItemStack target);

    boolean pushItem(ItemStack target); // Push an item into this inventory. Returning true if successful

}
