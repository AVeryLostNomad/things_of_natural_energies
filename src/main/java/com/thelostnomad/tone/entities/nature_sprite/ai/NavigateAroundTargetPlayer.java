package com.thelostnomad.tone.entities.nature_sprite.ai;

import com.thelostnomad.tone.entities.nature_sprite.NatureSpriteEntity;
import com.thelostnomad.tone.util.MobUtil;
import com.thelostnomad.tone.util.annotation.SpriteAI;
import net.minecraft.entity.ai.EntityMoveHelper;

import java.util.Random;

@SpriteAI(optionalArg=long.class)
public class NavigateAroundTargetPlayer extends NatureSpriteAI
{
    private final NatureSpriteEntity parentEntity;

    public NavigateAroundTargetPlayer(NatureSpriteEntity ghast)
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
        if(parentEntity.flags.containsKey("collecting")) return false;
        if(!parentEntity.flags.containsKey("targetPlayer") || !parentEntity.getTargetPlayerIsOnline()){
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
        double d0 = parentEntity.getTargetPlayer().getPosition().getX() + (double)((random.nextFloat() * 2.0F - 1.0F) * 2.0F);
        double d1 = parentEntity.getTargetPlayer().getPosition().getY() + (double)((random.nextFloat() * 2.0F - 1.0F) * 2.0F) + 1;
        double d2 = parentEntity.getTargetPlayer().getPosition().getZ() + (double)((random.nextFloat() * 2.0F - 1.0F) * 2.0F);
        this.parentEntity.getMoveHelper().setMoveTo(d0, d1, d2, 2.0D);
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