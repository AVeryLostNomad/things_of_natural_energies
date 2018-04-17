package com.thelostnomad.tone.block.tileentity;

import com.thelostnomad.tone.util.world.IInteractable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TEFluidHollow extends TileEntity implements IInteractable, ICapabilityProvider {

    public static final String NAME = "tone_fluidhollow_tileentity";
    private BlockPos coreLocation = null;

    // For every given fluid in this hollow, how many millibuckets do we have
    private Map<Fluid, Long> millibucketsByFluid = new HashMap<Fluid, Long>();

    private HollowType storageLevel;

    public TEFluidHollow(){}

    public void setStorageLevel(HollowType type){
        this.storageLevel = type;
    }

    public long getFilled(){
        long total = 0L;
        for(Map.Entry<Fluid, Long> e : millibucketsByFluid.entrySet()){
            total+=e.getValue();
        }
        return total;
    }

    public long getCapacity(){
        return (long) this.storageLevel.size;
    }

    public void addFluid(Fluid f, Long millibuckets){
        if(millibucketsByFluid.containsKey(f)){
            millibucketsByFluid.put(f, millibucketsByFluid.get(f) + millibuckets);
        }else{
            millibucketsByFluid.put(f, millibuckets);
        }
    }

    public void setCoreLocation(BlockPos core){
        this.coreLocation = core;
    }

    public BlockPos getCoreLocation(){
        return this.coreLocation;
    }

    public Set<Fluid> getFluids(){
        return millibucketsByFluid.keySet();
    }

    public boolean containsFluid(Fluid f){
        return millibucketsByFluid.containsKey(f);
    }

    public long amountFluid(Fluid f){
        return millibucketsByFluid.get(f);
    }

    public void removeFluid(Fluid f, Long millibuckets){
        millibucketsByFluid.put(f, millibucketsByFluid.get(f) - millibuckets);
        if(amountFluid(f) == 0){
            millibucketsByFluid.remove(f);
        }
    }

    // This is where you save any data that you don't want to lose when the tile entity unloads
    // In this case, it saves the itemstacks stored in the container
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound parentNBTTagCompound) {
        super.writeToNBT(parentNBTTagCompound); // The super call is required to save and load the tileEntity's location

        NBTTagString storageType = new NBTTagString(storageLevel.name);

        NBTTagList dataForAllSlots = new NBTTagList();
        for (Map.Entry<Fluid, Long> entry : millibucketsByFluid.entrySet()) {
            NBTTagCompound thisFluidStack = new NBTTagCompound();
            thisFluidStack.setString("fluid_name", entry.getKey().getName());
            thisFluidStack.setLong("fluid_amt", entry.getValue());
            dataForAllSlots.appendTag(thisFluidStack);
        }
//        // the array of hashmaps is then inserted into the parent hashmap for the container
        parentNBTTagCompound.setTag("Level", storageType);
        parentNBTTagCompound.setTag("Fluids", dataForAllSlots);

        if (coreLocation == null) {
            return parentNBTTagCompound;
        }

        NBTTagCompound blockPosNBT = new NBTTagCompound();        // NBTTagCompound is similar to a Java HashMap
        blockPosNBT.setInteger("x", coreLocation.getX());
        blockPosNBT.setInteger("y", coreLocation.getY());
        blockPosNBT.setInteger("z", coreLocation.getZ());
        parentNBTTagCompound.setTag("coreLocation", blockPosNBT);

        // to use an analogy with Java, this code generates an array of hashmaps
        // The itemStack in each slot is converted to an NBTTagCompound, which is effectively a hashmap of key->value pairs such
        //   as slot=1, id=2353, count=1, etc
        // Each of these NBTTagCompound are then inserted into NBTTagList, which is similar to an array.
        // return the NBT Tag Compound
        return parentNBTTagCompound;
    }

    // This is where you load the data that you saved in writeToNBT
    @Override
    public void readFromNBT(NBTTagCompound parentNBTTagCompound) {
        super.readFromNBT(parentNBTTagCompound); // The super call is required to save and load the tiles location

        String level = parentNBTTagCompound.getString("Level");
        for (HollowType ht : HollowType.values()) {
            if (ht.name.equals(level)) {
                this.storageLevel = ht;
                break;
            }
        }

        NBTTagCompound coreLoc = parentNBTTagCompound.getCompoundTag("coreLocation");
        if (coreLoc != null) {
            coreLocation = new BlockPos(coreLoc.getInteger("x"),
                    coreLoc.getInteger("y"), coreLoc.getInteger("z"));
        }

        final byte NBT_TYPE_COMPOUND = 10;       // See NBTBase.createNewByType() for a listing
        NBTTagList dataForAllSlots = parentNBTTagCompound.getTagList("Fluids", NBT_TYPE_COMPOUND);

        for (int i = 0; i < dataForAllSlots.tagCount(); ++i) {
            NBTTagCompound dataForOneSlot = dataForAllSlots.getCompoundTagAt(i);
            long amount = dataForOneSlot.getLong("fluid_amt");
            String fluidName = dataForOneSlot.getString("fluid_name");

            Fluid f = FluidRegistry.getFluid(fluidName);
            millibucketsByFluid.put(f, amount);
        }
    }

    @Override
    public InteractableType getType() {
        return InteractableType.FLUID;
    }

    // Type of hollow in terms of bucket capacity and name
    // Bucket capacity here represented in integers.
    public enum HollowType {
        INDIVIDUAL(10000, "Specialized"), // Pending balance.
        BASIC(8000, "Basic"),
        BIG(16000, "Big"),
        LARGE(32000, "Large"),
        MASSIVE(42000, "Massive"),
        GARGANTUAN(84000, "Gargantuan"),
        QUITE_BIG(168000, "Quite Big"),
        BIGGER_THAN_THAT(336000, "Even Bigger"),
        SINGULARITY(Integer.MAX_VALUE, "Singularity");

        private int size;
        private String name;

        HollowType(int size, String name) {
            this.size = size;
            this.name = name;
        }

    }
}
