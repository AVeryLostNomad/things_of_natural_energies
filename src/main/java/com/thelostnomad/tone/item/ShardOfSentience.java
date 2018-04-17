package com.thelostnomad.tone.item;

import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import net.minecraft.item.Item;

public class ShardOfSentience extends Item {

    public ShardOfSentience()
    {
        this.setMaxStackSize(64);
        this.setCreativeTab(ThingsOfNaturalEnergies.creativeTab);
        setUnlocalizedName(ThingsOfNaturalEnergies.MODID + ".shard_of_sentience");     // Used for localization (en_US.lang)
        setRegistryName("shard_of_sentience");        // The unique name (within your mod) that identifies this block
    }

}
