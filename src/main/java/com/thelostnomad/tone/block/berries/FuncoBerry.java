package com.thelostnomad.tone.block.berries;

import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import com.thelostnomad.tone.registry.ModItems;
import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;

public class FuncoBerry extends BlockBerry {

    // Hastoberries increase a sentient tree's tickrate. Trees start at the default
    public FuncoBerry() {
        toSpawnOnBreak = ModItems.funcoBerryItem;
        breakString = "funcoberry";
        setUnlocalizedName(ThingsOfNaturalEnergies.MODID + ".berry_funco");     // Used for localization (en_US.lang)
        setRegistryName("berry_funco");
        setCreativeTab(ThingsOfNaturalEnergies.creativeTab);
    }

    @Override
    public Biome[] getThrivesIn() {
        return new Biome[]{Biomes.EXTREME_HILLS, Biomes.EXTREME_HILLS_EDGE, Biomes.EXTREME_HILLS_WITH_TREES, Biomes.FOREST_HILLS}; // Hastoberries are only obtainable in the desert
    }

}
