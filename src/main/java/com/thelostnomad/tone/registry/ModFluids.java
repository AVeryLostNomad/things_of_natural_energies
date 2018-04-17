package com.thelostnomad.tone.registry;

import com.thelostnomad.tone.fluids.FluidTransmutationGas;

public class ModFluids {

    public static FluidTransmutationGas transmutationGas;

    public static void registerFluids() {
        transmutationGas = new FluidTransmutationGas();
    }

}
