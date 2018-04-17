package com.thelostnomad.tone.block.storage_hollows;

import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import com.thelostnomad.tone.block.tileentity.TEStorageHollow;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class GargantuanStorageHollow extends BasicStorageHollow {

    public GargantuanStorageHollow(){
        super();
    }

    public void initialize(){
        setUnlocalizedName(ThingsOfNaturalEnergies.MODID + ".storagehollow_gargantuan");     // Used for localization (en_US.lang)
        setRegistryName("storagehollow_gargantuan");        // The unique name (within your mod) that identifies this block
        setCreativeTab(ThingsOfNaturalEnergies.creativeTab);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        TEStorageHollow e = new TEStorageHollow();
        e.setStorageLevel(TEStorageHollow.HollowType.GARGANTUAN);
        return e;
    }

}