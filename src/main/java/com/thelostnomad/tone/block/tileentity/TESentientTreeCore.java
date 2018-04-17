package com.thelostnomad.tone.block.tileentity;

import com.google.common.base.Predicate;
import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import com.thelostnomad.tone.block.BlockPuller;
import com.thelostnomad.tone.block.BlockPusher;
import com.thelostnomad.tone.block.berries.FuncoBerry;
import com.thelostnomad.tone.block.berries.GlutoBerry;
import com.thelostnomad.tone.block.berries.HastoBerry;
import com.thelostnomad.tone.block.berries.RezzoBerry;
import com.thelostnomad.tone.item.tokens.ItemToken;
import com.thelostnomad.tone.registry.ModItems;
import com.thelostnomad.tone.util.LifeUtil;
import com.thelostnomad.tone.util.MobUtil;
import com.thelostnomad.tone.util.TreeUtil;
import com.thelostnomad.tone.util.crafting.CraftTreeBuilder;
import com.thelostnomad.tone.util.crafting.StackUtil;
import com.thelostnomad.tone.util.world.IInteractable;
import mezz.jei.gui.CraftingGridHelper;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import scala.actors.threadpool.Arrays;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TESentientTreeCore extends TileEntity implements ITickable {

    // Name of tile entity
    public static final String NAME = "tone_sentienttree_tileentity";

    List<BlockPos> berries = new ArrayList<>();

    List<BlockPos> roots = new ArrayList<>();

    List<BlockPos> interactables = new ArrayList<>();

    private long tickcount = 0;
    private Long tickRate = 60L; // For a tree without hastoberries, this is the default. One action cycle every three
    // seconds.

    private Integer glutoCount = 0;
    private Integer funcoCount = 0;
    private Integer craftoCount = 80; // TODO implement later.
    private Integer rezzoCount = 0;

    // For rezzoberry
    private EntityLiving targetSpawn = null;
    private Integer lifeContributedSoFar = 0;
    private Integer lifeNeeded = 0;

    private Double life = 0D;
    private Double maxLife = 0D; // The maximum amount of life that can be stored in this thing.

    // When the world loads from disk, the server needs to send the TileEntity information to the client
    //  it uses getUpdatePacket(), getUpdateTag(), onDataPacket(), and handleUpdateTag() to do this:
    //  getUpdatePacket() and onDataPacket() are used for one-at-a-time TileEntity updates
    //  getUpdateTag() and handleUpdateTag() are used by vanilla to collate together into a single chunk update packet
    //  Not really required for this example since we only use the timer on the client, but included anyway for illustration
    @Override
    @Nullable
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        writeToNBT(nbtTagCompound);
        int metadata = getBlockMetadata();
        return new SPacketUpdateTileEntity(this.pos, metadata, nbtTagCompound);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
    }

    /* Creates a tag containing the TileEntity information, used by vanilla to transmit from server to client
     */
    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        writeToNBT(nbtTagCompound);
        return nbtTagCompound;
    }

    public void reIndexRoots() {
        roots = TreeUtil.findAllConnectedRoots(world, pos.down());
    }

    public void addRoot(BlockPos pos) {
        this.roots.add(pos);
    }

    public void removeRoot(BlockPos pos) {
        this.roots.remove(pos);
    }

    /* Populates this TileEntity with information from the tag, used by vanilla to transmit from server to client
     */
    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        this.readFromNBT(tag);
    }

    // This is where you save any data that you don't want to lose when the tile entity unloads
    // In this case, we only need to store the ticks left until explosion, but we store a bunch of other
    //  data as well to serve as an example.
    // NBTexplorer is a very useful tool to examine the structure of your NBT saved data and make sure it's correct:
    //   http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-tools/1262665-nbtexplorer-nbt-editor-for-windows-and-mac


    private void selectSpawnTarget() {
        List<EntityLiving> el = MobUtil.mobsForItemLoot(world, allItemsInStorage());
        if (el.size() == 0) return;
        Map<Integer, EntityLiving> oddsBreakdown = new HashMap<>();
        int total = 0;
        for (EntityLiving e : el) {
            if (LifeUtil.getLifeForEntity(e) > getMaxLife()) {
                // We have no way of making this entity.
                // Skip it
                continue;
            }
            total += LifeUtil.getLifeForEntity(e);
            oddsBreakdown.put(Integer.valueOf(total), e);
        }
        boolean found = false;
        EntityLiving last = null;
        while (oddsBreakdown.size() > 1 && !found) {
            // Eliminate mobs to select
            int rand = world.rand.nextInt(total);
            for (Map.Entry<Integer, EntityLiving> entry : oddsBreakdown.entrySet()) {
                if (rand < entry.getKey()) {
                    last = entry.getValue();
                    continue;
                } else {
                    // We've reached the end of the line. rand is too big. Get the last one that it was less than
                    found = true;
                    break;
                }
            }
        }
        if (last == null) {
            last = el.get(0);
        }

        targetSpawn = last;
        lifeContributedSoFar = 0;
        lifeNeeded = (int) LifeUtil.getLifeForEntity(targetSpawn) * 2;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound parentNBTTagCompound) {
        super.writeToNBT(parentNBTTagCompound); // The super call is required to save the tiles location

        parentNBTTagCompound.setTag("tickRate", new NBTTagLong(tickRate));
        parentNBTTagCompound.setTag("life", new NBTTagDouble(life));
        parentNBTTagCompound.setTag("maxlife", new NBTTagDouble(maxLife));
        parentNBTTagCompound.setTag("glutocount", new NBTTagInt(glutoCount));
        parentNBTTagCompound.setTag("funcocount", new NBTTagInt(funcoCount));
        parentNBTTagCompound.setTag("rezzocount", new NBTTagInt(rezzoCount));

        NBTTagCompound entityTargetTag = new NBTTagCompound();
        if (targetSpawn != null) {
            targetSpawn.writeEntityToNBT(entityTargetTag);
            parentNBTTagCompound.setTag("target_spawn", entityTargetTag);
            parentNBTTagCompound.setTag("target_spawn_name", new NBTTagString(targetSpawn.getClass().getName()));
        }
        parentNBTTagCompound.setTag("lifeContributedSoFar", new NBTTagInt(lifeContributedSoFar));
        parentNBTTagCompound.setTag("lifeNeeded", new NBTTagInt(lifeNeeded));

        NBTTagList interactables = new NBTTagList();
        for (BlockPos b : this.interactables) {
            NBTTagCompound thisBlockPos = new NBTTagCompound();        // NBTTagCompound is similar to a Java HashMap
            thisBlockPos.setInteger("x", b.getX());
            thisBlockPos.setInteger("y", b.getY());
            thisBlockPos.setInteger("z", b.getZ());
            interactables.appendTag(thisBlockPos);
        }
        parentNBTTagCompound.setTag("interactables", interactables);

        NBTTagList berries = new NBTTagList();
        for (BlockPos b : this.berries) {
            NBTTagCompound thisBlockPos = new NBTTagCompound();        // NBTTagCompound is similar to a Java HashMap
            thisBlockPos.setInteger("x", b.getX());
            thisBlockPos.setInteger("y", b.getY());
            thisBlockPos.setInteger("z", b.getZ());
            berries.appendTag(thisBlockPos);
        }
        parentNBTTagCompound.setTag("berries", berries);

        NBTTagList roots = new NBTTagList();
        for (BlockPos b : this.roots) {
            NBTTagCompound thisBlockPos = new NBTTagCompound();        // NBTTagCompound is similar to a Java HashMap
            thisBlockPos.setInteger("x", b.getX());
            thisBlockPos.setInteger("y", b.getY());
            thisBlockPos.setInteger("z", b.getZ());
            roots.appendTag(thisBlockPos);
        }
        parentNBTTagCompound.setTag("roots", roots);

        return parentNBTTagCompound;
    }

    // This is where you load the data that you saved in writeToNBT
    @Override
    public void readFromNBT(NBTTagCompound parentNBTTagCompound) {
        super.readFromNBT(parentNBTTagCompound); // The super call is required to load the tiles location

        tickRate = parentNBTTagCompound.getLong("tickRate");
        if (tickRate == null) tickRate = 60L;
        life = parentNBTTagCompound.getDouble("life");
        if (life == null) life = 0D;
        maxLife = parentNBTTagCompound.getDouble("maxlife");
        if (maxLife == null) maxLife = 0D;
        glutoCount = parentNBTTagCompound.getInteger("glutocount");
        if (glutoCount == null) glutoCount = 0;
        funcoCount = parentNBTTagCompound.getInteger("funcocount");
        if (funcoCount == null) funcoCount = 0;
        rezzoCount = parentNBTTagCompound.getInteger("rezzocount");
        if (rezzoCount == null) rezzoCount = 0;
        lifeNeeded = parentNBTTagCompound.getInteger("lifeNeeded");
        if (lifeNeeded == null) lifeNeeded = 0;

        lifeContributedSoFar = parentNBTTagCompound.getInteger("lifeContributedSoFar");
        if (lifeContributedSoFar == null) lifeContributedSoFar = 0;
        if (lifeNeeded == lifeContributedSoFar) {
            lifeContributedSoFar = (int) (lifeContributedSoFar / 2D);
        }
        try {
            NBTTagCompound entityTargetTag = parentNBTTagCompound.getCompoundTag("target_spawn");
            Class<? extends EntityLiving> entityClass = null;
            entityClass = (Class<? extends EntityLiving>) Class.forName(parentNBTTagCompound.getString("target_spawn_name"));
            EntityLiving el = (EntityLiving) entityClass.getConstructor(World.class).newInstance(world);
            el.readEntityFromNBT(entityTargetTag);
            targetSpawn = el;
        } catch (Exception e) {
            targetSpawn = null;
        }
        NBTTagList berries = parentNBTTagCompound.getTagList("berries", 10);
        for (int i = 0; i < berries.tagCount(); i++) {
            NBTTagCompound comp = berries.getCompoundTagAt(i);
            BlockPos pos = new BlockPos(comp.getInteger("x"), comp.getInteger("y"), comp.getInteger("z"));
            this.berries.add(pos);
        }
        NBTTagList roots = parentNBTTagCompound.getTagList("roots", 10);
        for (int i = 0; i < roots.tagCount(); i++) {
            NBTTagCompound comp = roots.getCompoundTagAt(i);
            BlockPos pos = new BlockPos(comp.getInteger("x"), comp.getInteger("y"), comp.getInteger("z"));
            this.roots.add(pos);
        }
        NBTTagList integrations = parentNBTTagCompound.getTagList("interactables", 10);
        for (int i = 0; i < integrations.tagCount(); i++) {
            NBTTagCompound comp = integrations.getCompoundTagAt(i);
            BlockPos pos = new BlockPos(comp.getInteger("x"), comp.getInteger("y"), comp.getInteger("z"));
            this.interactables.add(pos);
        }

    }

    public EntityLiving getSpawnTarget() {
        return this.targetSpawn;
    }

    public void setSpawnTarget(EntityLiving newTarget) {
        this.targetSpawn = newTarget;
    }

    public int getContributedToSpawn() {
        return this.lifeContributedSoFar;
    }

    public int getNeededToSpawn() {
        return this.lifeNeeded;
    }

    public void addBerry(BlockPos position) {
        if (world.getBlockState(position).getBlock() instanceof HastoBerry) {
            this.tickRate--;
            if (this.tickRate < 1) {
                this.tickRate = 1L;
            }
        }
        if (world.getBlockState(position).getBlock() instanceof GlutoBerry) {
            this.glutoCount++;
        }
        if (world.getBlockState(position).getBlock() instanceof FuncoBerry) {
            this.funcoCount++;
        }
        if (world.getBlockState(position).getBlock() instanceof RezzoBerry) {
            this.rezzoCount++;
        }
        this.berries.add(position);
    }

    public void removeBerry(BlockPos position, String name) {
        if (name.equals("hastoberry")) {
            this.tickRate++;
            if (this.tickRate > 60) {
                this.tickRate = 60L;
            }
        }
        if (name.equals("glutoberry")) {
            this.glutoCount--;
        }
        if (name.equals("funcoberry")) {
            this.funcoCount--;
        }
        if (name.equals("rezzoberry")) {
            this.rezzoCount--;
        }
        this.berries.remove(position);
    }

    public void addInteractable(BlockPos position){
        this.interactables.add(position);
    }

    public void removeInteractable(BlockPos position) {
        this.interactables.remove(position);
    }

    public boolean hasFluids() {
        for (BlockPos bp : this.interactables) {
            IInteractable te = (IInteractable) world.getTileEntity(bp);
            if(te == null) continue;
            if(te.getType() != IInteractable.InteractableType.FLUID) continue;
            TEFluidHollow teFluidHollow = (TEFluidHollow) world.getTileEntity(bp);
            if (teFluidHollow.getFilled() != 0L) {
                return true;
            }
        }
        return false;
    }

    public boolean hasItems() {
        for (BlockPos bp : this.interactables) {
            IInteractable te = (IInteractable) world.getTileEntity(bp);
            if(te == null) continue;
            if(te.getType() != IInteractable.InteractableType.STORAGE) continue;
            TEStorageHollow teStorageHollow = (TEStorageHollow) world.getTileEntity(bp);
            if (!teStorageHollow.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    // Updated method to see if we can pull an itemstack out of inventories in this tree network
    public boolean canGetItemstackOut(ItemStack toFetch, boolean exact, boolean ignoreKeepers){
        int remaining = toFetch.getCount(); // We need to reduce this to zero in order to return the desired stack.

        for (BlockPos bp : this.interactables) {
            IInteractable te = (IInteractable) world.getTileEntity(bp);
            if(te == null) continue;
            if(te.getType() != IInteractable.InteractableType.STORAGE){
                if(ignoreKeepers) continue;
                if(te.getType() != IInteractable.InteractableType.KEEPER) continue;

                // We have a keeper. We should validate this inventory
                TEKeeper keeper = (TEKeeper)te;
                if(keeper.isIncludeInInventory()){
                    // Do check this inventory
                    ItemStack stack = keeper.getOutCopy();
                    if(exact){
                        // NBT comparison
                        if(StackUtil.stacksEqual(stack, toFetch)){
                            if(stack.getCount() >= remaining){
                                remaining = 0;
                                break;
                            }
                            remaining -= stack.getCount();
                        }
                    }else{
                        if(StackUtil.stacksShallowEqual(stack, toFetch)){
                            if(stack.getCount() >= remaining){
                                remaining = 0;
                                break;
                            }
                            remaining -= stack.getCount();
                        }
                    }
                }
                continue;
            }
            TEStorageHollow teStorageHollow = (TEStorageHollow) te;
            for (ItemStack stack : teStorageHollow.getItemStacks()) {
                if(exact){
                    // NBT comparison
                    if(StackUtil.stacksEqual(stack, toFetch)){
                        if(stack.getCount() >= remaining){
                            remaining = 0;
                            break;
                        }
                        remaining -= stack.getCount();
                    }
                }else{
                    if(StackUtil.stacksShallowEqual(stack, toFetch)){
                        if(stack.getCount() >= remaining){
                            remaining = 0;
                            break;
                        }
                        remaining -= stack.getCount();
                    }
                }
            }
        }

        if(remaining != 0){
            return false;
        }

        return true;
    }

    public ItemStack getItemstackOut(ItemStack toFetch, boolean exact, boolean ignoreKeepers){
        int remaining = toFetch.getCount(); // We need to reduce this to zero in order to return the desired stack.

        for (BlockPos bp : this.interactables) {
            IInteractable te = (IInteractable) world.getTileEntity(bp);
            if(te == null) continue;
            if(te.getType() != IInteractable.InteractableType.STORAGE){
                if(ignoreKeepers) continue;
                if(te.getType() != IInteractable.InteractableType.KEEPER) continue;

                // We have a keeper. We should validate this inventory
                TEKeeper keeper = (TEKeeper)te;
                if(keeper.isIncludeInInventory()){
                    // Do check this inventory
                    ItemStack stack = keeper.getOutCopy();
                    if(exact){
                        // NBT comparison
                        if(StackUtil.stacksEqual(stack, toFetch)){
                            if(stack.getCount() >= remaining){
                                keeper.decrStackSize(37, remaining);
                                remaining = 0;
                                break;
                            }
                            remaining -= stack.getCount();
                            keeper.setInventorySlotContents(37, ItemStack.EMPTY);
                        }
                    }else{
                        if(StackUtil.stacksShallowEqual(stack, toFetch)){
                            if(stack.getCount() >= remaining){
                                keeper.decrStackSize(37, remaining);
                                remaining = 0;
                                break;
                            }
                            remaining -= stack.getCount();
                            keeper.setInventorySlotContents(37, ItemStack.EMPTY);
                        }
                    }
                }
                continue;
            }
            TEStorageHollow teStorageHollow = (TEStorageHollow) te;
            for(int slot = 0; slot < teStorageHollow.getCapacity(); slot++) {
                ItemStack stack = teStorageHollow.getStackInSlot(slot);
                if(exact){
                    // NBT comparison
                    if(StackUtil.stacksEqual(stack, toFetch)){
                        if(stack.getCount() >= remaining){
                            teStorageHollow.decrStackSize(slot, remaining);
                            remaining = 0;
                            break;
                        }
                        remaining -= stack.getCount();
                        teStorageHollow.setInventorySlotContents(slot, ItemStack.EMPTY);
                    }
                }else{
                    if(StackUtil.stacksShallowEqual(stack, toFetch)){
                        if(stack.getCount() >= remaining){
                            teStorageHollow.decrStackSize(slot, remaining);
                            remaining = 0;
                            break;
                        }
                        remaining -= stack.getCount();
                        teStorageHollow.setInventorySlotContents(slot, ItemStack.EMPTY);
                    }
                }
            }
        }

        if(remaining != 0){
            return null;
        }

        return toFetch.copy();
    }

    public ItemStack getFirstItemstackFromInventoryMatching(ItemStack toGet) {
        for (BlockPos bp : this.interactables) {
            IInteractable te = (IInteractable) world.getTileEntity(bp);
            if(te == null) continue;
            if(te.getType() != IInteractable.InteractableType.STORAGE) continue;
            TEStorageHollow teStorageHollow = (TEStorageHollow) world.getTileEntity(bp);
            int i = 0;
            for (ItemStack stack : teStorageHollow.getItemStacks()) {
                if (StackUtil.stacksEqual(toGet, stack)) {
                    // We're good here. We can pull out, if *and only if* we have enough quantity.
                    if (stack.getCount() >= toGet.getCount()) {
                        // This is the proper item!
                        ItemStack toReturn = stack.splitStack(toGet.getCount());

                        if (stack.getCount() == 0) {
                            teStorageHollow.setInventorySlotContents(i, ItemStack.EMPTY);
                        }

                        return toReturn;
                    }
                }
                i++;
            }
        }
        return null;
    }

    public boolean hasRoomLeft() {
        for (BlockPos bp : this.interactables) {
            IInteractable te = (IInteractable) world.getTileEntity(bp);
            if(te == null) continue;
            if(te.getType() != IInteractable.InteractableType.STORAGE) continue;
            TEStorageHollow teStorageHollow = (TEStorageHollow) world.getTileEntity(bp);
            if (!teStorageHollow.isFull()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasFluidRoomLeft() {
        for (BlockPos bp : this.interactables) {
            IInteractable te = (IInteractable) world.getTileEntity(bp);
            if(te == null) continue;
            if(te.getType() != IInteractable.InteractableType.FLUID) continue;
            TEFluidHollow teStorageHollow = (TEFluidHollow) world.getTileEntity(bp);
            if (teStorageHollow.getFilled() != teStorageHollow.getCapacity()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether the various storage hollows attached to this block can fit this itemstack
     * @param stack The stack to check
     * @return Leftover itemstack
     */
    public ItemStack canFitItem(ItemStack stack){
        int amtToFit = stack.getCount();
        for (BlockPos bp : this.interactables) {
            IInteractable te = (IInteractable) world.getTileEntity(bp);
            if(te == null) continue;
            if(te.getType() != IInteractable.InteractableType.STORAGE) continue;
            TEStorageHollow teStorageHollow = (TEStorageHollow) world.getTileEntity(bp);

            ItemStack toTest = ItemHandlerHelper.copyStackWithSize(stack, amtToFit);

            toTest = teStorageHollow.canFit(toTest);

            if (toTest == ItemStack.EMPTY) {
                // We fit the whole stack
                return ItemStack.EMPTY;
            }else{
                amtToFit = toTest.getCount(); // The remaining amount
            }
        }
        return ItemHandlerHelper.copyStackWithSize(stack, amtToFit);
    }

    public ItemStack doFitItem(ItemStack stack){
        int amtToFit = stack.getCount();
        for (BlockPos bp : this.interactables) {
            IInteractable te = (IInteractable) world.getTileEntity(bp);
            if(te == null) continue;
            if(te.getType() != IInteractable.InteractableType.STORAGE) continue;
            TEStorageHollow teStorageHollow = (TEStorageHollow) world.getTileEntity(bp);

            ItemStack toTest = ItemHandlerHelper.copyStackWithSize(stack, amtToFit);
            toTest = teStorageHollow.doFitNoNewStacks(toTest);

            if (toTest == ItemStack.EMPTY) {
                // We fit the whole stack
                return ItemStack.EMPTY;
            }else{
                amtToFit = toTest.getCount(); // The remaining amount
            }
        }

        // Now that we've done our first pass of checks to see if it fits in without new stacks, let's do the second go
        // where we just try to fit it in

        for (BlockPos bp : this.interactables) {
            IInteractable te = (IInteractable) world.getTileEntity(bp);
            if(te == null) continue;
            if(te.getType() != IInteractable.InteractableType.STORAGE) continue;
            TEStorageHollow teStorageHollow = (TEStorageHollow) world.getTileEntity(bp);

            ItemStack toTest = ItemHandlerHelper.copyStackWithSize(stack, amtToFit);
            toTest = teStorageHollow.doFit(toTest);

            if (toTest == ItemStack.EMPTY) {
                // We fit the whole stack
                return ItemStack.EMPTY;
            }else{
                amtToFit = toTest.getCount(); // The remaining amount
            }
        }

        return ItemHandlerHelper.copyStackWithSize(stack, amtToFit);
    }

    // Additive liquid check. Can we remove this liquid, if we iterate through all fluid hollows?
    public boolean canRemoveAllLiquid(Fluid fluid, Long amt) {
        for (BlockPos bp : this.interactables) {
            IInteractable te = (IInteractable) world.getTileEntity(bp);
            if(te == null) continue;
            if(te.getType() != IInteractable.InteractableType.FLUID) continue;
            TEFluidHollow teFluidHollow = (TEFluidHollow) world.getTileEntity(bp);
            if (teFluidHollow.containsFluid(fluid)) {
                // This guy has some of that fluid.
                long amtFluid = teFluidHollow.amountFluid(fluid);

                if (amtFluid >= amt) {
                    return true;
                } else {
                    // We have less than needed
                    amt -= amtFluid;
                }
            }
        }
        return false;
    }

    public void doRemoveAllLiquid(Fluid fluid, Long amt) {
        for (BlockPos bp : this.interactables) {
            IInteractable te = (IInteractable) world.getTileEntity(bp);
            if(te.getType() != IInteractable.InteractableType.FLUID) continue;
            TEFluidHollow teFluidHollow = (TEFluidHollow) world.getTileEntity(bp);
            if (teFluidHollow.containsFluid(fluid)) {
                // This guy has some of that fluid.
                long amtFluid = teFluidHollow.amountFluid(fluid);

                if (amtFluid >= amt) {
                    teFluidHollow.removeFluid(fluid, amt);
                } else {
                    // We have less than needed
                    teFluidHollow.removeFluid(fluid, amtFluid);
                    amt -= amtFluid;
                }
            }
        }
    }

    public boolean canStoreLiquid(Fluid fluid, Long amt) {
        for (BlockPos bp : this.interactables) {
            IInteractable te = (IInteractable) world.getTileEntity(bp);
            if(te.getType() != IInteractable.InteractableType.FLUID) continue;
            TEFluidHollow teFluidHollow = (TEFluidHollow) world.getTileEntity(bp);
            if (teFluidHollow.getFilled() <= (teFluidHollow.getCapacity() - amt)) {
                // We're solid. We can fill it all entirely in here
                return true;
            } else if (teFluidHollow.getFilled() != teFluidHollow.getCapacity()) {
                // We have some room
                long amtCanHold = teFluidHollow.getCapacity() - teFluidHollow.getFilled();
                amt -= amtCanHold;
            }
        }
        return false;
    }

    public void doStoreLiquid(Fluid fluid, Long amt) {
        for (BlockPos bp : this.interactables) {
            IInteractable te = (IInteractable) world.getTileEntity(bp);
            if(te.getType() != IInteractable.InteractableType.FLUID) continue;
            TEFluidHollow teFluidHollow = (TEFluidHollow) world.getTileEntity(bp);
            if (teFluidHollow.getFilled() <= (teFluidHollow.getCapacity() - amt)) {
                // We're solid. We can fill it all entirely in here
                teFluidHollow.addFluid(fluid, amt);
                return;
            } else if (teFluidHollow.getFilled() != teFluidHollow.getCapacity()) {
                // We have some room
                long amtCanHold = teFluidHollow.getCapacity() - teFluidHollow.getFilled();
                teFluidHollow.addFluid(fluid, amtCanHold);
                amt -= amtCanHold;
            }
        }
    }

    public void reIndexMaxLife() {
        maxLife = (double) (2000 * TreeUtil.findAllTreeBlocks(world, pos).size());
    }

    @Override
    public void update() {
        if (!this.hasWorld()) return;  // prevent crash
        World world = this.getWorld();
        if (world.isRemote) return;   // don't bother doing anything on the client side.

        // Tick slowing thing
        tickcount++;
        if (tickcount == Long.MAX_VALUE) {
            tickcount = 0L;
        }

        try {
            if ((tickcount % tickRate) != 0) {
                // Timer not up yet, continue
                return;
            }
        } catch (ArithmeticException e) {
            tickRate = 60L;
            return;
        }

        if ((tickcount % 1200) == 0) {
            reIndexMaxLife();
        }

//		this.markDirty();            // if you update a tileentity variable on the server and this should be communicated to the client,
// 																		you need to markDirty() to force a resend.  In this case, the client doesn't need to know

        // Item checks
        if (hasRoomLeft()) {
            // We can pull things, maybe!
            for (BlockPos pos : interactables) {
                IInteractable te = (IInteractable) ((TileEntity) world.getTileEntity(pos));
                if(te == null) continue;
                if(te.getType() != IInteractable.InteractableType.PULLER) continue;
                if (world.getBlockState(pos).getBlock() instanceof BlockPuller) {
                    TEPuller tePuller = (TEPuller) world.getTileEntity(pos);
                    if (tePuller == null) continue;
                    // We need to get the item filter, here.
                    ItemStack[] filter = tePuller.getStacks();
                    // We're in good shape. Get it's facing
                    EnumFacing direction = world.getBlockState(pos).getValue(BlockPuller.FACING);
                    EnumFacing blockFace = direction.getOpposite();
                    BlockPos offset = pos.offset(direction);
                    // Allows us to stack functional blocks
                    while (world.getBlockState(offset).getBlock() instanceof BlockPuller || world.getBlockState(offset).getBlock() instanceof BlockPusher) {
                        offset = offset.offset(direction);
                    }
                    if (world.isAirBlock(offset)) {
                        // There is no block here.
                        continue;
                    }

                    // We are ready to pull, but do we have enough life? Maybe.
                    // This call will pull one item. We'll make one item cost 200. A stack of items is now 12,800 life
                    if (this.getLife() < (200 * (funcoCount + 1))) {
                        continue;
                    }
                    boolean success = pullItems(offset, blockFace, filter);

                    if (success) {
                        this.setLife(getLife() - (200 * (funcoCount + 1)));
                    }
                }
            }
        }

        if (hasItems()) {
            for (BlockPos pos : interactables) {
                IInteractable te = (IInteractable) world.getTileEntity(pos);
                if(te == null) continue;
                if(te.getType() != IInteractable.InteractableType.PUSHER) continue;
                if (world.getBlockState(pos).getBlock() instanceof BlockPusher) {
                    TEPusher tePusher = (TEPusher) world.getTileEntity(pos);
                    if (tePusher == null) continue;

                    ItemStack[] filter = tePusher.getStacks();

                    EnumFacing direction = world.getBlockState(pos).getValue(BlockPuller.FACING);
                    EnumFacing blockface = direction.getOpposite();
                    BlockPos offset = pos.offset(direction);

                    while (world.getBlockState(offset).getBlock() instanceof BlockPuller || world.getBlockState(offset).getBlock() instanceof BlockPusher) {
                        offset = offset.offset(direction);
                    }
                    if (world.isAirBlock(offset)) {
                        continue;
                    }

                    // What was true for pulling is also true for pushing
                    if (this.getLife() < (200 * (funcoCount + 1))) {
                        continue;
                    }
                    boolean success = pushItemsIfPossible(offset, blockface, filter);
                    if (success) this.setLife(getLife() - (200 * (funcoCount + 1)));
                }
            }
        }

        // Let's do fluid stuff too!
        if (hasFluidRoomLeft()) {
            // We can try to pull fluids, if we are capable of pulling fluids.

            for (BlockPos pos : interactables) {
                IInteractable te = (IInteractable) world.getTileEntity(pos);
                if(te == null) continue;
                if(te.getType() != IInteractable.InteractableType.PULLER) continue;
                if (world.getBlockState(pos).getBlock() instanceof BlockPuller) {
                    TEPuller tePuller = (TEPuller) world.getTileEntity(pos);
                    if (tePuller == null) continue;

                    ItemStack[] filter = tePuller.getStacks();
                    // We're in good shape. Get it's facing
                    EnumFacing direction = world.getBlockState(pos).getValue(BlockPuller.FACING);
                    EnumFacing blockFace = direction.getOpposite();
                    BlockPos offset = pos.offset(direction);
                    while (world.getBlockState(offset).getBlock() instanceof BlockPuller || world.getBlockState(offset).getBlock() instanceof BlockPusher) {
                        offset = offset.offset(direction);
                    }
                    if (world.isAirBlock(offset)) {
                        // There is no block here.
                        continue;
                    }

                    // We are ready to pull, but do we have enough life? Maybe.
                    // This call will pull one item. We'll make one item cost 200. A stack of items is now 12,800 life
                    if (this.getLife() < (200 * (funcoCount + 1))) {
                        continue;
                    }
                    boolean success = pullLiquids(offset, blockFace, filter);

                    if (success) {
                        this.setLife(getLife() - (200 * (funcoCount + 1)));
                    }
                }
            }
        }

        // Let's try fluid pushing, too!
        if (hasFluids()) {
            for (BlockPos pos : interactables) {
                IInteractable te = (IInteractable) world.getTileEntity(pos);
                if(te == null) continue;
                if(te.getType() != IInteractable.InteractableType.PUSHER) continue;
                if (world.getBlockState(pos).getBlock() instanceof BlockPusher) {
                    TEPusher tePusher = (TEPusher) world.getTileEntity(pos);
                    if (tePusher == null) continue;

                    ItemStack[] filter = tePusher.getStacks();
                    // We're in good shape. Get it's facing
                    EnumFacing direction = world.getBlockState(pos).getValue(BlockPuller.FACING);
                    EnumFacing blockFace = direction.getOpposite();
                    BlockPos offset = pos.offset(direction);
                    while (world.getBlockState(offset).getBlock() instanceof BlockPuller || world.getBlockState(offset).getBlock() instanceof BlockPusher) {
                        offset = offset.offset(direction);
                    }
                    if (world.isAirBlock(offset)) {
                        // There is no block here.
                        continue;
                    }

                    // We are ready to push, but do we have enough life? Maybe.
                    // This call will pull one item. We'll make one item cost 200. A stack of items is now 12,800 life
                    if (this.getLife() < (200 * (funcoCount + 1))) {
                        continue;
                    }
                    boolean success = pushLiquids(offset, blockFace, filter);

                    if (success) {
                        this.setLife(getLife() - (200 * (funcoCount + 1)));
                    }
                }
            }
        }

        pullLifeIntoSystem();

        if (rezzoCount > 0) {
            // We can do some work towards spawning a creature.
            if (targetSpawn == null) {
                selectSpawnTarget();
                return;
            }

            int lifeToAdd = (rezzoCount * 50);
            if (lifeToAdd > (lifeNeeded - lifeContributedSoFar)) {
                lifeToAdd = (lifeNeeded - lifeContributedSoFar);
            }
            setLife(getLife() - lifeToAdd);
            lifeContributedSoFar += lifeToAdd;
            if (lifeContributedSoFar.equals(lifeNeeded)) {
                // We have done enough. Let's spawn this gosh darn mob!
                // Pick a random root block
                BlockPos root = this.roots.get(world.rand.nextInt(this.roots.size()));
                BlockPos spawnPos = root.up(4);
                targetSpawn.setPosition(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
                targetSpawn.setHealth(targetSpawn.getMaxHealth());
                try {
                    if (targetSpawn == null || world == null) {
                        return;
                    }
                    world.spawnEntity(targetSpawn);
                    targetSpawn = null;
                } catch (Exception e) {
                    return;
                }
            }
        }
    }

    private void pullLifeIntoSystem() {
        // Scan through all still connected root blocks and see if we can pull something here.
        if (rezzoCount == 0) { // Rezoberries disable this
            for (BlockPos bp : roots) {
                List<EntityLiving> nearbyEntities = world.getEntities(EntityLiving.class, new Predicate<EntityLiving>() {
                    @Override
                    public boolean apply(@Nullable EntityLiving input) {
                        return (Math.sqrt(input.getPosition().distanceSq(bp.getX(), bp.getY(), bp.getZ())) < 5D) && ((input.getHealth() / input.getMaxHealth()) > 0.5F);
                    }
                });
                for (EntityLiving near : nearbyEntities) {
                    double amt = LifeUtil.getLifeForEntity(near);
                    this.setLife(getLife() + amt);
                    if (this.life > this.maxLife) {
                        this.life = this.maxLife;
                        return;
                    }
                    LifeUtil.deductLifeFromEntity(near);
                }
            }
        }

        if (glutoCount > 0) {
            for (BlockPos bp : roots) {
                // We can feed on some items
                List<EntityItem> nearbyItems = world.getEntities(EntityItem.class, new Predicate<EntityItem>() {
                    @Override
                    public boolean apply(@Nullable EntityItem input) {
                        return (Math.sqrt(input.getPosition().distanceSq(bp.getX(), bp.getY(), bp.getZ())) < 5D) && (input.getItem().getItem() instanceof ItemFood);
                    }
                });
                // See if we have any food nearby
                for (EntityItem i : nearbyItems) {
                    ItemStack stack = i.getItem();
                    int amt = stack.getCount();
                    ItemFood food = (ItemFood) stack.getItem();
                    int healAmt = food.getHealAmount(stack);
                    float saturation = food.getSaturationModifier(stack);
                    int totalLife = (int) ((healAmt * (10 + glutoCount)) + (saturation * (40 + glutoCount))) * amt;
                    this.setLife(getLife() + (totalLife));
                    if (this.life > this.maxLife) {
                        this.life = this.maxLife;
                        return;
                    }
                    i.setDropItemsWhenDead(false);
                    i.setDead();
                    world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ENTITY_PLAYER_BURP, SoundCategory.PLAYERS, 0.5F, world.rand.nextFloat() * 0.1F + 0.9F);
                }
            }
        }


    }

    public double getLife() {
        return this.life;
    }

    public void setLife(double amt) {
        this.life = amt;
    }

    public double getMaxLife() {
        return this.maxLife;
    }

    private boolean itemContainedInStorage(ItemStack stackToFind) {
        for (BlockPos storage : interactables) {
            IInteractable te = (IInteractable) world.getTileEntity(storage);
            if(te == null) continue;
            if(te.getType() != IInteractable.InteractableType.STORAGE) continue;
            TEStorageHollow storageHollow = (TEStorageHollow) world.getTileEntity(storage);
            for (int i = 0; i < storageHollow.getSizeInventory(); ++i) {
                ItemStack stack = storageHollow.getStackInSlot(i);
                if (stack.isItemEqual(stackToFind)) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<ItemStack> allItemsInStorage() {
        List<ItemStack> toReturn = new ArrayList<ItemStack>();
        for (BlockPos storage : interactables) {
            IInteractable te = (IInteractable) world.getTileEntity(storage);
            if(te == null) continue;
            if(te.getType() != IInteractable.InteractableType.STORAGE) continue;
            TEStorageHollow storageHollow = (TEStorageHollow) world.getTileEntity(storage);
            for (int i = 0; i < storageHollow.getSizeInventory(); ++i) {
                ItemStack stack = storageHollow.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    toReturn.add(stack.copy());
                }
            }
        }
        return toReturn;
    }

    private boolean pushItemsIfPossible(BlockPos place, EnumFacing face, ItemStack[] filter) {
        IInventory target = TileEntityHopper.getInventoryAtPosition(world, place.getX(), place.getY(), place.getZ());
        boolean itemWasPushed = false;
        if (target == null) {
            return false;
        } else {
            // There is an inventory here
            if (this.isInventoryFull(target, face)) {
                // It's full
                return false;
            } else {
                // See if we can find the items that go in here

                int amtToPull = 1 + this.funcoCount;
                int count = 0;
                for (BlockPos storage : interactables) {
                    IInteractable te = (IInteractable) world.getTileEntity(storage);
                    if(te.getType() != IInteractable.InteractableType.STORAGE) continue;
                    TEStorageHollow storageHollow = (TEStorageHollow) world.getTileEntity(storage);
                    for (int i = 0; i < storageHollow.getSizeInventory(); ++i) {
                        ItemStack stack = storageHollow.getStackInSlot(i);
                        if (!matchesFilter(stack, new CompareOptions(filter))) continue;
                        if (!stack.isEmpty()) {
                            ItemStack itemstack = storageHollow.getStackInSlot(i).copy().splitStack(1); // Get one item off the top

                            TileEntityHopper.putStackInInventoryAllSlots(storageHollow, target, itemstack, face);
                            itemWasPushed = true;
                            ItemStack second = storageHollow.decrStackSize(i, 1);
                            if (second.isEmpty()) {
                                storageHollow.removeStackFromSlot(i);
                            }
                            count++;
                            if (count == amtToPull) {
                                break;
                            }
                        }
                    }
                }

                if (count == amtToPull)
                    return itemWasPushed;

                // See if we can output a crafted material? Sounds good
                tryAutocraft(filter, count, amtToPull, target, face);
            }
        }
        return itemWasPushed;
    }

    public synchronized List<ItemStack> getMissingItemsToCraft(ItemStack goal) {
        List<ItemStack> alreadyHave = allItemsInStorage();
        List<CraftTreeBuilder.DirectionalItemStack> directionalItemStacks = CraftTreeBuilder.findProcessToMake(goal, alreadyHave);

        if (directionalItemStacks == null) {
            List<ItemStack> missing = CraftTreeBuilder.findMissingItems(goal, alreadyHave);
            return missing;
        }
        return new ArrayList<>();
    }

    // Will simply autocraft the item into a storage hollow
    public synchronized boolean autocraftIfPossible(List<ItemStack> possibilities) {
        List<ItemStack> alreadyHave = allItemsInStorage();
        for (ItemStack is : possibilities) {
            List<CraftTreeBuilder.DirectionalItemStack> directionalItemStacks = CraftTreeBuilder.findProcessToMake(is, alreadyHave);

            if (directionalItemStacks == null) {
                continue;
            }

            // It was done. Include craftoberries here, please.
            for (CraftTreeBuilder.DirectionalItemStack dir : directionalItemStacks) {
                if (dir.isAdd()) {
                    // Add an item to this block
                    if(canFitItem(dir.getStack()) != ItemStack.EMPTY){
                        // We can't fit the intermediate stages of crafting
                        return false;
                    }
                    doFitItem(dir.getStack());
                } else {
                    tryRemoveItemFromInventory(dir.getStack());
                }
            }
            ItemStack result = is.copy();
            ThingsOfNaturalEnergies.logger.error("The stack has " + result.getCount() + " items in it");
            if(canFitItem(result) != ItemStack.EMPTY){
                // We can't fit the final stages of crafting
                return false;
            }
            doFitItem(result);
            alreadyHave = allItemsInStorage();
            return true;
        }
        return false;
    }

    private void tryAutocraft(ItemStack[] filter, int count, int amtToPull, IInventory target, EnumFacing face) {
        for (ItemStack is : filter) {
            if (is.isEmpty()) continue;
            if (!(is.getItem() instanceof ItemToken) && !itemContainedInStorage(is)) {
                // It's not a token, so we can try to craft it!
                boolean result = autocraftIfPossible(Arrays.asList(new ItemStack[]{is}));
                if (result) {
                    count++;
                    if (count == amtToPull) {
                        break;
                    }
                }
            }
        }
    }

    private TEStorageHollow tryRemoveItemFromInventory(ItemStack stackToRemove) {
        for (BlockPos storage : interactables) {
            IInteractable te = (IInteractable) world.getTileEntity(storage);
            if(te.getType() != IInteractable.InteractableType.STORAGE) continue;
            TEStorageHollow storageHollow = (TEStorageHollow) world.getTileEntity(storage);
            for (int i = 0; i < storageHollow.getSizeInventory(); ++i) {
                ItemStack stack = storageHollow.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    if (!StackUtil.stacksEqual(stack, stackToRemove)) {
                        continue;
                    }
                    ItemStack second = storageHollow.decrStackSize(i, stackToRemove.getCount());
                    if (second.isEmpty()) {
                        storageHollow.removeStackFromSlot(i);
                    }
                    return storageHollow;
                }
            }
        }
        return null;
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

    private static boolean canExtractItemFromSlot(IInventory inventoryIn, ItemStack stack, int index, EnumFacing side) {
        return !(inventoryIn instanceof ISidedInventory) || ((ISidedInventory) inventoryIn).canExtractItem(index, stack, side);
    }

    private boolean compareItems(ItemStack toCheck, ItemStack filter, CompareOptions co) {
        if (filter == null) {
            // We're checking for broad category type filters.
            /*
            public boolean pullAll = false; // Pull all items?
        public boolean invert = false; // If true, all values will be inverted. "ex: pull NOT full stacks, pull NOT named this"
        public boolean compareItemType = true; // Should the item type be compared. On by default.
        public boolean compareItemAmount = false; // Compare the amount of items? Useful if you want to pull, say, a stack at a time.
        public boolean compareItemName = false; // Compare the literal name of the items for match?
        public boolean compareItemNBT = false; // Should NBT data match?
        public boolean pullAnyNBT = false; // Should pull any item with nbt data?
        public boolean compareEnchantment = false; // Should it have the same enchantment?
        public boolean pullAnyEnchantment = false; // Pull any random enchantment?
             */
            if (co.pullAll) return true;
            if (co.pullAnyEnchantment || co.pullAnyNBT) {
                if (toCheck.hasTagCompound()) {
                    return true;
                }
            }
        } else {
            // We have two items to compare
            // 'toCheck' is the item we're checking, filter is the item we're filtering
            boolean resultSoFar = true;
            if (co.compareItemType) {
                if (toCheck.getItem() != filter.getItem()) {
                    resultSoFar = false;
                }
            }
            if (co.compareItemName) {
                if (!toCheck.getDisplayName().equals(filter.getDisplayName())) {
                    resultSoFar = false;
                }
            }
            if (co.compareItemAmount) {
                if (toCheck.getCount() != filter.getCount()) {
                    resultSoFar = false;
                }
            }
            if (co.compareMeta) {
                if (toCheck.getMetadata() != filter.getMetadata()) {
                    resultSoFar = false;
                }
            }
            if (co.compareItemNBT || co.compareEnchantment) {
                if (!toCheck.getEnchantmentTagList().equals(filter.getEnchantmentTagList())) {
                    resultSoFar = false;
                }
            }
            return co.invert ? !resultSoFar : resultSoFar;
        }
        return false;
    }

    private boolean matchesFilter(ItemStack item, CompareOptions co) {
        if (compareItems(item, null, co)) return true;

        for (ItemStack stack : co.filterItems) {
            if (compareItems(item, stack, co)) {
                return true;
            }
        }
        return false;
    }

    private boolean pushLiquids(BlockPos place, EnumFacing face, ItemStack[] filter) {
        IFluidHandler handler = FluidUtil.getFluidHandler(world, place, face);
        if (handler == null) {
            // We cannot pull from this block, at all. So return
            ThingsOfNaturalEnergies.logger.error("No fluid handler there");
            return false;
        }

        // This is indeed a fluid handler. It might not have one of our fluids, though.

        int amtToPush = 1000 + (this.funcoCount * 1000);
        int count = 0;

        // Go through the filter - for each slot checking if it holds some kind of fluid
        // if so, see if we can add that fluid to the handler.
        for (ItemStack stack : filter) {
            Fluid f = null;

            FluidStack heldInBucket = FluidUtil.getFluidContained(stack);
            if (heldInBucket == null) {
                // Was not a liquid bucket
                continue;
            }
            f = heldInBucket.getFluid();
            ThingsOfNaturalEnergies.logger.error("We're trying to push " + f.getName());

            int attemptedDrain = handler.fill(new FluidStack(f, amtToPush - count), false);
            // Only simulate. There's a chance we can't actually do that
            if (attemptedDrain == 0) {
                // This handler didn't allow us to push this fluid out.
                ThingsOfNaturalEnergies.logger.error("Didn't let us push");
                continue;
            }
            // We were able to push it
            ThingsOfNaturalEnergies.logger.error("It can push");
            boolean worked = canRemoveAllLiquid(f, (long) amtToPush - count);
            if (!worked) {
                ThingsOfNaturalEnergies.logger.error("But we can't steal that liquid");
                return false;
            } else {
                // It did work
                handler.fill(new FluidStack(f, amtToPush - count), true);
            }
            ThingsOfNaturalEnergies.logger.error("And we can remove that liquid");
            doRemoveAllLiquid(f, (long) amtToPush - count);
            world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.PLAYERS, 1.0F, world.rand.nextFloat() * 0.1F + 0.9F);
            count += attemptedDrain;

            if (count == amtToPush) {
                break;
            }
        }

        if (count == 0) {
            // We pulled nothing.
            return false;
        }

        return true;
    }

    private boolean pullLiquids(BlockPos place, EnumFacing face, ItemStack[] filter) {
        IFluidHandler handler = FluidUtil.getFluidHandler(world, place, face);
        if (handler == null) {
            // We cannot pull from this block, at all. So return
            return false;
        }

        // This is indeed a fluid handler. It might not have one of our fluids, though.

        int amtToPull = 1000 + (this.funcoCount * 1000);
        int count = 0;

        // Go through the filter - for each slot checking if it holds some kind of fluid
        // if so, see if we can drain that fluid from the handler.
        for (ItemStack stack : filter) {
            Fluid f = null;

            FluidStack heldInBucket = FluidUtil.getFluidContained(stack);
            if (heldInBucket == null) {
                // Was not a liquid bucket
                continue;
            }
            f = heldInBucket.getFluid();

            FluidStack attemptedDrain = handler.drain(new FluidStack(f, amtToPull - count), false);
            boolean specialDrain = false;
            if (attemptedDrain == null) {
                // This handler didn't have one to the amount we would like to pull, how about if it's a fluid block?
                // Those will only allow us to pull a bucket at once. If the life charge is proportional, that's not too
                // sha
                attemptedDrain = handler.drain(1000, false);
                if (attemptedDrain == null) {
                    continue;
                } else {
                    specialDrain = true;
                }
            }
            // We were able to pull itboolean worked = storeLiquidInFirstOpenSlot(attemptedDrain.getFluid(), (long) attemptedDrain.amount);
            boolean worked = canStoreLiquid(attemptedDrain.getFluid(), (long) attemptedDrain.amount);
            if (!worked) {
                // We can't actually fit this item in our inventory (which should be reflected by now, but just in case)
                return false;
            } else {
                //It did work, actually drain it
                if (!specialDrain) {
                    handler.drain(new FluidStack(f, amtToPull - count), true);
                } else {
                    handler.drain(1000, true);
                }
            }
            doStoreLiquid(attemptedDrain.getFluid(), (long) attemptedDrain.amount);
            world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ITEM_BUCKET_FILL, SoundCategory.PLAYERS, 1.0F, world.rand.nextFloat() * 0.1F + 0.9F);
            count += attemptedDrain.amount;

            if (count == amtToPull) {
                break;
            }
        }

        if (count == 0) {
            // We pulled nothing.
            return false;
        }

        return true;
    }

    public int getOverallSizeInventory(){
        int totalCount = 0;
        for (BlockPos storage : interactables) {
            IInteractable te = (IInteractable) world.getTileEntity(storage);
            if(te == null) continue;
            if (te.getType() != IInteractable.InteractableType.STORAGE){
                if(te.getType() == IInteractable.InteractableType.KEEPER){
                    // This keeper might be worth including.
                    TEKeeper keeper = (TEKeeper) te;
                    if(keeper.isIncludeInInventory()){
                        // We should include it!
                        totalCount += 1;
                    }
                }
                continue;
            }
            TEStorageHollow storageHollow = (TEStorageHollow) te;
            totalCount += storageHollow.getSizeInventory();
        }
        return totalCount;
    }

    public ItemStack getOverallStackInSlot(int desiredSlot){
        for (BlockPos storage : interactables) {
            IInteractable te = (IInteractable) world.getTileEntity(storage);
            if(te == null) continue;
            if(te.getType() == IInteractable.InteractableType.STORAGE){
                TEStorageHollow storageHollow = (TEStorageHollow) te;
                if(desiredSlot >= storageHollow.getCapacity()){
                    desiredSlot -= storageHollow.getCapacity();
                    continue;
                }else{
                    return storageHollow.getStackInSlot(desiredSlot);
                }
            }else{
                if(te.getType() == IInteractable.InteractableType.KEEPER){
                    TEKeeper keeper = (TEKeeper) te;
                    if(keeper.isIncludeInInventory()){
                        // We should include it!
                        if(desiredSlot > 0){
                            desiredSlot -= 1;
                            continue;
                        }else{
                            return ((TEKeeper) te).getStackInSlot(37);
                        }
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    public ItemStack overallDecrStackSize(int desiredSlot, int count){
        for (BlockPos storage : interactables) {
            IInteractable te = (IInteractable) world.getTileEntity(storage);
            if(te == null) continue;
            if(te.getType() == IInteractable.InteractableType.STORAGE){
                TEStorageHollow storageHollow = (TEStorageHollow) te;
                if(desiredSlot >= storageHollow.getCapacity()){
                    desiredSlot -= storageHollow.getCapacity();
                    continue;
                }else{
                    return storageHollow.decrStackSize(desiredSlot, count);
                }
            }else{
                if(te.getType() == IInteractable.InteractableType.KEEPER){
                    TEKeeper keeper = (TEKeeper) te;
                    if(keeper.isIncludeInInventory()) {
                        if (desiredSlot > 0) {
                            desiredSlot -= 1;
                            continue;
                        } else {
                            return ((TEKeeper) te).decrStackSize(37, count);
                        }
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    public ItemStack overallRemoveStack(int desiredSlot){
        for (BlockPos storage : interactables) {
            IInteractable te = (IInteractable) world.getTileEntity(storage);
            if(te == null) continue;
            if(te.getType() == IInteractable.InteractableType.STORAGE){
                TEStorageHollow storageHollow = (TEStorageHollow) te;
                if(desiredSlot >= storageHollow.getCapacity()){
                    desiredSlot -= storageHollow.getCapacity();
                    continue;
                }else{
                    return storageHollow.removeStackFromSlot(desiredSlot);
                }
            }else{
                if(te.getType() == IInteractable.InteractableType.KEEPER){
                    TEKeeper keeper = (TEKeeper) te;
                    if(keeper.isIncludeInInventory()) {
                        if (desiredSlot > 0) {
                            desiredSlot -= 1;
                            continue;
                        } else {
                            return ((TEKeeper) te).removeStackFromSlot(37);
                        }
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    public int overallStackMax(int desiredSlot, ItemStack stack){
        for (BlockPos storage : interactables) {
            IInteractable te = (IInteractable) world.getTileEntity(storage);
            if(te == null) continue;
            if(te.getType() == IInteractable.InteractableType.STORAGE){
                TEStorageHollow storageHollow = (TEStorageHollow) te;
                if(desiredSlot >= storageHollow.getCapacity()){
                    desiredSlot -= storageHollow.getCapacity();
                    continue;
                }else{
                    return storageHollow.getInventoryStackLimit();
                }
            }else{
                if(te.getType() == IInteractable.InteractableType.KEEPER){
                    TEKeeper keeper = (TEKeeper) te;
                    if(keeper.isIncludeInInventory()) {
                        if (desiredSlot > 0) {
                            desiredSlot -= 1;
                            continue;
                        } else {
                            // We do actually want to put it right here.
                            // We can't do that, though
                            return 0;
                        }
                    }
                }
            }
        }
        return -1; // Unimplemented
    }

    public void overallSetContents(int desiredSlot, ItemStack stack){
        for (BlockPos storage : interactables) {
            IInteractable te = (IInteractable) world.getTileEntity(storage);
            if(te == null) continue;
            if(te.getType() == IInteractable.InteractableType.STORAGE){
                TEStorageHollow storageHollow = (TEStorageHollow) te;
                if(desiredSlot >= storageHollow.getCapacity()){
                    desiredSlot -= storageHollow.getCapacity();
                    continue;
                }else{
                    storageHollow.setInventorySlotContents(desiredSlot, stack);
                    return;
                }
            }else{
                if(te.getType() == IInteractable.InteractableType.KEEPER){

                    TEKeeper keeper = (TEKeeper) te;
                    if(keeper.isIncludeInInventory()) {
                        if (desiredSlot > 0) {
                            desiredSlot -= 1;
                            continue;
                        } else {
                            if (stack.isEmpty()) {
                                // We can set this to empty, sure
                                ((TEKeeper) te).setInventorySlotContents(37, ItemStack.EMPTY);
                                return;
                            }
                            // We do actually want to put it right here.
                            // We can't do that, though
                            return;
                        }
                    }
                }
            }
        }
    }

    private boolean pullItems(BlockPos place, EnumFacing face, ItemStack[] filter) {
        IInventory target = TileEntityHopper.getInventoryAtPosition(world, place.getX(), place.getY(), place.getZ());
        boolean itemWasPulled = false;
        if (target != null) {
            // We can see this inventory
            if (isInventoryEmpty(target, face)) {
                // We could not pull an item from this block
                return false;
            }

            int amtToPull = 1 + this.funcoCount;
            int count = 0;
            if (target instanceof ISidedInventory) {
                ISidedInventory iSidedInventory = (ISidedInventory) target;
                int[] aint = iSidedInventory.getSlotsForFace(face);
                for (int i : aint) {
                    ItemStack itemstack = target.getStackInSlot(i);

                    if (!itemstack.isEmpty() && canExtractItemFromSlot(target, itemstack, i, face)) {
                        // We're good - theoretically, for this item, but does it match the filter?
                        if (!matchesFilter(itemstack, new CompareOptions(filter))) continue;
                        ItemStack one = itemstack.copy().splitStack(1);
                        doFitItem(one);
                        itemWasPulled = true;
                        ItemStack second = target.decrStackSize(i, 1);
                        if (second.isEmpty()) {
                            target.removeStackFromSlot(i);
                        }
                        count++;
                        if (count == amtToPull) {
                            break;
                        }
                    }
                }
            } else {
                int j = target.getSizeInventory();

                for (int k = 0; k < j; ++k) {
                    ItemStack itemstack = target.getStackInSlot(k);

                    if (!itemstack.isEmpty() && canExtractItemFromSlot(target, itemstack, k, face)) {
                        // We're good - theoretically, for this item, but does it match the filter?
                        if (!matchesFilter(itemstack, new CompareOptions(filter))) continue;
                        // We're good. Let's store it.
                        ItemStack one = itemstack.copy().splitStack(1);

                        doFitItem(one);
                        itemWasPulled = true;
                        ItemStack second = target.decrStackSize(k, 1);
                        if (second.isEmpty()) {
                            target.removeStackFromSlot(k);
                        }
                        count++;
                        if (count == amtToPull) {
                            break;
                        }
                    }
                }
            }
        } else {
            // There is no inventory here
            return false;
        }
        return itemWasPulled;
    }

    private static boolean isInventoryEmpty(IInventory inventoryIn, EnumFacing side) {
        if (inventoryIn instanceof ISidedInventory) {
            ISidedInventory isidedinventory = (ISidedInventory) inventoryIn;
            int[] aint = isidedinventory.getSlotsForFace(side);

            for (int i : aint) {
                if (!isidedinventory.getStackInSlot(i).isEmpty()) {
                    return false;
                }
            }
        } else {
            int j = inventoryIn.getSizeInventory();

            for (int k = 0; k < j; ++k) {
                if (!inventoryIn.getStackInSlot(k).isEmpty()) {
                    return false;
                }
            }
        }

        return true;
    }

    // These define the behavior of the comparison algorithm. The puller can ONLY pull items which match those things
    // put into its inventory, unless you add special tokens that change behavior.
    private class CompareOptions {

        public boolean pullAll = false; // Pull all items?
        public boolean invert = false; // If true, all values will be inverted. "ex: pull NOT full stacks, pull NOT named this"
        public boolean compareMeta = true;
        public boolean compareItemType = true; // Should the item type be compared. On by default.
        public boolean compareItemAmount = false; // Compare the amount of items? Useful if you want to pull, say, a stack at a time.
        public boolean compareItemName = false; // Compare the literal name of the items for match?
        public boolean compareItemNBT = false; // Should NBT data match?
        public boolean pullAnyNBT = false; // Should pull any item with nbt data?
        public boolean compareEnchantment = false; // Should it have the same enchantment?
        public boolean pullAnyEnchantment = false; // Pull any random enchantment?

        public ItemStack[] filterItems;

        public CompareOptions(ItemStack[] filterItems) {
            this.filterItems = filterItems;

            ArrayList<ItemStack> withSpecialsRemoved = new ArrayList<>();
            // TODO go through items in the filter list, find any special things and mark those flags.
            for (ItemStack is : filterItems) {
                if (is.getItem() == ModItems.tokenPullAll) {
                    // We don't need this one
                    pullAll = true;
                    continue;
                }
                withSpecialsRemoved.add(is);
            }

            ItemStack[] newFilter = new ItemStack[withSpecialsRemoved.size()];
            for (int i = 0; i < newFilter.length; i++) {
                newFilter[i] = withSpecialsRemoved.get(i);
            }
            this.filterItems = newFilter;
        }

    }

}
