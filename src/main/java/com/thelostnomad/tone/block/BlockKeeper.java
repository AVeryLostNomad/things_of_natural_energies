package com.thelostnomad.tone.block;

import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import com.thelostnomad.tone.block.tileentity.TEKeeper;
import com.thelostnomad.tone.block.tileentity.TEPuller;
import com.thelostnomad.tone.block.tileentity.TESentientTreeCore;
import com.thelostnomad.tone.network.TonePacketHandler;
import com.thelostnomad.tone.registry.ModGuiHandler;
import com.thelostnomad.tone.util.ChatUtil;
import com.thelostnomad.tone.util.TreeUtil;
import com.thelostnomad.tone.util.world.ITree;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

// Useful interface for other mods.
// Simply keeps a configurable amount of one stack of items in its inventory at all times - if possible.
// Would be the ideal interface from which to access a tree's inventory with pipes or conduits, for example.
public class BlockKeeper extends BlockContainer implements ITree {

    public BlockKeeper() {
        super(Material.WOOD);
        setUnlocalizedName(ThingsOfNaturalEnergies.MODID + ".keeper");
        setRegistryName("keeper");
        setCreativeTab(ThingsOfNaturalEnergies.creativeTab);
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState iBlockState) {
        return EnumBlockRenderType.MODEL;
    }

    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (worldIn.isRemote) {
            return true;
        } else {
            playerIn.openGui(ThingsOfNaturalEnergies.instance, ModGuiHandler.getGuiID(), worldIn, pos.getX(), pos.getY(), pos.getZ());
            return true;
        }
    }

    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TEKeeper();
    }

    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {

        BlockPos core = TreeUtil.findCore(worldIn, pos);
        if (core == null) {
            // There is no core here.
            if (!worldIn.isRemote) {
                if (placer instanceof EntityPlayer) {
                    List<ITextComponent> toSend = new ArrayList<ITextComponent>();
                    toSend.add(new TextComponentString("Without a core nearby, there can be no keeper."));
                    ChatUtil.sendNoSpam((EntityPlayer) placer, toSend.toArray(new ITextComponent[toSend.size()]));
                }
            } else {
                worldIn.spawnParticle(EnumParticleTypes.SMOKE_LARGE, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 0, 0, 0, new int[0]);
            }
            worldIn.removeTileEntity(pos);
            worldIn.setBlockToAir(pos);
            return;
        }
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);

        TileEntity tileentity = worldIn.getTileEntity(core);
        if (tileentity instanceof TESentientTreeCore) { // prevent a crash if not the right type, or is null
            TESentientTreeCore tileEntityData = (TESentientTreeCore) tileentity;
            tileEntityData.addInteractable(pos);
            TEKeeper thisPusher = (TEKeeper) worldIn.getTileEntity(pos);
            thisPusher.setCoreLocation(core);
        }
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity thisTE = worldIn.getTileEntity(pos);
        if (thisTE != null && thisTE instanceof TEPuller) {
            TEKeeper thisHollow = (TEKeeper) thisTE;
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

}
