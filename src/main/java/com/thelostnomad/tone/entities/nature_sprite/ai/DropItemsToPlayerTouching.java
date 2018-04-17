package com.thelostnomad.tone.entities.nature_sprite.ai;

import com.google.common.base.Predicate;
import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import com.thelostnomad.tone.entities.nature_sprite.NatureSpriteEntity;
import com.thelostnomad.tone.util.annotation.SpriteAI;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@SpriteAI(optionalArg=int.class)
public class DropItemsToPlayerTouching extends NatureSpriteAI
{
    private final NatureSpriteEntity parentEntity;
    private int carryCapacity;

    public DropItemsToPlayerTouching(NatureSpriteEntity ghast)
    {
        this(ghast, 1);
    }

    private List<EntityPlayer> getPlayersNearby(){
        List<Entity> list1 = this.parentEntity.world.getEntitiesWithinAABBExcludingEntity(this.parentEntity, parentEntity.getEntityBoundingBox().grow(0.75D));
        List<EntityPlayer> toReturn = new ArrayList<>();
        for(Entity e : list1){
            if(e instanceof EntityPlayer){
                toReturn.add((EntityPlayer) e);
            }
        }
        return toReturn;
    }

    public int getImportance(){
        return 10;
    }

    @Override
    public double getActionCost() {
        return 1.25;
    }

    public DropItemsToPlayerTouching(NatureSpriteEntity ghast, int carryCapacity){
        this.parentEntity = ghast;
        this.setMutexBits(4);
        this.carryCapacity = carryCapacity;
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        if(this.parentEntity.isResting()) return false;
        if(!parentEntity.flags.containsKey("targetPlayer")) return false;
        if(!parentEntity.getTargetPlayerIsOnline()) return false;
        if(isTargetInventoryFull()) return false;
        if(touchingTarget()) {
            return true;
        }
        return false;
    }

    public boolean touchingTarget(){
        for(EntityPlayer player : getPlayersNearby()){
            if(player.getDisplayName().equals(parentEntity.getTargetPlayer().getDisplayName())){
                return true;
            }
        }
        return false;
    }

    public boolean isTargetInventoryFull(){
        for(int i = 0; i < parentEntity.getTargetPlayer().inventory.getSizeInventory(); i++){
            ItemStack stack = parentEntity.getTargetPlayer().inventory.getStackInSlot(i);
            if(stack.isEmpty()){
                return false;
            }
        }
        return true;
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
        for(int i = 0; i < carryCapacity; i++){
            ItemStack held = parentEntity.getHeldItemstacks()[i];
            if(held == null || held.isEmpty()){
                continue;
            }
            boolean fits = parentEntity.getTargetPlayer().inventory.addItemStackToInventory(held);
            if(fits){
                parentEntity.setHeld(i, ItemStack.EMPTY);
                this.parentEntity.setStamina(this.parentEntity.getStamina() - getActionCost());
            }
        }
    }
}