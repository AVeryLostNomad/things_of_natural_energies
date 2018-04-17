package com.thelostnomad.tone.item.tokens;

import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class TokenPullAll extends ItemToken {

    public TokenPullAll()
    {
        this.setMaxStackSize(1);
        this.setCreativeTab(CreativeTabs.MISC);   // the item will appear on the Miscellaneous tab in creative
        setUnlocalizedName(ThingsOfNaturalEnergies.MODID + ".token_pull_all");     // Used for localization (en_US.lang)
        setRegistryName("token_pull_all");        // The unique name (within your mod) that identifies this block
    }

}
