package com.thelostnomad.tone.block.berries;

import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import com.thelostnomad.tone.registry.ModItems;
import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;

// Rezzoberry only comes from the nether. That's it.
public class RezzoBerry extends BlockBerry {

    // Hastoberries increase a sentient tree's tickrate. Trees start at the default
    public RezzoBerry() {
        toSpawnOnBreak = ModItems.rezzoBerryItem;
        breakString = "rezzoberry";
        setUnlocalizedName(ThingsOfNaturalEnergies.MODID + ".berry_rezzo");     // Used for localization (en_US.lang)
        setRegistryName("berry_rezzo");
        setCreativeTab(ThingsOfNaturalEnergies.creativeTab);
    }

    @Override
    public Biome[] getThrivesIn() {
        return new Biome[]{Biomes.HELL}; // Hastoberries are only obtainable in the desert
    }

}

