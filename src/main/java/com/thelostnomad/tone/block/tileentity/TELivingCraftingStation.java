package com.thelostnomad.tone.block.tileentity;

import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import com.thelostnomad.tone.util.TreeUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;

// Much of the inspiration for this particular class comes from the TileBench class of Vanhal's
// Just another crafting Bench mod. While I did modify it significantly, his mod is released under the
// COFH don't be a jerk license.

/*
I explicitly copied the portions of code dealing with the NonNullList of ItemStack saving and reading
The rest was created by myself, without looking at his source.
 */
public class TELivingCraftingStation extends TileEntity implements IInventory {

    public static final String NAME = "tone_living_crafting_station";
    private BlockPos coreLocation = null;

    private NonNullList<ItemStack> slots;

    public TELivingCraftingStation() {
        slots = NonNullList.<ItemStack>withSize(12, ItemStack.EMPTY);
    }

    @Override
    public final void readFromNBT(NBTTagCompound nbt) {
        slots = NonNullList.<ItemStack>withSize(this.slots.size(), ItemStack.EMPTY);
        super.readFromNBT(nbt);

        NBTTagCompound coreLoc = nbt.getCompoundTag("coreLocation");
        coreLocation = new BlockPos(coreLoc.getInteger("x"),
                coreLoc.getInteger("y"), coreLoc.getInteger("z"));
        if(nbt.hasKey("Items")){
            ItemStackHelper.loadAllItems(nbt, this.slots);
        }
    }

    public void setCoreLocation(BlockPos pos){
        this.coreLocation = pos;
    }

    public BlockPos getCoreLocation() {
        return coreLocation;
    }

    // We've been shoved this old item.
    public boolean overStack(int slot, ItemStack oldStack){
        // What do
        if(slot == 10){
            // This is the "send to storage" slot.
            // so do that.
            TESentientTreeCore core = (TESentientTreeCore) world.getTileEntity(coreLocation);
            if(core == null){
                BlockPos loc = TreeUtil.findCore(world, pos);
                setCoreLocation(loc);
                if(loc == null){
                    return false;
                }
                core = (TESentientTreeCore) world.getTileEntity(coreLocation);
            }
            ItemStack result = core.canFitItem(oldStack);
            if(result != ItemStack.EMPTY){
                // We can't actually fit the item
                return false;
            }
            core.doFitItem(oldStack);
            return true;
        }
        if(slot == 11){
            return true;
        }
        return true;
    }

    @Override
    public final NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        ItemStackHelper.saveAllItems(nbt, this.slots, false);

        NBTTagCompound blockPosNBT = new NBTTagCompound();        // NBTTagCompound is similar to a Java HashMap
        blockPosNBT.setInteger("x", coreLocation.getX());
        blockPosNBT.setInteger("y", coreLocation.getY());
        blockPosNBT.setInteger("z", coreLocation.getZ());
        nbt.setTag("coreLocation", blockPosNBT);
        ThingsOfNaturalEnergies.logger.error("Saving to coreLocation");
        return nbt;
    }

    @Override
    public int getSizeInventory() {
        return slots.size();
    }

    @Override
    public boolean isEmpty() {
        return slots.isEmpty();
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return slots.get(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack i = slots.get(index);
        if(i.isEmpty()) return i;
        ItemStack other = i.splitStack(count);
        return other;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        return slots.remove(index);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        slots.set(index, stack);
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory(EntityPlayer player) {    }

    @Override
    public void closeInventory(EntityPlayer player) {    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if(index == 9) return false; // They're trying to put it into the slot for recipe output.
        return true;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {

    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
        this.slots.clear();
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }
}
