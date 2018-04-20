package com.thelostnomad.tone.entities.nature_sprite.ai;

import com.thelostnomad.tone.entities.nature_sprite.NatureSpriteEntity;
import com.thelostnomad.tone.util.annotation.SpriteAI;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

@SpriteAI(optionalArg=int.class)
public class DropOffItemsAtChest extends NatureSpriteAI {
    private final NatureSpriteEntity parentEntity;
    private int carryCapacity;

    public DropOffItemsAtChest(NatureSpriteEntity ghast)
    {
        this(ghast, 1);
    }

    public int getImportance(){
        return 10;
    }

    @Override
    public double getActionCost() {
        return 0.25;
    }

    public DropOffItemsAtChest(NatureSpriteEntity ghast, int carryCapacity){
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
        if(!parentEntity.flags.containsKey("targetInventory")) return false;
        if(isTargetInventoryFull()) return false;
        if(touchingTarget()) {
            return true;
        }
        return false;
    }

    public TileEntity getTargetInventory(){
        return parentEntity.world.getTileEntity((BlockPos) parentEntity.flags.get("targetInventory"));
    }


    public boolean touchingTarget(){
        TileEntity target = getTargetInventory();
        if(target.getPos().distanceSq(parentEntity.getPosition()) <= 2D){
            return true;
        }
        return false;
    }

    public boolean isTargetInventoryFull(){
        TileEntity storageEntity = getTargetInventory();
        boolean hasItemCapability = storageEntity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
        if(!hasItemCapability) return true;
        InvWrapper helper = (InvWrapper) storageEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
        for(int i = 0; i < helper.getSlots(); i++){
            ItemStack stack = helper.getStackInSlot(i);
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

            InvWrapper helper = (InvWrapper) getTargetInventory().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
            int index = 0;
            ItemStack left = helper.insertItem(index, held, false);
            while(left != ItemStack.EMPTY && !left.isEmpty()){
                left = helper.insertItem(index++, left, false);
            }
            if(left.isEmpty()){
                parentEntity.setHeld(i, ItemStack.EMPTY);
                this.parentEntity.setStamina(this.parentEntity.getStamina() - getActionCost());
            }else{
                parentEntity.setHeld(i, left);
                this.parentEntity.setStamina(this.parentEntity.getStamina() - getActionCost());
            }
        }
    }
}
