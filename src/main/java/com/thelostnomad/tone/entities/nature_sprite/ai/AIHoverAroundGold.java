package com.thelostnomad.tone.entities.nature_sprite.ai;

import com.google.common.base.Predicate;
import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import com.thelostnomad.tone.entities.nature_sprite.NatureSpriteEntity;
import com.thelostnomad.tone.util.annotation.SpriteAI;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;

import javax.annotation.Nullable;
import java.util.Random;

@SpriteAI(optionalArg=long.class)
public class AIHoverAroundGold extends NatureSpriteAI
{
    private final NatureSpriteEntity parentEntity;

    public AIHoverAroundGold(NatureSpriteEntity ghast)
    {
        this.parentEntity = ghast;
        this.setMutexBits(1);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        if(parentEntity.isResting()){
            if(parentEntity.flags.containsKey("gold")) parentEntity.flags.remove("gold");
            return false;
        }
        if(parentEntity.flags.containsKey("collecting")){
            if(parentEntity.flags.containsKey("gold")) parentEntity.flags.remove("gold");
            return false;
        }
        if(getClosestItem() == null){
            if(parentEntity.flags.containsKey("gold")) parentEntity.flags.remove("gold");
            return false;
        }
        EntityMoveHelper entitymovehelper = this.parentEntity.getMoveHelper();

        parentEntity.flags.put("gold", true);
        return true;
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean shouldContinueExecuting()
    {
        return false;
    }

    private EntityItem getClosestItem(){
        EntityItem closest = null;
        double distance = Double.MAX_VALUE;

        for(EntityItem ei : parentEntity.world.getEntities(EntityItem.class, new Predicate<EntityItem>() {
            @Override
            public boolean apply(@Nullable EntityItem input) {
                return input.getPosition().distanceSq(parentEntity.getPosition()) < 84 && input.getItem().getItem() == Items.GOLD_INGOT;
            }
        })){
            if(ei.getPosition().distanceSq(this.parentEntity.getPosition()) < distance){
                closest = ei;
                distance = ei.getPosition().distanceSq(this.parentEntity.getPosition());
            }
        }
        return closest;
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
        Random random = this.parentEntity.getRNG();
        double d0 = getClosestItem().getPosition().getX() + (double)((random.nextFloat() * 2.0F - 1.0F) * 2.0F);
        double d1 = getClosestItem().getPosition().getY() + (double)((random.nextFloat() * 2.0F - 1.0F) * 2.0F) + 1;
        double d2 = getClosestItem().getPosition().getZ() + (double)((random.nextFloat() * 2.0F - 1.0F) * 2.0F);
        this.parentEntity.getMoveHelper().setMoveTo(d0, d1, d2, 2.0D);
    }

    @Override
    public int getImportance() {
        return 8;
    }

    @Override
    public double getActionCost() {
        return 0;
    }
}