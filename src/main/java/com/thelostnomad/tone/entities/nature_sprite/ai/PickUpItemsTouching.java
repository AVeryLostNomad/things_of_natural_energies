package com.thelostnomad.tone.entities.nature_sprite.ai;

import com.google.common.base.Predicate;
import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import com.thelostnomad.tone.entities.nature_sprite.NatureSpriteEntity;
import com.thelostnomad.tone.network.SpritePlayEffectPacket;
import com.thelostnomad.tone.network.SpriteSingPacket;
import com.thelostnomad.tone.network.TonePacketHandler;
import com.thelostnomad.tone.util.annotation.SpriteAI;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@SpriteAI(optionalArg=int.class)
public class PickUpItemsTouching extends NatureSpriteAI
{
    private final NatureSpriteEntity parentEntity;
    private int carryCapacity;

    public PickUpItemsTouching(NatureSpriteEntity ghast)
    {
        this(ghast, 1);
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
        return 9;
    }

    @Override
    public double getActionCost() {
        return 2;
    }

    public PickUpItemsTouching(NatureSpriteEntity ghast, int carryCapacity){
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
        if(!getItemsNearby().isEmpty() && canHoldMore()){
            ThingsOfNaturalEnergies.logger.error("I can hold more and there are items near here");
            return true;
        }
        return false;
    }

    private boolean canHoldMore(){
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
            ThingsOfNaturalEnergies.logger.error("I picked up an item!");
            ei.setDead();
            break;
        }
    }
}
