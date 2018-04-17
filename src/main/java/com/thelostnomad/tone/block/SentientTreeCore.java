package com.thelostnomad.tone.block;

import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import com.thelostnomad.tone.block.tileentity.TESentientTreeCore;
import com.thelostnomad.tone.util.ChatUtil;
import com.thelostnomad.tone.util.world.ITree;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class SentientTreeCore extends Block implements ITree {

    public SentientTreeCore() {
        super(Material.WOOD);
        setUnlocalizedName(ThingsOfNaturalEnergies.MODID + ".sentient_core");     // Used for localization (en_US.lang)
        setRegistryName("sentient_core");
        setCreativeTab(ThingsOfNaturalEnergies.creativeTab);
    }

    private final int TIMER_COUNTDOWN_TICKS = 20 * 10; // duration of the countdown, in ticks = 10 seconds

    @Override
    public boolean hasTileEntity(IBlockState state)
    {
        return true;
    }

    // Called when the block is placed or loaded client side to get the tile entity for the block
    // Should return a new instance of the tile entity for the block
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {return new TESentientTreeCore();}

    // Called just after the player places a block.  Start the tileEntity's timer
    // This block in particular should not be "placed" in the conventional sense, but will rather grow naturall in
    // a tree!
    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);

        TileEntity tileentity = worldIn.getTileEntity(pos);
        if (tileentity instanceof TESentientTreeCore) { // prevent a crash if not the right type, or is null
            TESentientTreeCore tileEntityData = (TESentientTreeCore) tileentity;
        }
    }

    // the block will render in the SOLID layer.  See http://greyminecraftcoder.blogspot.co.at/2014/12/block-rendering-18.html for more information.
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer()
    {
        return BlockRenderLayer.SOLID;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand,
                                    EnumFacing side, float hitX, float hitY, float hitZ) {
        if(worldIn.getTileEntity(pos) != null){
            if(worldIn.getTileEntity(pos) instanceof TESentientTreeCore){
                TESentientTreeCore core = (TESentientTreeCore) worldIn.getTileEntity(pos);
                List<ITextComponent> toSend = new ArrayList<ITextComponent>();
                toSend.add(new TextComponentString("Life: " + String.valueOf(core.getLife()) + "  Max Life: " + String.valueOf(core.getMaxLife())));
                if(core.getSpawnTarget() != null){
                    toSend.add(new TextComponentString("In the process of spawning: " + core.getSpawnTarget().
                            getName() + ". [" + core.getContributedToSpawn() + " / " + core.getNeededToSpawn() + "]"));
                }
                ChatUtil.sendNoSpam(playerIn, toSend.toArray(new ITextComponent[toSend.size()]));

                if(playerIn.isSneaking()){
                    core.setSpawnTarget(null);
                }
            }
        }
        return true;
    }
}
