package com.thelostnomad.tone.block.berries;

import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import com.thelostnomad.tone.registry.ModItems;
import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;

public class HastoBerry extends BlockBerry {

    // Hastoberries increase a sentient tree's tickrate. Trees start at the default
    public HastoBerry() {
        toSpawnOnBreak = ModItems.hastoBerryItem;
        breakString = "hastoberry";
        setUnlocalizedName(ThingsOfNaturalEnergies.MODID + ".berry_hasto");     // Used for localization (en_US.lang)
        setRegistryName("berry_hasto");
        setCreativeTab(ThingsOfNaturalEnergies.creativeTab);
    }

    @Override
    public Biome[] getThrivesIn() {
        return new Biome[]{Biomes.DESERT, Biomes.DESERT_HILLS}; // Hastoberries are only obtainable in the desert
    }

}
