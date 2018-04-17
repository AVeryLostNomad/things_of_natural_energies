package com.thelostnomad.tone.block.tileentity;

import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import com.thelostnomad.tone.util.gui.SyncableTileEntity;
import com.thelostnomad.tone.util.world.IInteractable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;

public class TEKeeper extends TileEntity implements IInventory, IInteractable, ITickable, SyncableTileEntity, ICapabilityProvider {

    public static final String NAME = "tone_keeper_tileentity";
    private BlockPos coreLocation = null;

    private final int NUMBER_OF_SLOTS = 2;
    private ItemStack[] itemStacks;

    private Boolean includeInInventory;
    private Boolean redstoneRequired;
    private Boolean exactItem;

    // Anything calling this should only be trying to interact with the rightmost slot. Nothing more
    private ItemStackHandler inventory = new ItemStackHandler(){
        @Override
        public void setStackInSlot(int slot, @Nonnull ItemStack stack)
        {
            if(stack.isEmpty()) {
                TEKeeper.this.setInventorySlotContents(37, stack);
            }
        }

        @Override
        public int getSlots(){
            return 1;
        }

        @Override
        @Nonnull
        public ItemStack getStackInSlot(int slot)
        {
            return TEKeeper.this.getStackInSlot(37);
        }

        @Override
        @Nonnull
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate)
        {
            if (stack.isEmpty())
                return ItemStack.EMPTY;

            return stack; // Nothing goes in to a keeper.
        }

