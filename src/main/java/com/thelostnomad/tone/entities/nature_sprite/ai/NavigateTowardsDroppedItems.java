package com.thelostnomad.tone.entities.nature_sprite.ai;

import com.google.common.base.Predicate;
import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import com.thelostnomad.tone.entities.nature_sprite.NatureSpriteEntity;
import com.thelostnomad.tone.util.annotation.SpriteAI;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@SpriteAI(optionalArg=double.class)
public class NavigateTowardsDroppedItems extends NatureSpriteAI
{
    private final NatureSpriteEntity parentEntity;
    private double mvSpeed;

    public NavigateTowardsDroppedItems(NatureSpriteEntity ghast)
    {
        this(ghast, 1.0D);
    }

    public int getImportance(){
        return 6;
    }

    @Override
    public double getActionCost() {
        return 0;
    }

    public NavigateTowardsDroppedItems(NatureSpriteEntity ghast, double moveSpeed){
        this.parentEntity = ghast;
        this.setMutexBits(4);
        this.mvSpeed = moveSpeed;
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        if(this.parentEntity.isResting()) return false;
        ThingsOfNaturalEnergies.logger.error("Checking whether we should navigate");
        if(getClosestItem() != null){
            ThingsOfNaturalEnergies.logger.error("There's an item somewhere!");
            return true;
        }
        return false;
    }

    private EntityItem getClosestItem(){
        EntityItem closest = null;
        double distance = Double.MAX_VALUE;

        for(EntityItem ei : parentEntity.world.getEntities(EntityItem.class, new Predicate<EntityItem>() {
            @Override
            public boolean apply(@Nullable EntityItem input) {
                return input.getPosition().distanceSq(parentEntity.getPosition()) < 84;
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
        BlockPos target = getClosestItem().getPosition();
        this.parentEntity.getMoveHelper().setMoveTo(target.getX(), target.getY(), target.getZ(), this.mvSpeed);
    }
}
