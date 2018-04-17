package com.thelostnomad.tone.block.tileentity;

import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import com.thelostnomad.tone.block.BlockPuller;
import com.thelostnomad.tone.block.BlockPusher;
import com.thelostnomad.tone.util.crafting.StackUtil;
import com.thelostnomad.tone.util.gui.SyncableTileEntity;
import com.thelostnomad.tone.util.world.IInteractable;
import com.thelostnomad.tone.util.world.ITree;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;

public class TEFocusPusher extends TileEntity implements IInventory, IInteractable, ITickable, SyncableTileEntity, ICapabilityProvider {

    public static final String NAME = "tone_focus_pusher_tileentity";
    private BlockPos coreLocation = null;

    private final int NUMBER_OF_SLOTS = 1;
    private ItemStack[] itemStacks;

    private Boolean redstoneRequired;
    private Boolean exactItem;
    private Boolean pushIntoOtherSlots;
    private Integer rate; // Number of items to try to push at once
    private Integer slot; // Slot to try to target

    // Anything calling this should only be trying to interact with the rightmost slot. Nothing more
    private ItemStackHandler inventory = new ItemStackHandler(){
        @Override
        public void setStackInSlot(int slot, @Nonnull ItemStack stack)
        {
            TEFocusPusher.this.setInventorySlotContents(36, stack);
        }

        @Override
        public int getSlots(){
            return 1;
        }

        @Override
        @Nonnull
        public ItemStack getStackInSlot(int slot)
        {
            return TEFocusPusher.this.getStackInSlot(36);
        }

        @Override
        @Nonnull
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate)
        {
            if (stack.isEmpty())
                return ItemStack.EMPTY;

            return stack; // Nothing goes in to a focus pusher.
        }

