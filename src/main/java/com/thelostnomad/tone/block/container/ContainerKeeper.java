package com.thelostnomad.tone.block.container;

import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import com.thelostnomad.tone.block.tileentity.TEKeeper;
import com.thelostnomad.tone.util.gui.SyncableContainer;
import com.thelostnomad.tone.util.gui.SyncableTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerKeeper extends Container implements SyncableContainer{

    // Stores a reference to the tile entity instance for later use
    private TEKeeper teKeeper;

    // must assign a slot number to each of the slots used by the GUI.
    // For this container, we can see both the tile inventory's slots as well as the player inventory slots and the hotbar.
    // Each time we add a Slot to the container, it automatically increases the slotIndex, which means
    //  0 - 8 = hotbar slots (which will map to the InventoryPlayer slot numbers 0 - 8)
    //  9 - 35 = player inventory slots (which map to the InventoryPlayer slot numbers 9 - 35)

    private final int HOTBAR_SLOT_COUNT = 9;
    private final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;

    private final int VANILLA_FIRST_SLOT_INDEX = 0;
    private final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;
    private final int TE_INVENTORY_SLOT_COUNT = 2;

    public ContainerKeeper(InventoryPlayer invPlayer, TEKeeper tilePuller) {
        this.teKeeper = tilePuller;

        final int SLOT_X_SPACING = 18;
        final int SLOT_Y_SPACING = 18;
        final int HOTBAR_XPOS = 8;
        final int HOTBAR_YPOS = 109;
        // Add the players hotbar to the gui - the [xpos, ypos] location of each item
        for (int x = 0; x < HOTBAR_SLOT_COUNT; x++) {
            int slotNumber = x;
            addSlotToContainer(new Slot(invPlayer, slotNumber, HOTBAR_XPOS + SLOT_X_SPACING * x - 1, HOTBAR_YPOS - 1));
        }

        final int PLAYER_INVENTORY_XPOS = 8;
        final int PLAYER_INVENTORY_YPOS = 51;
        // Add the rest of the players inventory to the gui
        for (int y = 0; y < PLAYER_INVENTORY_ROW_COUNT; y++) {
            for (int x = 0; x < PLAYER_INVENTORY_COLUMN_COUNT; x++) {
                int slotNumber = HOTBAR_SLOT_COUNT + y * PLAYER_INVENTORY_COLUMN_COUNT + x;
                int xpos = PLAYER_INVENTORY_XPOS + x * SLOT_X_SPACING;
                int ypos = PLAYER_INVENTORY_YPOS + y * SLOT_Y_SPACING;
                addSlotToContainer(new Slot(invPlayer, slotNumber,  xpos - 1, ypos - 1));
            }
        }

        if (TE_INVENTORY_SLOT_COUNT != teKeeper.getSizeInventory()) {
            System.err.println("Mismatched slot count in ContainerBasic(" + TE_INVENTORY_SLOT_COUNT
                    + ") and TileInventory (" + teKeeper.getSizeInventory()+")");
        }

        addSlotToContainer(new Slot(teKeeper, 36, 7, 19));
        addSlotToContainer(new Slot(teKeeper, 37, 151, 19));
    }

    public SyncableTileEntity getSyncableTileEntity() {
        return teKeeper;
    }

    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player)
    {
        if((!player.inventory.getItemStack().isEmpty()) && slotId == 37){
            return ItemStack.EMPTY; //You cannot drop or swap items into slot 37.
        }else{
            return super.slotClick(slotId, dragType, clickTypeIn, player);
        }
    }

    // Vanilla calls this method every tick to make sure the player is still able to access the inventory, and if not closes the gui
    @Override
    public boolean canInteractWith(EntityPlayer player)
    {
        return teKeeper.isUsableByPlayer(player);
    }

    // This is where you specify what happens when a player shift clicks a slot in the gui
    //  (when you shift click a slot in the TileEntity Inventory, it moves it to the first available position in the hotbar and/or
    //    player inventory.  When you you shift-click a hotbar or player inventory item, it moves it to the first available
    //    position in the TileEntity inventory)
    // At the very least you must override this and return EMPTY_ITEM or the game will crash when the player shift clicks a slot
    // returns EMPTY_ITEM if the source slot is empty, or if none of the the source slot items could be moved
    //   otherwise, returns a copy of the source stack
    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int sourceSlotIndex)
    {
        Slot sourceSlot = (Slot)inventorySlots.get(sourceSlotIndex);
        if (sourceSlot == null || !sourceSlot.getHasStack()) return ItemStack.EMPTY;  //EMPTY_ITEM
        ItemStack sourceStack = sourceSlot.getStack();
        ItemStack copyOfSourceStack = sourceStack.copy();

        // Check if the slot clicked is one of the vanilla container slots
        if (sourceSlotIndex >= VANILLA_FIRST_SLOT_INDEX && sourceSlotIndex < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
            // This is a vanilla container slot so merge the stack into the tile inventory
            // We can only put it into the first slot though, keep that in mind.
            if (!mergeItemStack(sourceStack, TE_INVENTORY_FIRST_SLOT_INDEX, TE_INVENTORY_FIRST_SLOT_INDEX + 1, false)){
                return ItemStack.EMPTY;  // EMPTY_ITEM
            }
        } else if (sourceSlotIndex >= TE_INVENTORY_FIRST_SLOT_INDEX && sourceSlotIndex < TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT) {
            // This is a TE slot so merge the stack into the players inventory
            if (!mergeItemStack(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;   // EMPTY_ITEM
            }
        } else {
            return ItemStack.EMPTY;   // EMPTY_ITEM
        }

        // If stack size == 0 (the entire stack was moved) set slot contents to null
        if (sourceStack.getCount() == 0) {  // getStackSize
            sourceSlot.putStack(ItemStack.EMPTY);  // EMPTY_ITEM
        } else {
            sourceSlot.onSlotChanged();
        }

        sourceSlot.onTake(player, sourceStack);  //onPickupFromSlot()
        return copyOfSourceStack;
    }

    // pass the close container message to the tileEntityInventory (not strictly needed for this example)
    //  see ContainerChest and TileEntityChest
    @Override
    public void onContainerClosed(EntityPlayer playerIn)
    {
        super.onContainerClosed(playerIn);
        this.teKeeper.closeInventory(playerIn);
    }

}
