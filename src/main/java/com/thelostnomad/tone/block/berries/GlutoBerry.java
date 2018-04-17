package com.thelostnomad.tone.block.berries;

import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import com.thelostnomad.tone.registry.ModItems;
import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;

public class GlutoBerry extends BlockBerry {

    // Hastoberries increase a sentient tree's tickrate. Trees start at the default
    public GlutoBerry() {
        toSpawnOnBreak = ModItems.glutoBerryItem;
        breakString = "glutoberry";
        setUnlocalizedName(ThingsOfNaturalEnergies.MODID + ".berry_gluto");     // Used for localization (en_US.lang)
        setRegistryName("berry_gluto");
        setCreativeTab(ThingsOfNaturalEnergies.creativeTab);
    }

    @Override
    public Biome[] getThrivesIn() {
        return new Biome[]{Biomes.TAIGA, Biomes.TAIGA_HILLS, Biomes.COLD_TAIGA, Biomes.COLD_TAIGA_HILLS, Biomes.MUTATED_TAIGA, Biomes.MUTATED_TAIGA_COLD}; // Hastoberries are only obtainable in the desert
    }

}
