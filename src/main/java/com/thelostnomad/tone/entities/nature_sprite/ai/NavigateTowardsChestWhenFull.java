package com.thelostnomad.tone.entities.nature_sprite.ai;

import com.google.common.base.Predicate;
import com.thelostnomad.tone.entities.nature_sprite.NatureSpriteEntity;
import com.thelostnomad.tone.util.annotation.SpriteAI;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nullable;
import java.util.List;

@SpriteAI(optionalArg=int.class)
public class NavigateTowardsChestWhenFull extends NatureSpriteAI
{
    private final NatureSpriteEntity parentEntity;
    private int carryCapacity;

    public NavigateTowardsChestWhenFull(NatureSpriteEntity ghast)
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

    public NavigateTowardsChestWhenFull(NatureSpriteEntity ghast, int carryCap){
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
        if(!canHoldMore() && parentEntity.flags.containsKey("targetInventory")){
            if(isTargetInventoryFull()) return false;
            if(parentEntity.flags.containsKey("collecting")) return false;
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

    public TileEntity getTargetInventory(){
        return parentEntity.world.getTileEntity((BlockPos) parentEntity.flags.get("targetInventory"));
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
        TileEntity player = getTargetInventory();
        this.parentEntity.getMoveHelper().setMoveTo(player.getPos().getX(), player.getPos().getY() + 1, player.getPos().getZ(), 1.0d);
    }
}
