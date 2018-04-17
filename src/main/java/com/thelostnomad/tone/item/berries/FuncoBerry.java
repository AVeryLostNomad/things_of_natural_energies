package com.thelostnomad.tone.item.berries;

import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

public class FuncoBerry extends ItemFood {

    public FuncoBerry()
    {
        super(1, 0.1f, false);
        this.setMaxStackSize(16);
        this.setCreativeTab(ThingsOfNaturalEnergies.creativeTab);   // the item will appear on the Miscellaneous tab in creative
        setUnlocalizedName(ThingsOfNaturalEnergies.MODID + ".berry_funco_item");     // Used for localization (en_US.lang)
        setRegistryName("berry_funco_item");        // The unique name (within your mod) that identifies this block
    }

    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving)
    {
        if (entityLiving instanceof EntityPlayer)
        {
            EntityPlayer entityplayer = (EntityPlayer)entityLiving;
            entityplayer.addPotionEffect(new PotionEffect(Potion.getPotionById(3), 600, 2));
        }

        return super.onItemUseFinish(stack, worldIn, entityLiving);
    }

}