        @Override
        @Nonnull
        public ItemStack extractItem(int slot, int amount, boolean simulate)
        {
            if (amount == 0)
                return ItemStack.EMPTY;

            ItemStack existing = TEKeeper.this.getStackInSlot(37);

            if (existing.isEmpty())
                return ItemStack.EMPTY;

            int toExtract = Math.min(amount, existing.getMaxStackSize());

            if (existing.getCount() <= toExtract)
            {
                if (!simulate)
                {
                    setStackInSlot(slot, ItemStack.EMPTY);
                }
                return existing;
            }
            else
            {
                if (!simulate)
                {
                    setStackInSlot(slot, ItemHandlerHelper.copyStackWithSize(existing, existing.getCount() - toExtract));
                }

                return ItemHandlerHelper.copyStackWithSize(existing, toExtract);
            }
        }
    };

    public TEKeeper() {
        itemStacks = new ItemStack[NUMBER_OF_SLOTS];
        clear();
    }

    public ItemStack[] getStacks(){
        return itemStacks;
    }

    @Override
    public int getSizeInventory() {
        return itemStacks.length;
    }

    public boolean isIncludeInInventory() {
        return includeInInventory == null ? false : includeInInventory;
    }

    public void setIncludeInInventory(boolean value){
        includeInInventory = value;
    }

    public boolean isRedstoneRequired() {
        return redstoneRequired == null ? false : redstoneRequired;
    }

    public boolean isExactItem() {
        return exactItem == null ? false : exactItem;
    }

    public void setExactItem(boolean value){
        exactItem = value;
    }

    public void setRedstoneRequired(boolean value){
        redstoneRequired = value;
    }

    @Override
    public boolean isEmpty()
    {
        for (ItemStack itemstack : itemStacks) {
            if (!itemstack.isEmpty()) {  // terminal()
                return false;
            }
        }

        return true;
    }

    // Gets the stack in the given slot
    @Override
    public ItemStack getStackInSlot(int slotIndex) {
        int actualSlot = slotIndex - 36;
        return itemStacks[actualSlot];
    }

    /**
     * Removes some of the units from itemstack in the given slot, and returns as a separate itemstack
     * @param slotIndex the slot number to remove the items from
     * @param count the number of units to remove
     * @return a new itemstack containing the units removed from the slot
     */
    @Override
    public ItemStack decrStackSize(int slotIndex, int count) {
        ItemStack itemStackInSlot = getStackInSlot(slotIndex);
        if (itemStackInSlot.isEmpty()) return ItemStack.EMPTY;  // isEmpt();   EMPTY_ITEM

        ItemStack itemStackRemoved;
        if (itemStackInSlot.getCount() <= count) {  // getStackSize()
            itemStackRemoved = itemStackInSlot;
            setInventorySlotContents(slotIndex, ItemStack.EMPTY);   // EMPTY_ITEM
        } else {
            itemStackRemoved = itemStackInSlot.splitStack(count);
            if (itemStackInSlot.getCount() == 0) { // getStackSize
                setInventorySlotContents(slotIndex, ItemStack.EMPTY);   // EMPTY_ITEM
            }
        }
        markDirty();
        return itemStackRemoved;
    }

    // overwrites the stack in the given slotIndex with the given stack
    @Override
    public void setInventorySlotContents(int slotIndex, ItemStack itemstack) {
        int actualSlot = slotIndex - 36;
        itemStacks[actualSlot] = itemstack;
        if (itemstack.isEmpty() && itemstack.getCount() > getInventoryStackLimit()) { //  terminal(); getStackSize()
            itemstack.setCount(getInventoryStackLimit());  //setStackSize
        }
        markDirty();
    }

    // This is the maximum number if items allowed in each slot
    // This only affects things such as hoppers trying to insert items you need to use the container to enforce this for players
    // inserting items via the gui
    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isItemValidForSlot(int slotIndex, ItemStack itemstack) {
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

    // Return true if the given player is able to use this block. In this case it checks that
    // 1) the world tileentity hasn't been replaced in the meantime, and
    // 2) the player isn't too far away from the centre of the block
    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        if (this.world.getTileEntity(this.pos) != this) return false;
        final double X_CENTRE_OFFSET = 0.5;
        final double Y_CENTRE_OFFSET = 0.5;
        final double Z_CENTRE_OFFSET = 0.5;
        final double MAXIMUM_DISTANCE_SQ = 5.0 * 5.0;
        return player.getDistanceSq(pos.getX() + X_CENTRE_OFFSET, pos.getY() + Y_CENTRE_OFFSET, pos.getZ() + Z_CENTRE_OFFSET) < MAXIMUM_DISTANCE_SQ;
    }

    @Override
    public void openInventory(EntityPlayer player) {

    }

    @Override
    public void closeInventory(EntityPlayer player) {

    }

    public void setCoreLocation(BlockPos core){
        this.coreLocation = core;
    }

    public BlockPos getCoreLocation() {
        return this.coreLocation;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound parentNBTTagCompound)
    {
        super.writeToNBT(parentNBTTagCompound);

        if(coreLocation == null){
            ThingsOfNaturalEnergies.logger.error("Writing to null");
            return parentNBTTagCompound;
        }

        NBTTagCompound blockPosNBT = new NBTTagCompound();        // NBTTagCompound is similar to a Java HashMap
        blockPosNBT.setInteger("x", coreLocation.getX());
        blockPosNBT.setInteger("y", coreLocation.getY());
        blockPosNBT.setInteger("z", coreLocation.getZ());
        parentNBTTagCompound.setTag("coreLocation", blockPosNBT);

        NBTTagList dataForAllSlots = new NBTTagList();
        for (int i = 0; i < this.itemStacks.length; ++i) {
            if (!this.itemStacks[i].isEmpty())	{ //terminal()
                NBTTagCompound dataForThisSlot = new NBTTagCompound();
                dataForThisSlot.setByte("Slot", (byte) i);
                this.itemStacks[i].writeToNBT(dataForThisSlot);
                dataForAllSlots.appendTag(dataForThisSlot);
            }
        }
        // the array of hashmaps is then inserted into the parent hashmap for the container
        parentNBTTagCompound.setTag("Items", dataForAllSlots);

        if(includeInInventory == null){
            // We likely haven't loaded any. Presume false
            includeInInventory = false;
            exactItem = false;
            redstoneRequired = false;
        }

        parentNBTTagCompound.setBoolean("IncludeInventory", includeInInventory);
        parentNBTTagCompound.setBoolean("ExactItem", exactItem);
        parentNBTTagCompound.setBoolean("RedstoneOn", redstoneRequired);

        // return the NBT Tag Compound
        return parentNBTTagCompound;
    }

    @Override
    public ItemStack removeStackFromSlot(int slotIndex) {
        ItemStack itemStack = getStackInSlot(slotIndex);
        if (!itemStack.isEmpty()) setInventorySlotContents(slotIndex, ItemStack.EMPTY);  //terminal(), EMPTY_ITEM
        return itemStack;
    }

    @Override
    public void clear() {
        Arrays.fill(itemStacks, ItemStack.EMPTY);  //empty item
    }

    @Override
    public void readFromNBT(NBTTagCompound parentNBTTagCompound)
    {
        super.readFromNBT(parentNBTTagCompound);

        NBTTagCompound coreLoc = parentNBTTagCompound.getCompoundTag("coreLocation");
        if(coreLoc != null){
            coreLocation = new BlockPos(coreLoc.getInteger("x"),
                    coreLoc.getInteger("y"), coreLoc.getInteger("z"));
        }

        final byte NBT_TYPE_COMPOUND = 10;       // See NBTBase.createNewByType() for a listing
        NBTTagList dataForAllSlots = parentNBTTagCompound.getTagList("Items", NBT_TYPE_COMPOUND);

        Arrays.fill(itemStacks, ItemStack.EMPTY);           // set all slots to empty EMPTY_ITEM
        for (int i = 0; i < dataForAllSlots.tagCount(); ++i) {
            NBTTagCompound dataForOneSlot = dataForAllSlots.getCompoundTagAt(i);
            int slotIndex = dataForOneSlot.getByte("Slot") & 255;

            if (slotIndex >= 0 && slotIndex < this.itemStacks.length) {
                this.itemStacks[slotIndex] = new ItemStack(dataForOneSlot);
            }
        }

        includeInInventory = parentNBTTagCompound.getBoolean("IncludeInventory");
        exactItem = parentNBTTagCompound.getBoolean("ExactItem");
        redstoneRequired = parentNBTTagCompound.getBoolean("RedstoneOn");
        ThingsOfNaturalEnergies.logger.error("Reading in redstone " + redstoneRequired);
    }

    @Override
    public String getName() {
        return NAME;
    }

    public ItemStack getOutCopy(){
        return itemStacks[1].copy();
    }

    public ItemStack getInCopy(){
        return itemStacks[0].copy();
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public InteractableType getType() {
        return InteractableType.KEEPER;
    }

    @Override
    public void update() {
        if(world.isRemote) return;

        if(isRedstoneRequired()){
            // Only function if this block is powered
            if(world.getStrongPower(pos) < 13){
                return;
            }
        }

        TileEntity coreTE = world.getTileEntity(coreLocation);
        if(coreTE != null){
            TESentientTreeCore core = (TESentientTreeCore) coreTE;

            if(getOutCopy().isEmpty() && !getInCopy().isEmpty()){
                // We don't already have an item in our target slot
                // See if we can just grabbomundo
                ItemStack toFetch = getInCopy();

                boolean result = core.canGetItemstackOut(toFetch, this.exactItem, true);
                if(result){
                    setInventorySlotContents(37, core.getItemstackOut(toFetch, this.exactItem, true));
                }else{
                    // We can't quite extract this item, maybe we can craft it?
                    boolean attempt = core.autocraftIfPossible(Arrays.asList(new ItemStack[]{toFetch}));
                    if(attempt){
                        setInventorySlotContents(37, core.getItemstackOut(toFetch, this.exactItem, true));
                    }
                }
            }
        }
    }

    @Override
    public NBTTagCompound getSyncable() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("key", "keeper");

        NBTTagCompound blockPosNBT = new NBTTagCompound();        // NBTTagCompound is similar to a Java HashMap
        blockPosNBT.setInteger("x", pos.getX());
        blockPosNBT.setInteger("y", pos.getY());
        blockPosNBT.setInteger("z", pos.getZ());
        tag.setTag("location", blockPosNBT);

        if(includeInInventory == null){
            includeInInventory = false;
            exactItem = false;
            redstoneRequired = false;
        }

        tag.setBoolean("include", includeInInventory);
        tag.setBoolean("exact", exactItem);
        tag.setBoolean("redstone", redstoneRequired);
        return tag;
    }

    @Override
    public void doSync(NBTTagCompound fromClient) {
        if(!fromClient.getString("key").equals("keeper")) return;

        NBTTagCompound coreLoc = fromClient.getCompoundTag("location");
        BlockPos loc = new BlockPos(coreLoc.getInteger("x"),
                    coreLoc.getInteger("y"), coreLoc.getInteger("z"));
        if(!loc.equals(pos)) return;

        includeInInventory = fromClient.getBoolean("include");
        exactItem = fromClient.getBoolean("exact");
        redstoneRequired = fromClient.getBoolean("redstone");
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ? (T)inventory : super.getCapability(capability, facing);
    }
}
