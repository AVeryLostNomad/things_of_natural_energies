package com.thelostnomad.tone.item.sprites;

import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import com.thelostnomad.tone.teisr.SpriteTEISR;
import net.minecraft.item.Item;

public class NatureSpriteItem extends Item {

    public NatureSpriteItem(String model){
        this.setMaxStackSize(1);
        this.setCreativeTab(ThingsOfNaturalEnergies.creativeTab);
        this.setTileEntityItemStackRenderer(new SpriteTEISR());
        setUnlocalizedName(ThingsOfNaturalEnergies.MODID + "." + model);     // Used for localization (en_US.lang)
        setRegistryName(model);        // The unique name (within your mod) that identifies this block
    }

}
