package com.thelostnomad.tone.block;

import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import com.thelostnomad.tone.block.tileentity.TESentientTreeCore;
import com.thelostnomad.tone.util.ChatUtil;
import com.thelostnomad.tone.util.world.ITree;
import com.thelostnomad.tone.util.TreeUtil;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class RootBlock extends Block implements ITree {

    public RootBlock() {
        super(Material.WOOD);
        setUnlocalizedName(ThingsOfNaturalEnergies.MODID + ".roots");     // Used for localization (en_US.lang)
        setRegistryName("roots");        // The unique name (within your mod) that identifies this block
        setCreativeTab(ThingsOfNaturalEnergies.creativeTab);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {

        BlockPos core = TreeUtil.findCore(worldIn, pos);
        if(core == null){
            // There is no core here.
            if(!worldIn.isRemote){
                if(placer instanceof EntityPlayer){
                    List<ITextComponent> toSend = new ArrayList<ITextComponent>();
                    toSend.add(new TextComponentString("Without a core nearby, the root withers away."));
                    ChatUtil.sendNoSpam((EntityPlayer) placer, toSend.toArray(new ITextComponent[toSend.size()]));
                }
            }
            worldIn.setBlockToAir(pos);
            return;
        }
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);

        TileEntity tileentity = worldIn.getTileEntity(core);
        if (tileentity instanceof TESentientTreeCore) { // prevent a crash if not the right type, or is null
            TESentientTreeCore tileEntityData = (TESentientTreeCore) tileentity;
            tileEntityData.addRoot(pos);
        }
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        BlockPos core = TreeUtil.findCore(worldIn, pos);
        if(core == null){
            return;
        }

        TileEntity tileentity = worldIn.getTileEntity(core);
        if (tileentity instanceof TESentientTreeCore) { // prevent a crash if not the right type, or is null
            TESentientTreeCore tileEntityData = (TESentientTreeCore) tileentity;
            tileEntityData.removeRoot(pos);
        }
    }

}