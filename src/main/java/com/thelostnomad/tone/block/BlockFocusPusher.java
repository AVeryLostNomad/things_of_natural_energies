package com.thelostnomad.tone.block;

import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import com.thelostnomad.tone.block.tileentity.TEAcceptor;
import com.thelostnomad.tone.block.tileentity.TEFocusPusher;
import com.thelostnomad.tone.block.tileentity.TEPuller;
import com.thelostnomad.tone.block.tileentity.TESentientTreeCore;
import com.thelostnomad.tone.registry.ModGuiHandler;
import com.thelostnomad.tone.util.ChatUtil;
import com.thelostnomad.tone.util.TreeUtil;
import com.thelostnomad.tone.util.world.ITree;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
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

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BlockFocusPusher extends BlockContainer implements ITree {

    public static final PropertyDirection FACING = PropertyDirection.create("facing", Arrays.asList(EnumFacing.values()));

    public BlockFocusPusher() {
        super(Material.WOOD);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
        setUnlocalizedName(ThingsOfNaturalEnergies.MODID + ".focus_pusher");
        setRegistryName("focus_pusher");
        setCreativeTab(ThingsOfNaturalEnergies.creativeTab);
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity thisTE = worldIn.getTileEntity(pos);
        if (thisTE != null && thisTE instanceof TEFocusPusher) {
            TEFocusPusher thisHollow = (TEFocusPusher) thisTE;
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

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TEFocusPusher();
    }

    private void setDefaultFacing(World worldIn, BlockPos pos, IBlockState state) {
        if (!worldIn.isRemote) {
            IBlockState iblockstate = worldIn.getBlockState(pos.north());
            IBlockState iblockstate1 = worldIn.getBlockState(pos.south());
            IBlockState iblockstate2 = worldIn.getBlockState(pos.west());
            IBlockState iblockstate3 = worldIn.getBlockState(pos.east());
            IBlockState iblockstate4 = worldIn.getBlockState(pos.up());
            IBlockState iblockstate5 = worldIn.getBlockState(pos.down());
            EnumFacing enumfacing = (EnumFacing) state.getValue(FACING);

            if (enumfacing == EnumFacing.NORTH && iblockstate.isFullBlock() && !iblockstate1.isFullBlock()) {
                enumfacing = EnumFacing.SOUTH;
            } else if (enumfacing == EnumFacing.SOUTH && iblockstate1.isFullBlock() && !iblockstate.isFullBlock()) {
                enumfacing = EnumFacing.NORTH;
            } else if (enumfacing == EnumFacing.WEST && iblockstate2.isFullBlock() && !iblockstate3.isFullBlock()) {
                enumfacing = EnumFacing.EAST;
            } else if (enumfacing == EnumFacing.EAST && iblockstate3.isFullBlock() && !iblockstate2.isFullBlock()) {
                enumfacing = EnumFacing.WEST;
            } else if (enumfacing == EnumFacing.UP && iblockstate4.isFullBlock() && !iblockstate5.isFullBlock()) {
                enumfacing = EnumFacing.DOWN;
            } else if (enumfacing == EnumFacing.DOWN && iblockstate4.isFullBlock() && !iblockstate5.isFullBlock()) {
                enumfacing = EnumFacing.UP;
            }

            worldIn.setBlockState(pos, state.withProperty(FACING, enumfacing), 2);
        }
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState iBlockState) {
        return EnumBlockRenderType.MODEL;
    }

    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (worldIn.isRemote) {
            return true;
        } else {
            if (playerIn.isSneaking()) {
                // Rotate the block.
                EnumFacing[] possVals = EnumFacing.values();
                int index = state.getValue(FACING).getIndex();
                index++;
                if (index == possVals.length) {
                    index = 0;
                }
                EnumFacing newVal = possVals[index];
                worldIn.setBlockState(pos, state.withProperty(FACING, newVal), 3);
                TEFocusPusher tile = (TEFocusPusher) worldIn.getTileEntity(pos);
                if(tile.getCoreLocation() == null) {
                    BlockPos core = TreeUtil.findCore(worldIn, pos);
                    if (core != null) {
                        tile.setCoreLocation(core);
                    }
                }
                return true;
            }

            playerIn.openGui(ThingsOfNaturalEnergies.instance, ModGuiHandler.getGuiID(), worldIn, pos.getX(), pos.getY(), pos.getZ());
            return true;
        }
    }

    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    public IBlockState getStateFromMeta(int meta) {
        EnumFacing enumfacing = EnumFacing.getFront(meta);

        return this.getDefaultState().withProperty(FACING, enumfacing);
    }

    public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
        return state.withRotation(mirrorIn.toRotation((EnumFacing) state.getValue(FACING)));
    }

    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[]{FACING});
    }

    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(FACING, rot.rotate((EnumFacing) state.getValue(FACING)));
    }

    public int getMetaFromState(IBlockState state) {
        return ((EnumFacing) state.getValue(FACING)).getIndex();
    }

    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        BlockPos core = TreeUtil.findCore(worldIn, pos);
        if (core == null) {
            // There is no core here.
            if (!worldIn.isRemote) {
                if (placer instanceof EntityPlayer) {
                    List<ITextComponent> toSend = new ArrayList<ITextComponent>();
                    toSend.add(new TextComponentString("Without a core nearby, there can be no focus pusher."));
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
            TEFocusPusher thisAcceptor = (TEFocusPusher) worldIn.getTileEntity(pos);
            thisAcceptor.setCoreLocation(core);
        }
    }

}
