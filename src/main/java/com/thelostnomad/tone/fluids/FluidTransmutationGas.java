package com.thelostnomad.tone.fluids;

import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import java.awt.*;

public class FluidTransmutationGas extends Fluid {


    public FluidTransmutationGas() {
        super("transmutation_gas", new ResourceLocation(ThingsOfNaturalEnergies.MODID, "fluids/transmutation_still"), null, Color.GREEN);
        setGaseous(true);
        setViscosity(1000);
        setLuminosity(10);
        setDensity(-800);
        FluidRegistry.registerFluid(this);
    }


}
