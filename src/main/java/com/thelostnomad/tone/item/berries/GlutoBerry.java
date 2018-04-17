package com.thelostnomad.tone.item.berries;

import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class GlutoBerry extends ItemFood {

    public GlutoBerry()
    {
        super(1, 10.0f, false);
        this.setMaxStackSize(16);
        this.setCreativeTab(ThingsOfNaturalEnergies.creativeTab);   // the item will appear on the Miscellaneous tab in creative
        setUnlocalizedName(ThingsOfNaturalEnergies.MODID + ".berry_gluto_item");     // Used for localization (en_US.lang)
        setRegistryName("berry_gluto_item");        // The unique name (within your mod) that identifies this block
    }

    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving)
    {
        if(entityLiving instanceof EntityPlayer){
            EntityPlayer ep = (EntityPlayer) entityLiving;
            ep.getFoodStats().setFoodSaturationLevel(10F);
            ep.getFoodStats().setFoodLevel(20);
        }
        return super.onItemUseFinish(stack, worldIn, entityLiving);
    }

}
