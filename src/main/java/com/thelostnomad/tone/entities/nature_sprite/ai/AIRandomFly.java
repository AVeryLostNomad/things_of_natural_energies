package com.thelostnomad.tone.entities.nature_sprite.ai;

import com.thelostnomad.tone.entities.nature_sprite.NatureSpriteEntity;
import com.thelostnomad.tone.util.MobUtil;
import com.thelostnomad.tone.util.annotation.SpriteAI;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityMoveHelper;

import java.util.Random;

@SpriteAI(optionalArg=long.class)
public class AIRandomFly extends NatureSpriteAI
{
    private final NatureSpriteEntity parentEntity;

    public AIRandomFly(NatureSpriteEntity ghast)
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
            return false;
        }
        if(parentEntity.isNavigatingDown()){
            return false;
        }
        if(parentEntity.flags.containsKey("gold")){
            return false;
        }
        if(this.parentEntity.posY > (MobUtil.getGroundHeight(parentEntity) + 5)){
            return false;
        }
        EntityMoveHelper entitymovehelper = this.parentEntity.getMoveHelper();

        if (!entitymovehelper.isUpdating())
        {
            return true;
        }
        else
        {
            double d0 = entitymovehelper.getX() - this.parentEntity.posX;
            double d1 = entitymovehelper.getY() - this.parentEntity.posY;
            double d2 = entitymovehelper.getZ() - this.parentEntity.posZ;
            double d3 = d0 * d0 + d1 * d1 + d2 * d2;
            return d3 < 1.0D || d3 > 500.0D;
        }
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean shouldContinueExecuting()
    {
        return false;
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
        Random random = this.parentEntity.getRNG();
        double d0 = this.parentEntity.posX + (double)((random.nextFloat() * 2.0F - 1.0F) * 4.0F);
        double d1 = this.parentEntity.posY + (double)((random.nextFloat() * 2.0F - 1.0F) * 4.0F);
        double d2 = this.parentEntity.posZ + (double)((random.nextFloat() * 2.0F - 1.0F) * 4.0F);
        if(d1 > (MobUtil.getGroundHeight(this.parentEntity) + 5)) d1 = this.parentEntity.posY;
        this.parentEntity.getMoveHelper().setMoveTo(d0, d1, d2, 1.0D);
    }

    @Override
    public int getImportance() {
        return 5;
    }

    @Override
    public double getActionCost() {
        return 0;
    }
}