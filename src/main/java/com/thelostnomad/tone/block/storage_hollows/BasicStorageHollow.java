package com.thelostnomad.tone.block.storage_hollows;

import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import com.thelostnomad.tone.block.tileentity.TESentientTreeCore;
import com.thelostnomad.tone.block.tileentity.TEStorageHollow;
import com.thelostnomad.tone.util.ChatUtil;
import com.thelostnomad.tone.util.world.ITree;
import com.thelostnomad.tone.util.TreeUtil;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BasicStorageHollow extends BlockContainer implements ITree {

    public BasicStorageHollow() {
        super(Material.WOOD);
        initialize();
    }

    public void initialize(){
        setUnlocalizedName(ThingsOfNaturalEnergies.MODID + ".storagehollow_basic");     // Used for localization (en_US.lang)
        setRegistryName("storagehollow_basic");        // The unique name (within your mod) that identifies this block
        setCreativeTab(ThingsOfNaturalEnergies.creativeTab);
    }

    /**
     * Create the Tile Entity for this block.
     * If your block doesn't extend BlockContainer, use createTileEntity(World worldIn, IBlockState state) instead
     *
     * @param worldIn
     * @param meta
     * @return
     */

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        TEStorageHollow e = new TEStorageHollow();
        e.setStorageLevel(TEStorageHollow.HollowType.BASIC);
        return e;
    }

    // not needed if your block implements ITileEntityProvider (in this case implemented by BlockContainer), but it
    //  doesn't hurt to include it anyway...
    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    // Called when the block is right clicked
    // In this block it is used to open the block gui when right clicked by a player
    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand,
                                    EnumFacing side, float hitX, float hitY, float hitZ) {
        // Uses the gui handler registered to your mod to open the gui for the given gui id
        // open on the server side only  (not sure why you shouldn't open client side too... vanilla doesn't, so we better not either)
        if (worldIn.isRemote)
            return true;
        TileEntity te = worldIn.getTileEntity(pos);
        if (te == null) return true;
        if (te instanceof TEStorageHollow) {
            TEStorageHollow storage = (TEStorageHollow) te;

            int filled = storage.getFilled();
            int cap = storage.getCapacity();
            double percentage = filled / (double) cap;
            percentage *= 100D;

            List<ITextComponent> toSend = new ArrayList<ITextComponent>();
            toSend.add(new TextComponentString("This storage hollow is " + String.valueOf(percentage) + " filled " +
                    "(" + String.valueOf(filled) + "/" + String.valueOf(cap) + ")"));
            ChatUtil.sendNoSpam(playerIn, toSend.toArray(new ITextComponent[toSend.size()]));
        }
//        playerIn.openGui(MinecraftByExample.instance, GuiHandlerMBE30.getGuiID(), worldIn, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }

    // This is where you can do something when the block is broken. In this case drop the inventory's contents
    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        IInventory inventory = worldIn.getTileEntity(pos) instanceof IInventory ? (IInventory) worldIn.getTileEntity(pos) : null;

        if (inventory != null) {
            // For each slot in the inventory
            for (int i = 0; i < inventory.getSizeInventory(); i++) {
                // If the slot is not empty
                if (!inventory.getStackInSlot(i).isEmpty())  // terminal
                {
                    // Create a new entity item with the item stack in the slot
                    EntityItem item = new EntityItem(worldIn, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, inventory.getStackInSlot(i));

                    // Apply some random motion to the item
                    float multiplier = 0.1f;
                    float motionX = worldIn.rand.nextFloat() - 0.5f;
                    float motionY = worldIn.rand.nextFloat() - 0.5f;
                    float motionZ = worldIn.rand.nextFloat() - 0.5f;

                    item.motionX = motionX * multiplier;
                    item.motionY = motionY * multiplier;
                    item.motionZ = motionZ * multiplier;

                    // Spawn the item in the world
                    worldIn.spawnEntity(item);
                }
            }

            // Clear the inventory so nothing else (such as another mod) can do anything with the items
            inventory.clear();
        }

        TileEntity thisTE = worldIn.getTileEntity(pos);
        if (thisTE != null && thisTE instanceof TEStorageHollow) {
            TEStorageHollow thisHollow = (TEStorageHollow) thisTE;
            if (thisHollow.getCoreLocation() != null) {
                TileEntity te = worldIn.getTileEntity(thisHollow.getCoreLocation());
                if (te != null && te instanceof TESentientTreeCore) {
                    TESentientTreeCore core = (TESentientTreeCore) te;
                    core.removeInteractable(pos);
                }
            }
        }

        // Super MUST be called last because it removes the tile entity
        super.breakBlock(worldIn, pos, state);
    }

    //---------------------------------------------------------

    // the block is smaller than a full cube, specify dimensions here
    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return new AxisAlignedBB(1 / 16.0F, 0, 1 / 16.0F, 15 / 16.0F, 8 / 16.0F, 15 / 16.0F);
    }

    // the block will render in the SOLID layer.  See http://greyminecraftcoder.blogspot.co.at/2014/12/block-rendering-18.html for more information.
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.SOLID;
    }

    // used by the renderer to control lighting and visibility of other block.
    // set to false because this block doesn't fill the entire 1x1x1 space
    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    // used by the renderer to control lighting and visibility of other block, also by
    // (eg) wall or fence to control whether the fence joins itself to this block
    // set to false because this block doesn't fill the entire 1x1x1 space
    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    // render using a BakedModel (mbe30_inventory_basic.json --> mbe30_inventory_basic_model.json)
    // required because the default (super method) is INVISIBLE for BlockContainers.
    @Override
    public EnumBlockRenderType getRenderType(IBlockState iBlockState) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);

        BlockPos core = TreeUtil.findCore(worldIn, pos);
        if (core == null) {
            // There is no core here.
            if (!worldIn.isRemote) {
                if (placer instanceof EntityPlayer) {
                    List<ITextComponent> toSend = new ArrayList<ITextComponent>();
                    toSend.add(new TextComponentString("Without a core nearby, there can be no storage hollow."));
                    ChatUtil.sendNoSpam((EntityPlayer) placer, toSend.toArray(new ITextComponent[toSend.size()]));
                }
            } else {
                worldIn.spawnParticle(EnumParticleTypes.SMOKE_LARGE, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 0, 0, 0, new int[0]);
            }
            worldIn.removeTileEntity(pos);
            worldIn.setBlockToAir(pos);
            return;
        }

        TileEntity tileentity = worldIn.getTileEntity(core);
        if (tileentity instanceof TESentientTreeCore) { // prevent a crash if not the right type, or is null
            TESentientTreeCore tileEntityData = (TESentientTreeCore) tileentity;
            tileEntityData.addInteractable(pos);
            TEStorageHollow thisStorageHollow = (TEStorageHollow) worldIn.getTileEntity(pos);
            thisStorageHollow.setCoreLocation(core);
        }

    }

    public String debugTab(IBlockAccess world, BlockPos pos) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TEStorageHollow) {
            String toReturn = "";
            TEStorageHollow teStorageHollow = (TEStorageHollow) tileEntity;
            int filled = teStorageHollow.getFilled();
            toReturn += "Filled: " + String.valueOf(filled);
            int capacity = teStorageHollow.getCapacity();
            toReturn += " Capacity: " + String.valueOf(capacity);
            BigDecimal a = new BigDecimal((double) filled / (double) capacity);
            toReturn += " Percent: " + String.valueOf(a);
            BigDecimal b = a.setScale(1, RoundingMode.HALF_EVEN);
            toReturn += " Rounded: " + String.valueOf(b);
            b = b.multiply(new BigDecimal(10));
            toReturn += " Multiplied: " + String.valueOf(b);
            int filledAmt = MathHelper.clamp(b.intValue(), 0, 10);
            toReturn += " Final: " + String.valueOf(filledAmt);
            return toReturn;
        }
        return "";
    }

    public int tabulateDesiredFill(IBlockAccess world, BlockPos pos) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TEStorageHollow) {
            TEStorageHollow teStorageHollow = (TEStorageHollow) tileEntity;
            int filled = teStorageHollow.getFilled();
            int capacity = teStorageHollow.getCapacity();
            BigDecimal a = new BigDecimal((double) filled / (double) capacity);
            BigDecimal b = a.setScale(1, RoundingMode.HALF_EVEN);
            b = b.multiply(new BigDecimal(10));
            int filledAmt = MathHelper.clamp(b.intValue(), 0, 10);
            return filledAmt;
        }
        return -1;
    }

}
