package com.thelostnomad.tone.block.fluid;

import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import com.thelostnomad.tone.registry.ModFluids;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Random;

public class BlockTransmutationGas extends BlockFluidClassic {

    public BlockTransmutationGas() {
        super(ModFluids.transmutationGas, Material.WATER);
        setRegistryName("transmutation_gas");
        setUnlocalizedName(ThingsOfNaturalEnergies.MODID + "transmutation_gas");
    }

    @SideOnly(Side.CLIENT)
    void render() {
        ModelLoader.setCustomStateMapper(this, new StateMap.Builder().ignore(LEVEL).build());
    }


    @Override
    public void updateTick(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Random rand)
    {
        // Do nothing here
    }

}
