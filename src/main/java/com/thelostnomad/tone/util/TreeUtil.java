package com.thelostnomad.tone.util;

import com.thelostnomad.tone.block.RootBlock;
import com.thelostnomad.tone.block.SentientLeaves;
import com.thelostnomad.tone.block.SentientTreeCore;
import com.thelostnomad.tone.util.world.ITree;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Vector;

public class TreeUtil {

    public static BlockPos findCore(World worldIn, BlockPos start){
        return findCore(new Vector<BlockPos>(), worldIn, start);
    }

    public static BlockPos findCore(Vector<BlockPos> previous, World worldIn, BlockPos start){
        if(worldIn.getBlockState(start).getBlock() instanceof SentientTreeCore){
            return start;
        }
        previous.add(start);
        BlockPos leftCorner = new BlockPos(start).south().west().down();
        BlockPos rightCorner = new BlockPos(start).north().east().up();
        Iterable<BlockPos> surrounding = BlockPos.getAllInBox(leftCorner, rightCorner);
        for(BlockPos b : surrounding){
            if(b.equals(start)){
                continue;
            }
            if(previous.contains(b)){
                continue;
            }
            if(ITree.class.isAssignableFrom(worldIn.getBlockState(b).getBlock().getClass())){
                BlockPos result = findCore(previous, worldIn, b);
                if(result == null){
                    continue;
                }else{
                    return result;
                }
            }
        }
        return null;
    }

    public static ArrayList<BlockPos> findAllTreeBlocks(World worldIn, BlockPos start){
        ArrayList<BlockPos> toReturn = new ArrayList<>();
        findAllTreeBlocks(new ArrayList<BlockPos>(), toReturn, worldIn, start);
        return toReturn;
    }

    public static void findAllTreeBlocks(ArrayList<BlockPos> visited, ArrayList<BlockPos> list, World worldIn, BlockPos start){
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
            if(ITree.class.isAssignableFrom(worldIn.getBlockState(b).getBlock().getClass())){
                findAllTreeBlocks(visited, list, worldIn, b);
            }
        }
    }

    public static ArrayList<BlockPos> findAllConnectedLeaves(World worldIn, BlockPos start){
        ArrayList<BlockPos> toReturn = new ArrayList<>();
        findAllConnectedLeaves(new ArrayList<BlockPos>(), toReturn, worldIn, start);
        return toReturn;
    }

    public static void findAllConnectedLeaves(ArrayList<BlockPos> visited, ArrayList<BlockPos> list, World worldIn, BlockPos start){
        if(worldIn.getBlockState(start).getBlock() instanceof SentientLeaves){
            list.add(start);
        }
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
            if(ITree.class.isAssignableFrom(worldIn.getBlockState(b).getBlock().getClass())){
                findAllConnectedLeaves(visited, list, worldIn, b);
            }
        }
    }

    public static ArrayList<BlockPos> findAllConnectedRoots(World worldIn, BlockPos start){
        ArrayList<BlockPos> toReturn = new ArrayList<>();
        findAllConnectedRoots(new ArrayList<BlockPos>(), toReturn, worldIn, start);
        return toReturn;
    }

    public static void findAllConnectedRoots(ArrayList<BlockPos> visited, ArrayList<BlockPos> list, World worldIn, BlockPos start){
        if(worldIn.getBlockState(start).getBlock() instanceof RootBlock){
            list.add(start);
        }
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
            if(ITree.class.isAssignableFrom(worldIn.getBlockState(b).getBlock().getClass())){
                findAllConnectedRoots(visited, list, worldIn, b);
            }
        }
    }

}
