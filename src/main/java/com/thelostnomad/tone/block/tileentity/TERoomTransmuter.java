package com.thelostnomad.tone.block.tileentity;

import com.thelostnomad.tone.registry.ModBlocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;

public class TERoomTransmuter extends TileEntity implements ITickable {

    boolean hasFoundBlocks = false;
    ArrayList<BlockPos> needToHit = new ArrayList<BlockPos>();

    @Override
    public void update() {
        if(world.isRemote) return;
        if(!hasFoundBlocks){
            boolean result = findAllAirBlocksNotSky(new ArrayList<BlockPos>(), needToHit, world, pos.up());
            if(!result){
                // Access to sky. Refuse to do a thing.
                needToHit.clear();
                hasFoundBlocks = true;
            }
        }else{
            //Once a tick, pop off a block and set it to our gas.
            BlockPos first = needToHit.remove(0);
            world.setBlockState(first, ModBlocks.transmutationGas.getDefaultState());
        }
    }

    public boolean canSeeSky(BlockPos b){
        boolean cont = true;
        while(cont){
            b = b.up();
            if(!world.isAirBlock(b)){
                cont = false;
                return false;
            }
        }
        return true;
    }

    public boolean findAllAirBlocksNotSky(ArrayList<BlockPos> visited, ArrayList<BlockPos> list, World worldIn, BlockPos start){
        list.add(start);
        visited.add(start);
        BlockPos leftCorner = new BlockPos(start).south().west().down();
        BlockPos rightCorner = new BlockPos(start).north().east().up();
        Iterable<BlockPos> surrounding = BlockPos.getAllInBox(leftCorner, rightCorner);
        for(BlockPos b : surrounding){
            if(b.equals(start)){
                continue;
            }
            if(visited.contains(b)){
                continue;
            }
            if(canSeeSky(b)){
                // We can see the sky.
                // Unacceptable
                return false;
            }
            if(worldIn.isAirBlock(b)){
                findAllAirBlocksNotSky(visited, list, worldIn, b);
            }
        }
        return true;
    }

}
