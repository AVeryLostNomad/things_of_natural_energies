package com.thelostnomad.tone.block.berries;

import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import com.thelostnomad.tone.block.SentientLeaves;
import com.thelostnomad.tone.block.tileentity.TESentientTreeCore;
import com.thelostnomad.tone.util.ChatUtil;
import com.thelostnomad.tone.util.world.IBerry;
import com.thelostnomad.tone.util.world.ITree;
import com.thelostnomad.tone.util.TreeUtil;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class BlockBerry extends Block implements ITree, IBerry {

    protected Item toSpawnOnBreak;
    protected String breakString;

    public BlockBerry() {
        super(Material.WOOD);
    }

    public boolean isOpaqueCube(IBlockState start){
        return false;
    }

    public boolean isFullBlock(IBlockState state)
    {
        return false;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        BlockPos core = TreeUtil.findCore(worldIn, pos);
        if(core == null){
            // There is no core here.
            if(!worldIn.isRemote){
                if(placer instanceof EntityPlayer){
                    List<ITextComponent> toSend = new ArrayList<ITextComponent>();
                    toSend.add(new TextComponentString("Without a core nearby, the berry shrivels up and dies."));
                    ChatUtil.sendNoSpam((EntityPlayer) placer, toSend.toArray(new ITextComponent[toSend.size()]));
                }
            }
            worldIn.setBlockToAir(pos);
            return;
        }

        if(!(worldIn.getBlockState(pos.up()).getBlock() instanceof SentientLeaves)){
            List<ITextComponent> toSend = new ArrayList<ITextComponent>();
            toSend.add(new TextComponentString("This needs to hang from a sentient leaf block."));
            ChatUtil.sendNoSpam((EntityPlayer) placer, toSend.toArray(new ITextComponent[toSend.size()]));
            worldIn.setBlockToAir(pos);
            return;
        }

        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);

        TileEntity tileentity = worldIn.getTileEntity(core);
        if (tileentity instanceof TESentientTreeCore) { // prevent a crash if not the right type, or is null
            TESentientTreeCore tileEntityData = (TESentientTreeCore) tileentity;
            tileEntityData.addBerry(pos);
        }
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        BlockPos core = TreeUtil.findCore(worldIn, pos);
        if(core == null){
            // There is no core here.
            ThingsOfNaturalEnergies.logger.error("Couldn't find core");
            worldIn.setBlockToAir(pos);
            return;
        }
        TileEntity tileentity = worldIn.getTileEntity(core);
        if (tileentity instanceof TESentientTreeCore) { // prevent a crash if not the right type, or is null
            TESentientTreeCore tileEntityData = (TESentientTreeCore) tileentity;
            tileEntityData.removeBerry(pos, breakString);
        }
        worldIn.setBlockToAir(pos);
    }

    public Item getItemDropped(IBlockState state, Random rand, int fortune)
    {
        return toSpawnOnBreak;
    }

    /**
     * Get the quantity dropped based on the given fortune level
     */
    public int quantityDroppedWithBonus(int fortune, Random random)
    {
        return this.quantityDropped(random) + random.nextInt(fortune + 1);
    }

    /**
     * Returns the quantity of items to drop on block destruction.
     */
    public int quantityDropped(Random random)
    {
        return 1 + random.nextInt(2);
    }

}
