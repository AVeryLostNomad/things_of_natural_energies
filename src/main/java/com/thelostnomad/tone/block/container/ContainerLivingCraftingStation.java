package com.thelostnomad.tone.block.container;

import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import com.thelostnomad.tone.block.gui.InventoryExtraSlot;
import com.thelostnomad.tone.block.tileentity.TELivingCraftingStation;
import com.thelostnomad.tone.block.tileentity.TESentientTreeCore;
import com.thelostnomad.tone.network.LastRecipeMessage;
import com.thelostnomad.tone.network.TonePacketHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ContainerLivingCraftingStation extends Container {

    public TELivingCraftingStation lcs;

    // Sortof a hybrid blend between tile entity and vanilla, we'll do a container side inventory too
    public IRecipe lastRecipe;
    IRecipe lastLastRecipe;
    public InventoryCrafting craftMatrix = new InventoryCrafting(this, 3, 3);
    public InventoryCraftResult craftResult = new InventoryCraftResult();

    public InventoryExtraSlot storeSlot;
    public InventoryExtraSlot deleteSlot;

    public boolean init = false;
    public BlockPos pos;
    public EntityPlayer player;

    public ContainerLivingCraftingStation(InventoryPlayer inv, TELivingCraftingStation tile){
        lcs = tile;
        this.pos = tile.getPos();
        this.player = inv.player;
        storeSlot = new InventoryExtraSlot(10, tile);
        deleteSlot = new InventoryExtraSlot(11, tile);

        for(int i = 0; i < 9; i++){
            craftMatrix.setInventorySlotContents(i, lcs.getStackInSlot(i));
        }
        storeSlot.setInventorySlotContents(10, tile.getStackInSlot(10)); // TODO make it so these are cleared on close. 10 sent to inv (if possible)
                                                                                        // 11 deleted
        init = true;

        // ADD UI COMPONENTS
        this.addSlotToContainer(new SlotCrafting(inv. player, craftMatrix, craftResult, 9, 124 - 1, 35 - 1));

        // Adds the crafting grid to the container
        for (int i = 0; i < 3; ++i)
        {
            for (int j = 0; j < 3; ++j)
            {
                this.addSlotToContainer(new Slot(this.craftMatrix, j + i * 3, (30 + j * 18) - 1, (17 + i * 18) - 1));
            }
        }

        this.addSlotToContainer(new Slot(storeSlot, 10, 187, 27));
        this.addSlotToContainer(new Slot(deleteSlot, 11, 226, 27));

        // Add the player's inventory.
        for (int k = 0; k < 3; ++k)
        {
            for (int i1 = 0; i1 < 9; ++i1)
            {
                this.addSlotToContainer(new Slot(inv, i1 + k * 9 + 9, (8 + i1 * 18) - 1, (84 + k * 18) - 1));
            }
        }

        // Add's the hotbar.
        for (int l = 0; l < 9; ++l)
        {
            this.addSlotToContainer(new Slot(inv, l, (8 + l * 18) - 1, 142 - 1));
        }
    }

    public void clearGrid(EntityPlayer playerIn) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = this.craftMatrix.getStackInSlot(i);
            if (stack != null) {
                TESentientTreeCore core = (TESentientTreeCore) lcs.getWorld().getTileEntity(lcs.getCoreLocation());
                ItemStack result = core.canFitItem(stack);
                this.craftMatrix.setInventorySlotContents(i, ItemStack.EMPTY);
                if (result != ItemStack.EMPTY) {
                    playerIn.dropItem(stack, false);
                }else{
                    core.doFitItem(stack);
                }
            }
        }
    }

    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player)
    {
        if(slotId == 10 || slotId == 11){
            // Let us handle that.
            Slot slot = this.inventorySlots.get(slotId);
            if(slot.getStack() != null && !slot.getStack().isEmpty() && (player.inventory.getItemStack() != null && !player.inventory.getItemStack().isEmpty())){
                // Oh dear, there's something there!
                ItemStack there = slot.getStack();
                boolean canFit = this.lcs.overStack(slotId, there);
                if(!canFit){
                    return ItemStack.EMPTY;
                }
                slot.putStack(ItemStack.EMPTY);
            }
            return super.slotClick(slotId, dragType, clickTypeIn, player);
        }else{
            return super.slotClick(slotId, dragType, clickTypeIn, player);
        }
    }

    public void onCraftMatrixChanged(IInventory inv){
        this.slotChangedCraftingGrid(lcs.getWorld(), player, this.craftMatrix, this.craftResult);
    }

    protected void slotChangedCraftingGrid(World world, EntityPlayer player, InventoryCrafting inv, InventoryCraftResult result){
        ItemStack itemStack = ItemStack.EMPTY;

        if(lastRecipe == null || !lastRecipe.matches(inv, world)) lastRecipe = CraftingManager.findMatchingRecipe(inv, world);

        if(lastRecipe != null){
            itemStack = lastRecipe.getCraftingResult(inv);
        }

        if(!world.isRemote){
            result.setInventorySlotContents(0, itemStack);
            EntityPlayerMP entityplayermp = (EntityPlayerMP) player;
            if (lastLastRecipe != lastRecipe) entityplayermp.connection.sendPacket(new SPacketSetSlot(this.windowId, 0, itemStack));
            else if (lastLastRecipe != null && lastLastRecipe == lastRecipe && !ItemStack.areItemStacksEqual(lastLastRecipe.getCraftingResult(inv), lastRecipe.getCraftingResult(inv))) entityplayermp.connection.sendPacket(new SPacketSetSlot(this.windowId, 0, itemStack));
            TonePacketHandler.sendTo(new LastRecipeMessage(lastRecipe), entityplayermp);
        }

        lastLastRecipe = lastRecipe;
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return playerIn.getDistanceSq(pos) <= 64.0D;
    }

    @Override
    public void onContainerClosed(EntityPlayer playerIn)
    {
        updateLcs();
    }

    public void updateLcs(){
        for(int i = 0; i < 9; i++){
            lcs.setInventorySlotContents(i, craftMatrix.getStackInSlot(i));
        }
        lcs.setInventorySlotContents(10, storeSlot.getStackInSlot(0));
        // 11 deleted
    }
}
