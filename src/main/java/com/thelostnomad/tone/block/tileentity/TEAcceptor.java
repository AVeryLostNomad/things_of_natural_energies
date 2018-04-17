package com.thelostnomad.tone.block.tileentity;


// Simple Tile Entity that accepts items from any side and shoves them into the storage hollow.
// Literally any time an item is put in, we will do the thing to it.

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

// Is an *outward facing inventory* (blocks an automation will be interacting with this), so we need to use capabilities
public class TEAcceptor extends TileEntity implements IInventory, IInteractable, ICapabilityProvider, ITickable, SyncableTileEntity {

    public static final String NAME = "tone_acceptor_tileentity";
    private BlockPos coreLocation;

    private Boolean partialFits; // Should the acceptor fit *as much as is possible* of the stack? Or should it only
                                 // put items in if all of them can fit?
    private Boolean redstoneRequired; // Can it function without redstone?
    private Boolean voidExcess; // Should this acceptor void items that cannot fit?

    private final int NUMBER_OF_SLOTS = 1;
    private ItemStack[] itemStacks;

    private ItemStackHandler inventory = new ItemStackHandler(1){
        @Override
        public void setStackInSlot(int slot, @Nonnull ItemStack stack)
        {
            if(stack.isEmpty()) {
                TEAcceptor.this.setInventorySlotContents(36, stack);
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
            return TEAcceptor.this.getStackInSlot(36);
        }

        @Override
        @Nonnull
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate)
        {
            if (stack.isEmpty())
                return ItemStack.EMPTY;

            validateSlotIndex(slot);

            ItemStack existing = TEAcceptor.this.getStackInSlot(36);

            int limit = getStackLimit(slot, stack);

            if (!existing.isEmpty())
            {
                if (!ItemHandlerHelper.canItemStacksStack(stack, existing))
                    return stack;

                limit -= existing.getCount();
            }

            if (limit <= 0)
                return stack;

            boolean reachedLimit = stack.getCount() > limit;

            if (!simulate)
            {
                if (existing.isEmpty())
                {
                    TEAcceptor.this.setInventorySlotContents(slot + 36, reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, limit) : stack);
                }
                else
                {
                    existing.grow(reachedLimit ? limit : stack.getCount());
                }
                onContentsChanged(slot);
            }

            return reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, stack.getCount()- limit) : ItemStack.EMPTY;
        }

        @Override
        @Nonnull
        public ItemStack extractItem(int slot, int amount, boolean simulate)
        {
            return ItemStack.EMPTY; // Cannot extract from this item.
        }
    };

    public TEAcceptor() {
        itemStacks = new ItemStack[NUMBER_OF_SLOTS];
        clear();
    }

    public ItemStack[] getStacks(){
        return itemStacks;
    }

    public Boolean getPartialFits() {
        return partialFits == null ? false : partialFits;
    }

    public void setPartialFits(Boolean partialFits) {
        this.partialFits = partialFits;
    }

    public Boolean getRedstoneRequired() {
        return redstoneRequired == null ? false : redstoneRequired;
    }

    public void setRedstoneRequired(Boolean redstoneRequired) {
        this.redstoneRequired = redstoneRequired;
    }

    public Boolean getVoidExcess() {
        return voidExcess == null ? false : voidExcess;
    }

    public void setVoidExcess(Boolean voidExcess) {
        this.voidExcess = voidExcess;
    }

    public TESentientTreeCore getCore() {
        return (TESentientTreeCore) world.getTileEntity(coreLocation);
    }

    public BlockPos getCoreLocation(){
        return coreLocation;
    }

    public void setCoreLocation(BlockPos core){
        this.coreLocation = core;
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

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        if(coreLocation == null){
            ThingsOfNaturalEnergies.logger.error("Writing to null");
            return compound;
        }

        NBTTagCompound blockPosNBT = new NBTTagCompound();        // NBTTagCompound is similar to a Java HashMap
        blockPosNBT.setInteger("x", coreLocation.getX());
        blockPosNBT.setInteger("y", coreLocation.getY());
        blockPosNBT.setInteger("z", coreLocation.getZ());
        compound.setTag("coreLocation", blockPosNBT);

        if(voidExcess == null){
            // We likely haven't loaded any. Presume false
            voidExcess = false;
            partialFits = false;
            redstoneRequired = false;
        }

        compound.setBoolean("VoidExcess", voidExcess);
        compound.setBoolean("PartialFit", partialFits);
        compound.setBoolean("RedstoneOn", redstoneRequired);

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
        compound.setTag("Items", dataForAllSlots);

        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        NBTTagCompound coreLoc = compound.getCompoundTag("coreLocation");
        if(coreLoc != null){
            coreLocation = new BlockPos(coreLoc.getInteger("x"),
                    coreLoc.getInteger("y"), coreLoc.getInteger("z"));
        }

        ThingsOfNaturalEnergies.logger.error("Loading stuff");
        partialFits = compound.getBoolean("PartialFit");
        voidExcess = compound.getBoolean("VoidExcess");
        ThingsOfNaturalEnergies.logger.error("Reading value " + voidExcess);
        redstoneRequired = compound.getBoolean("RedstoneOn");

        final byte NBT_TYPE_COMPOUND = 10;       // See NBTBase.createNewByType() for a listing
        NBTTagList dataForAllSlots = compound.getTagList("Items", NBT_TYPE_COMPOUND);

        Arrays.fill(itemStacks, ItemStack.EMPTY);           // set all slots to empty EMPTY_ITEM
        for (int i = 0; i < dataForAllSlots.tagCount(); ++i) {
            NBTTagCompound dataForOneSlot = dataForAllSlots.getCompoundTagAt(i);
            int slotIndex = dataForOneSlot.getByte("Slot") & 255;

            if (slotIndex >= 0 && slotIndex < this.itemStacks.length) {
                this.itemStacks[slotIndex] = new ItemStack(dataForOneSlot);
            }
        }
    }

    @Override
    public InteractableType getType() {
        return InteractableType.MISC;
    }

    @Override
    public void update() {
        if(world.isRemote) return;

        if(getRedstoneRequired()){
            if(world.getStrongPower(pos) < 13){
                return;
            }
        }

        if(!getStackInSlot(36).isEmpty()){
            // We have an item in that slot. Get rid of it!
            ItemStack result = getStackInSlot(36);
            if(getCore().canFitItem(result) == ItemStack.EMPTY){
                // It all fits. Shove it in
                getCore().doFitItem(result);
                setInventorySlotContents(36, ItemStack.EMPTY);
            }else{
                // Only some fits. Should we try to fit as much as is possible?
                if(getPartialFits()){
                    // We should fit as much as we can
                    ItemStack remaining = getCore().doFitItem(result);
                    setInventorySlotContents(36, remaining);
                }

                if(getVoidExcess()){
                    // We've reached an item we can do nothing with. Void it
                    setInventorySlotContents( 36, ItemStack.EMPTY);
                }
            }
        }
    }

    @Override
    public int getSizeInventory() {
        return itemStacks.length;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : itemStacks) {
            if (!itemstack.isEmpty()) {  // terminal()
                return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        int actualSlot = index - 36;
        return itemStacks[actualSlot];
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack itemStackInSlot = getStackInSlot(index);
        if (itemStackInSlot.isEmpty()) return ItemStack.EMPTY;  // isEmpt();   EMPTY_ITEM

        ItemStack itemStackRemoved;
        if (itemStackInSlot.getCount() <= count) {  // getStackSize()
            itemStackRemoved = itemStackInSlot;
            setInventorySlotContents(index, ItemStack.EMPTY);   // EMPTY_ITEM
        } else {
            itemStackRemoved = itemStackInSlot.splitStack(count);
            if (itemStackInSlot.getCount() == 0) { // getStackSize
                setInventorySlotContents(index, ItemStack.EMPTY);   // EMPTY_ITEM
            }
        }
        markDirty();
        return itemStackRemoved;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack itemStack = getStackInSlot(index);
        if (!itemStack.isEmpty()) setInventorySlotContents(index, ItemStack.EMPTY);  //terminal(), EMPTY_ITEM
        return itemStack;
    }

    @Override
    public void setInventorySlotContents(int slotIndex, ItemStack itemstack) {
        int actualSlot = slotIndex - 36;
        itemStacks[actualSlot] = itemstack;
        if (itemstack.isEmpty() && itemstack.getCount() > getInventoryStackLimit()) { //  terminal(); getStackSize()
            itemstack.setCount(getInventoryStackLimit());  //setStackSize
        }
        markDirty();
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

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

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
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
        Arrays.fill(itemStacks, ItemStack.EMPTY);
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    @Override
    public NBTTagCompound getSyncable() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("key", "acceptor");

        NBTTagCompound blockPosNBT = new NBTTagCompound();        // NBTTagCompound is similar to a Java HashMap
        blockPosNBT.setInteger("x", pos.getX());
        blockPosNBT.setInteger("y", pos.getY());
        blockPosNBT.setInteger("z", pos.getZ());
        tag.setTag("location", blockPosNBT);

        if(voidExcess == null){
            voidExcess = false;
            partialFits = false;
            redstoneRequired = false;
        }

        tag.setBoolean("void", voidExcess);
        tag.setBoolean("partial", partialFits);
        tag.setBoolean("redstone", redstoneRequired);
        return tag;
    }

    @Override
    public void doSync(NBTTagCompound fromClient) {
        if(!fromClient.getString("key").equals("acceptor")) return;

        NBTTagCompound coreLoc = fromClient.getCompoundTag("location");
        BlockPos loc = new BlockPos(coreLoc.getInteger("x"),
                coreLoc.getInteger("y"), coreLoc.getInteger("z"));
        if(!loc.equals(pos)) return;

        voidExcess = fromClient.getBoolean("void");
        partialFits = fromClient.getBoolean("partial");
        redstoneRequired = fromClient.getBoolean("redstone");
        ThingsOfNaturalEnergies.logger.error("Void excess now " + voidExcess);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }
}
