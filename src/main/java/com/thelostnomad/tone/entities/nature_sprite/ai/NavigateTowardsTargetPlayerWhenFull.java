package com.thelostnomad.tone.entities.nature_sprite.ai;

import com.google.common.base.Predicate;
import com.thelostnomad.tone.entities.nature_sprite.NatureSpriteEntity;
import com.thelostnomad.tone.util.annotation.SpriteAI;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

@SpriteAI(optionalArg=int.class)
public class NavigateTowardsTargetPlayerWhenFull extends NatureSpriteAI
{
    private final NatureSpriteEntity parentEntity;
    private int carryCapacity;

    public NavigateTowardsTargetPlayerWhenFull(NatureSpriteEntity ghast)
    {
        this(ghast, 1);
    }

    public int getImportance(){
        return 8;
    }

    @Override
    public double getActionCost() {
        return 0;
    }

    public NavigateTowardsTargetPlayerWhenFull(NatureSpriteEntity ghast, int carryCap){
        this.parentEntity = ghast;
        this.setMutexBits(1);
        this.carryCapacity = carryCap;
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        if(this.parentEntity.isResting()) return false;
        if(!canHoldMore() && parentEntity.flags.containsKey("targetPlayer") && parentEntity.getTargetPlayerIsOnline()){
            if(isTargetInventoryFull()) return false;
            return true;
        }
        return false;
    }

    public boolean isTargetInventoryFull(){
        for(int i = 0; i < getTargetPlayer().inventory.getSizeInventory(); i++){
            ItemStack stack = getTargetPlayer().inventory.getStackInSlot(i);
            if(stack.isEmpty()){
                return false;
            }
        }
        return true;
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

    public EntityPlayer getTargetPlayer(){
        List<EntityPlayer> target = parentEntity.world.getEntities(EntityPlayer.class, new Predicate<EntityPlayer>() {
            @Override
            public boolean apply(@Nullable EntityPlayer input) {
                return input.getDisplayName().equals(parentEntity.flags.get("targetPlayer"));
            }
        });
        return target.get(0);
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
        EntityPlayer player = getTargetPlayer();
        this.parentEntity.getMoveHelper().setMoveTo(player.getPosition().getX(), player.getPosition().getY() + 1, player.getPosition().getZ(), 1.0d);
    }
}
