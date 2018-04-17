package com.thelostnomad.tone.entities.nature_sprite.ai;

import com.thelostnomad.tone.entities.nature_sprite.NatureSpriteEntity;
import com.thelostnomad.tone.util.annotation.SpriteAI;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

@SpriteAI(optionalArg=int.class)
public class DropOffItemsWithPlayer extends NatureSpriteAI
{
    private final NatureSpriteEntity parentEntity;
    private final int carryCapacity;

    public DropOffItemsWithPlayer(NatureSpriteEntity ghast)
    {
        this(ghast, 1);
    }

    public DropOffItemsWithPlayer(NatureSpriteEntity ghast, int carryCapacity){
        this.parentEntity = ghast;
        this.carryCapacity = carryCapacity;
    }

    private List<EntityItem> getItemsNearby(){
        List<Entity> list1 = this.parentEntity.world.getEntitiesWithinAABBExcludingEntity(this.parentEntity, parentEntity.getEntityBoundingBox().grow(0.25D));
        List<EntityItem> toReturn = new ArrayList<>();
        for(Entity e : list1){
            if(e instanceof EntityItem){
                toReturn.add((EntityItem) e);
            }
        }
        return toReturn;
    }

    public int getImportance(){
        return 7;
    }

    @Override
    public double getActionCost() {
        return 0;
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        if(this.parentEntity.isResting()) return false;
//        if(this.parentEntity.)
//        if(!getItemsNearby().isEmpty() && canHoldMore()){
//            return true;
//        }
        return false;
    }

    private boolean inventoryFull(){
        for(int i = 0; i < this.carryCapacity; i++){
            ItemStack stack = parentEntity.getHeldItemstacks()[i];
            if(stack.isEmpty()){
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean shouldContinueExecuting()
    {
        return false;
    }

    public int getFirstNonemptyStack(){
        for(int i = 0; i < this.carryCapacity; i++){
            ItemStack stack = parentEntity.getHeldItemstacks()[i];
            if(stack.isEmpty()){
                return i;
            }
        }
        return 0;
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
        for(EntityItem ei : this.getItemsNearby()){
            ItemStack is = ei.getItem();
            ItemStack[] held = parentEntity.getHeldItemstacks();
            held[getFirstNonemptyStack()] = is;
            parentEntity.setHeldItemstacks(held);
            ei.setDead();
            break;
        }
    }
}
