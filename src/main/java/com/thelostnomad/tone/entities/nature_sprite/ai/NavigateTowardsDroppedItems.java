package com.thelostnomad.tone.entities.nature_sprite.ai;

import com.google.common.base.Predicate;
import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import com.thelostnomad.tone.entities.nature_sprite.NatureSpriteEntity;
import com.thelostnomad.tone.registry.ModItems;
import com.thelostnomad.tone.util.annotation.SpriteAI;
import com.thelostnomad.tone.util.crafting.StackUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@SpriteAI(optionalArg=int.class)
public class NavigateTowardsDroppedItems extends NatureSpriteAI
{
    private final NatureSpriteEntity parentEntity;
    private int carryCapacity;

    public NavigateTowardsDroppedItems(NatureSpriteEntity ghast)
    {
        this(ghast, 1);
    }

    public int getImportance(){
        return 6;
    }

    @Override
    public double getActionCost() {
        return 0;
    }

    public NavigateTowardsDroppedItems(NatureSpriteEntity ghast, int carryCap){
        this.parentEntity = ghast;
        this.setMutexBits(1);
        this.carryCapacity = carryCap;
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        if(this.parentEntity.isResting()){
            if(parentEntity.flags.containsKey("collecting")) parentEntity.flags.remove("collecting");
            return false;
        }
        if(getClosestItem() != null && canHoldMore()){
            parentEntity.flags.put("collecting", true);
            return true;
        }
        if(parentEntity.flags.containsKey("collecting")) parentEntity.flags.remove("collecting");
        return false;
    }

    private boolean canHoldMore(){
        for(int i = 0; i < this.carryCapacity; i++){
            ItemStack stack = parentEntity.getHeldItemstacks()[i];
            if(stack == null){
                return true;
            }
            if(stack.isEmpty()){
                return true;
            }
            if(getClosestItem() != null){
                if(StackUtil.stacksEqual(stack, getClosestItem().getItem())){
                    if(stack.getCount() + getClosestItem().getItem().getCount() < stack.getMaxStackSize()){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private EntityItem getClosestItem(){
        EntityItem closest = null;
        double distance = Double.MAX_VALUE;

        for(EntityItem ei : parentEntity.world.getEntities(EntityItem.class, new Predicate<EntityItem>() {
            @Override
            public boolean apply(@Nullable EntityItem input) {
                return input.getPosition().distanceSq(parentEntity.getPosition()) < 84 && input.getItem().getItem() != ModItems.shardOfSentience;
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
        this.parentEntity.getMoveHelper().setMoveTo(getClosestItem().posX, getClosestItem().posY + 0.75, getClosestItem().posZ, 1.75d);
    }
}