        @Override
        @Nonnull
        public ItemStack extractItem(int slot, int amount, boolean simulate)
        {
            return ItemStack.EMPTY; // Nothing comes out of a focus pusher.;
        }
    };

    public TEFocusPusher() {
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

        if(redstoneRequired == null){
            // We likely haven't loaded any. Presume false
            rate = 0;
            slot = 0;
            exactItem = false;
            redstoneRequired = false;
            pushIntoOtherSlots = false;
        }

        parentNBTTagCompound.setInteger("Rate", rate);
        parentNBTTagCompound.setInteger("Slot", slot);
        parentNBTTagCompound.setBoolean("ExactItem", exactItem);
        parentNBTTagCompound.setBoolean("RedstoneOn", redstoneRequired);
        parentNBTTagCompound.setBoolean("OtherSlots", pushIntoOtherSlots);

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

        /**
         *
         parentNBTTagCompound.setInteger("Rate", rate);
         parentNBTTagCompound.setInteger("Slot", slot);
         */
        rate = parentNBTTagCompound.getInteger("Rate");
        slot = parentNBTTagCompound.getInteger("Slot");
        exactItem = parentNBTTagCompound.getBoolean("ExactItem");
        redstoneRequired = parentNBTTagCompound.getBoolean("RedstoneOn");
        pushIntoOtherSlots = parentNBTTagCompound.getBoolean("OtherSlots");
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
        return InteractableType.MISC;
    }

    public BlockPos getTargetLocation() {
        EnumFacing direction = world.getBlockState(pos).getValue(BlockPuller.FACING);
        EnumFacing blockface = direction.getOpposite();
        BlockPos offset = pos.offset(direction);

        while (world.getBlockState(offset).getBlock() instanceof ITree) {
            offset = offset.offset(direction);
        }
        return offset;
    }

    public EnumFacing getTargetBlockface(){
        EnumFacing direction = world.getBlockState(pos).getValue(BlockPuller.FACING);
        EnumFacing blockface = direction.getOpposite();
        return blockface;
    }

    @Override
    public void update() {
        if(world.isRemote) return;

        if(getRedstoneRequired()){
            // Only function if this block is powered
            if(world.getStrongPower(pos) < 13){
                return;
            }
        }

        TileEntity coreTE = world.getTileEntity(coreLocation);
        if(coreTE != null){
            TESentientTreeCore core = (TESentientTreeCore) coreTE;

            if(!getStackInSlot(36).isEmpty()){
                // We have an output item. Let's try this
                ItemStack toFetch = getStackInSlot(36).copy();

                if(rate == 0){
                    return;
                }
                toFetch.setCount(getRate());

                // Let's make sure our facing inventory is capable of holding such a thing, first and foremost.
                BlockPos offset = getTargetLocation();
                TileEntity targetTile = world.getTileEntity(offset);
                if(targetTile == null){
                    return; // We cannot insert into a non-existent tile entity
                }
                boolean hasItemCapability = targetTile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, getTargetBlockface());
                if(hasItemCapability){
                    // It does have an item capability!
                    IItemHandler cap = targetTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, getTargetBlockface());

                    if(cap.getSlots() <= getSlot()){
                        return;
                    }

                    ItemStack existing = cap.getStackInSlot(getSlot());

                    if(existing.getCount() >= getStackInSlot(36).getCount()){
                        // If we already have enough items, stop.
                        return;
                    }

                    ItemStack result = cap.insertItem(getSlot(), toFetch, true);

                    if(result.isEmpty()){
                        // It would all fit.
                        boolean getRes = core.canGetItemstackOut(toFetch, getExactItem(), false);
                        if(getRes){
                            // We do have this item in our inventory as something that we can do.
                            // Do it.
                            core.getItemstackOut(toFetch, getExactItem(), false);
                            cap.insertItem(getSlot(), toFetch, false);
                        }else{
                            boolean attempt = core.autocraftIfPossible(Arrays.asList(new ItemStack[]{toFetch}));
                            if(attempt){
                                // We can find the item
                                core.getItemstackOut(toFetch, getExactItem(), false);
                                cap.insertItem(getSlot(), toFetch, false);
                            }
                        }
                    }else{
                        // TODO have a partial stack fit? No. Probably just lower your rate down.
                    }
                }else{
                    // Try the IInventory method
                    IInventory target = TileEntityHopper.getInventoryAtPosition(world, offset.getX(), offset.getY(), offset.getZ());
                    if(target == null){
                        // No IInventory either
                        return;
                    }

                    if(target.getSizeInventory() <= getSlot()){
                        // Slot is invalid.
                        return;
                    }

                    ItemStack existing = target.getStackInSlot(getSlot());

                    if(existing.isEmpty()){
                        // We can just put it in
                        int newAmt = toFetch.getCount();

                        if(newAmt > target.getInventoryStackLimit()){
                            return;
                        }

                        boolean result = core.canGetItemstackOut(toFetch, getExactItem(), false);
                        if(result){
                            // We do have this item in our inventory as something that we can do.
                            target.setInventorySlotContents(getSlot(), core.getItemstackOut(toFetch, getExactItem(), false));
                        }else{
                            boolean attempt = core.autocraftIfPossible(Arrays.asList(new ItemStack[]{toFetch}));
                            if(attempt){
                                // We can find the item
                                core.getItemstackOut(toFetch, getExactItem(), false);
                                target.setInventorySlotContents(getSlot(), core.getItemstackOut(toFetch, getExactItem(), false));
                            }
                        }
                    }else{
                        // We have to check if it will fit on top of the old stack
                        if(getExactItem()){
                            if(!StackUtil.stacksEqual(existing, toFetch)){
                                return;
                            }
                        }else{
                            if(StackUtil.stacksShallowEqual(existing, toFetch)){
                                return;
                            }
                        }

                        if(existing.getCount() >= getStackInSlot(36).getCount()){
                            return;
                        }

                        int newAmt = existing.getCount() + toFetch.getCount();

                        if(newAmt > target.getInventoryStackLimit()){
                            return;
                        }

                        ItemStack toSet = existing.copy();
                        toSet.setCount(newAmt);

                        // There is an IInventory
                        boolean result = core.canGetItemstackOut(toFetch, getExactItem(), false);
                        if(result){
                            // We do have this item in our inventory as something that we can do.
                            core.getItemstackOut(toFetch, getExactItem(), false);
                            target.setInventorySlotContents(getSlot(), toSet);
                        }else{
                            boolean attempt = core.autocraftIfPossible(Arrays.asList(new ItemStack[]{toFetch}));
                            if(attempt){
                                // We can find the item
                                core.getItemstackOut(toFetch, getExactItem(), false);
                                target.setInventorySlotContents(getSlot(), toSet);
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isInventoryFull(IInventory inventoryIn, EnumFacing side) {
        if (inventoryIn instanceof ISidedInventory) {
            ISidedInventory isidedinventory = (ISidedInventory) inventoryIn;
            int[] aint = isidedinventory.getSlotsForFace(side);

            for (int k : aint) {
                ItemStack itemstack1 = isidedinventory.getStackInSlot(k);

                if (itemstack1.isEmpty() || itemstack1.getCount() != itemstack1.getMaxStackSize()) {
                    return false;
                }
            }
        } else {
            int i = inventoryIn.getSizeInventory();

            for (int j = 0; j < i; ++j) {
                ItemStack itemstack = inventoryIn.getStackInSlot(j);

                if (itemstack.isEmpty() || itemstack.getCount() != itemstack.getMaxStackSize()) {
                    return false;
                }
            }
        }

        return true;
    }

    public Boolean getRedstoneRequired() {
        return redstoneRequired == null ? false : redstoneRequired;
    }

    public void setRedstoneRequired(Boolean redstoneRequired) {
        this.redstoneRequired = redstoneRequired;
    }

    public Boolean getExactItem() {
        return exactItem == null ? false : exactItem;
    }

    public void setExactItem(Boolean exactItem) {
        this.exactItem = exactItem;
    }

    public Integer getRate() {
        return rate == null ? 1 : rate;
    }

    public void setRate(Integer rate) {
        this.rate = rate;
    }

    public Integer getSlot() {
        return slot == null ? 1 : slot;
    }

    public void setSlot(Integer slot) {
        this.slot = slot;
    }

    public Boolean getPushIntoOtherSlots() {
        return pushIntoOtherSlots == null ? false : pushIntoOtherSlots;
    }

    public void setPushIntoOtherSlots(Boolean pushIntoOtherSlots) {
        this.pushIntoOtherSlots = pushIntoOtherSlots;
    }

    @Override
    public NBTTagCompound getSyncable() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("key", "focus_pusher");

        NBTTagCompound blockPosNBT = new NBTTagCompound();        // NBTTagCompound is similar to a Java HashMap
        blockPosNBT.setInteger("x", pos.getX());
        blockPosNBT.setInteger("y", pos.getY());
        blockPosNBT.setInteger("z", pos.getZ());
        tag.setTag("location", blockPosNBT);

        if(rate == null){
            rate = 0;
            slot = 0;
            exactItem = false;
            redstoneRequired = false;
            pushIntoOtherSlots = false;
        }

        tag.setInteger("Rate", rate);
        tag.setInteger("Slot", slot);
        tag.setBoolean("exact", exactItem);
        tag.setBoolean("redstone", redstoneRequired);
        tag.setBoolean("OtherSlots", pushIntoOtherSlots);
        return tag;
    }

    @Override
    public void doSync(NBTTagCompound fromClient) {
        if(!fromClient.getString("key").equals("focus_pusher")) return;

        NBTTagCompound coreLoc = fromClient.getCompoundTag("location");
        BlockPos loc = new BlockPos(coreLoc.getInteger("x"),
                coreLoc.getInteger("y"), coreLoc.getInteger("z"));
        if(!loc.equals(pos)) return;

        slot = fromClient.getInteger("Slot");
        rate = fromClient.getInteger("Rate");
        exactItem = fromClient.getBoolean("exact");
        redstoneRequired = fromClient.getBoolean("redstone");
        pushIntoOtherSlots = fromClient.getBoolean("OtherSlots");
        ThingsOfNaturalEnergies.logger.error("Syncing rate to " + rate);
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

