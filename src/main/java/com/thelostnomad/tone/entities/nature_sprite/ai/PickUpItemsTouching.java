package com.thelostnomad.tone.entities.nature_sprite.ai;

import com.google.common.base.Predicate;
import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import com.thelostnomad.tone.entities.nature_sprite.NatureSpriteEntity;
import com.thelostnomad.tone.network.SpritePlayEffectPacket;
import com.thelostnomad.tone.network.SpriteSingPacket;
import com.thelostnomad.tone.network.TonePacketHandler;
import com.thelostnomad.tone.util.annotation.SpriteAI;
import com.thelostnomad.tone.util.crafting.StackUtil;
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

@SpriteAI(optionalArg = int.class)
public class PickUpItemsTouching extends NatureSpriteAI {
    private final NatureSpriteEntity parentEntity;
    private int carryCapacity;

    public PickUpItemsTouching(NatureSpriteEntity ghast) {
        this(ghast, 1);
    }

    private List<EntityItem> getItemsNearby() {
        List<Entity> list1 = this.parentEntity.world.getEntitiesWithinAABBExcludingEntity(this.parentEntity, parentEntity.getEntityBoundingBox().grow(1.5D));
        List<EntityItem> toReturn = new ArrayList<>();
        for (Entity e : list1) {
            if (e instanceof EntityItem) {
                toReturn.add((EntityItem) e);
            }
        }
        return toReturn;
    }

    public int getImportance() {
        return 9;
    }

    @Override
    public double getActionCost() {
        return 2;
    }

    public PickUpItemsTouching(NatureSpriteEntity ghast, int carryCapacity) {
        this.parentEntity = ghast;
        this.setMutexBits(4);
        this.carryCapacity = carryCapacity;
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute() {
        if (this.parentEntity.isResting()) return false;
        if (!getItemsNearby().isEmpty() && canHoldMore()) {
            ThingsOfNaturalEnergies.logger.error("I'd like to pick up this item.");
            return true;
        }
        return false;
    }

    private boolean canHoldMore() {
        for (int i = 0; i < this.carryCapacity; i++) {
            ItemStack stack = parentEntity.getHeldItemstacks()[i];
            if (stack == null) {
                return true;
            }
            if (stack.isEmpty()) {
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
    public boolean shouldContinueExecuting() {
        return false;
    }

    public int getFirstNonemptyStack() {
        for (int i = 0; i < this.carryCapacity; i++) {
            ItemStack stack = parentEntity.getHeldItemstacks()[i];
            if (stack == null) {
                return i;
            }
            if (stack.isEmpty()) {
                return i;
            }
        }
        return 0;
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting() {
        EntityItem ei = this.getItemsNearby().get(0);
        ItemStack is = ei.getItem();
        ItemStack[] held = parentEntity.getHeldItemstacks();

        int i;
        for (i = 0; i < carryCapacity; i++) {
            if (held[i] != null && !held[i].isEmpty()) {
                if (StackUtil.stacksEqual(held[i], is)) {
                    // We might be able to combine this stack into the one we're holding
                    int heldCount = held[i].getCount();
                    int thereCount=  is.getCount();
                    ThingsOfNaturalEnergies.logger.error("Stacks equal holding "  + heldCount + " and there " + thereCount);
                    if(heldCount + thereCount <= held[i].getMaxStackSize()){
                        // We can add all of it.
                        ThingsOfNaturalEnergies.logger.error("We can hold all of it");
                        held[i].setCount(heldCount + thereCount);
                        parentEntity.setHeldItemstacks(held);
                        this.parentEntity.setStamina(this.parentEntity.getStamina() - getActionCost());
                        ei.setDead();
                        return;
                    }
                    // We couldn't add all of it, but we can probably do some
                    if(heldCount != held[i].getMaxStackSize()){
                        ThingsOfNaturalEnergies.logger.error("We can hold some!");
                        is.setCount(thereCount - (held[i].getMaxStackSize() - heldCount));
                        held[i].setCount(held[i].getMaxStackSize());
                        parentEntity.setHeldItemstacks(held);
                        this.parentEntity.setStamina(this.parentEntity.getStamina() - getActionCost());
                        ei.setItem(is);
                    }
                }
            }else{
                ThingsOfNaturalEnergies.logger.error("Not holding anything");
                held[i] = is;
                ei.setDead();
            }
        }
        parentEntity.setHeldItemstacks(held);
        this.parentEntity.setStamina(this.parentEntity.getStamina() - getActionCost());
        if(!ei.isDead) ei.setItem(is);
    }
}
