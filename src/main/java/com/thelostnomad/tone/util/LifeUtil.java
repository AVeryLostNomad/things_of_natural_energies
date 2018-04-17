package com.thelostnomad.tone.util;

import net.minecraft.entity.EntityLiving;
import net.minecraft.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;

public class LifeUtil {

    public static double getLifeForEntity(EntityLiving entityLiving){
        float maxHp = entityLiving.getMaxHealth();
        float currentHp = entityLiving.getHealth();
        List<PotionEffect> potionsOn = new ArrayList<>(entityLiving.getActivePotionEffects());

        double lifeToReturn = (4000 * maxHp) + (1200 * currentHp) + (600 * potionsOn.size());

        if(entityLiving.hasCustomName()){
            //Wither or dragon or other boss, I think
            lifeToReturn *= 2; // That's right, two TIMES the amount.
        }

        return lifeToReturn;
    }

    public static void deductLifeFromEntity(EntityLiving entityLiving){
        entityLiving.setHealth(1F);
        for(PotionEffect p : entityLiving.getActivePotionEffects()){
            entityLiving.removeActivePotionEffect(p.getPotion());
        }
    }

}
