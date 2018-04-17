package com.thelostnomad.tone.entities.nature_sprite.ai;

import com.thelostnomad.tone.entities.nature_sprite.NatureSpriteEntity;
import com.thelostnomad.tone.util.MobUtil;
import com.thelostnomad.tone.util.annotation.SpriteAI;
import net.minecraft.entity.ai.EntityAIBase;

import java.util.Random;

@SpriteAI(optionalArg=long.class)
public class AIReturnToGround extends NatureSpriteAI
{
    private final NatureSpriteEntity parentEntity;

    public AIReturnToGround(NatureSpriteEntity ghast)
    {
        this.parentEntity = ghast;
        this.setMutexBits(1);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        if(parentEntity.isResting()) return false;
        int limit = MobUtil.getGroundHeight(this.parentEntity);
        if(this.parentEntity.posY > (MobUtil.getGroundHeight(parentEntity) + 5)){
            return true;
        }else{
            parentEntity.setNavigatingDown(false);
            return false;
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
        if(parentEntity.getMoveHelper().isUpdating()){
            ((NatureSpriteEntity.SpriteMoveHelper) parentEntity.getMoveHelper()).setWait();
        }
        parentEntity.setNavigatingDown(true);
        Random random = this.parentEntity.getRNG();
        double d0 = this.parentEntity.posX + (double)((random.nextFloat() * 2.0F - 1.0F) * 2.0F);
        double d1 = this.parentEntity.posY - 0.2;
        double d2 = this.parentEntity.posZ + (double)((random.nextFloat() * 2.0F - 1.0F) * 2.0F);
        this.parentEntity.getMoveHelper().setMoveTo(d0, d1, d2, 1.0D);
    }

    @Override
    public int getImportance() {
        return 6;
    }

    @Override
    public double getActionCost() {
        return 0;
    }
}