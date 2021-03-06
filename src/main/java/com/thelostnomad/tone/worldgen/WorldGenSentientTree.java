package com.thelostnomad.tone.worldgen;

import com.google.common.collect.Lists;
import com.thelostnomad.tone.block.berries.BlockBerry;
import com.thelostnomad.tone.block.tileentity.TESentientTreeCore;
import com.thelostnomad.tone.registry.ModBlocks;
import com.thelostnomad.tone.util.TreeUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLog;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;

import java.util.*;

public class WorldGenSentientTree extends WorldGenAbstractTree
{
    private Random rand;
    private World world;
    private BlockPos basePos = BlockPos.ORIGIN;
    int heightLimit;
    int height;
    double heightAttenuation = 0.618D;
    double branchSlope = 0.381D;
    double scaleWidth = 1.0D;
    double leafDensity = 1.0D;
    int trunkSize = 1;
    int heightLimitLimit = 12;
    /** Sets the distance limit for how far away the generator will populate leaves from the base leaf node. */
    int leafDistanceLimit = 4;
    List<FoliageCoordinates> foliageCoords;

    public WorldGenSentientTree(boolean notify)
    {
        super(notify);
    }

    /**
     * Generates a list of leaf nodes for the tree, to be populated by generateLeaves.
     */
    void generateLeafNodeList()
    {
        this.height = (int)((double)this.heightLimit * this.heightAttenuation);

        if (this.height >= this.heightLimit)
        {
            this.height = this.heightLimit - 1;
        }

        int i = (int)(1.382D + Math.pow(this.leafDensity * (double)this.heightLimit / 13.0D, 2.0D));

        if (i < 1)
        {
            i = 1;
        }

        int j = this.basePos.getY() + this.height;
        int k = this.heightLimit - this.leafDistanceLimit;
        this.foliageCoords = Lists.<FoliageCoordinates>newArrayList();
        this.foliageCoords.add(new FoliageCoordinates(this.basePos.up(k), j));

        for (; k >= 0; --k)
        {
            float f = this.layerSize(k);

            if (f >= 0.0F)
            {
                for (int l = 0; l < i; ++l)
                {
                    double d0 = this.scaleWidth * (double)f * ((double)this.rand.nextFloat() + 0.328D);
                    double d1 = (double)(this.rand.nextFloat() * 2.0F) * Math.PI;
                    double d2 = d0 * Math.sin(d1) + 0.5D;
                    double d3 = d0 * Math.cos(d1) + 0.5D;
                    BlockPos blockpos = this.basePos.add(d2, (double)(k - 1), d3);
                    BlockPos blockpos1 = blockpos.up(this.leafDistanceLimit);

                    if (this.checkBlockLine(blockpos, blockpos1) == -1)
                    {
                        int i1 = this.basePos.getX() - blockpos.getX();
                        int j1 = this.basePos.getZ() - blockpos.getZ();
                        double d4 = (double)blockpos.getY() - Math.sqrt((double)(i1 * i1 + j1 * j1)) * this.branchSlope;
                        int k1 = d4 > (double)j ? j : (int)d4;
                        BlockPos blockpos2 = new BlockPos(this.basePos.getX(), k1, this.basePos.getZ());

                        if (this.checkBlockLine(blockpos2, blockpos) == -1)
                        {
                            this.foliageCoords.add(new FoliageCoordinates(blockpos, blockpos2.getY()));
                        }
                    }
                }
            }
        }
    }

    void crosSection(BlockPos pos, float p_181631_2_, IBlockState p_181631_3_)
    {
        int i = (int)((double)p_181631_2_ + 0.618D);

        for (int j = -i; j <= i; ++j)
        {
            for (int k = -i; k <= i; ++k)
            {
                if (Math.pow((double)Math.abs(j) + 0.5D, 2.0D) + Math.pow((double)Math.abs(k) + 0.5D, 2.0D) <= (double)(p_181631_2_ * p_181631_2_))
                {
                    BlockPos blockpos = pos.add(j, 0, k);
                    IBlockState state = this.world.getBlockState(blockpos);

                    if (state.getBlock().isAir(state, world, blockpos) || state.getBlock().isLeaves(state, world, blockpos))
                    {
                        this.setBlockAndNotifyAdequately(this.world, blockpos, p_181631_3_);
                    }
                }
            }
        }
    }

    /**
     * Gets the rough size of a layer of the tree.
     */
    float layerSize(int y)
    {
        if ((float)y < (float)this.heightLimit * 0.3F)
        {
            return -1.0F;
        }
        else
        {
            float f = (float)this.heightLimit / 2.0F;
            float f1 = f - (float)y;
            float f2 = MathHelper.sqrt(f * f - f1 * f1);

            if (f1 == 0.0F)
            {
                f2 = f;
            }
            else if (Math.abs(f1) >= f)
            {
                return 0.0F;
            }

            return f2 * 0.5F;
        }
    }

    float leafSize(int y)
    {
        if (y >= 0 && y < this.leafDistanceLimit)
        {
            return y != 0 && y != this.leafDistanceLimit - 1 ? 3.0F : 2.0F;
        }
        else
        {
            return -1.0F;
        }
    }

    /**
     * Generates the leaves surrounding an individual entry in the leafNodes list.
     */
    void generateLeafNode(BlockPos pos)
    {
        for (int i = 0; i < this.leafDistanceLimit; ++i)
        {
            this.crosSection(pos.up(i), this.leafSize(i), ModBlocks.sentientLeaves.getDefaultState());
        }
    }

    void limb(BlockPos p_175937_1_, BlockPos p_175937_2_, Block p_175937_3_)
    {
        BlockPos blockpos = p_175937_2_.add(-p_175937_1_.getX(), -p_175937_1_.getY(), -p_175937_1_.getZ());
        int i = this.getGreatestDistance(blockpos);
        float f = (float)blockpos.getX() / (float)i;
        float f1 = (float)blockpos.getY() / (float)i;
        float f2 = (float)blockpos.getZ() / (float)i;

        for (int j = 0; j <= i; ++j)
        {
            BlockPos blockpos1 = p_175937_1_.add((double)(0.5F + (float)j * f), (double)(0.5F + (float)j * f1), (double)(0.5F + (float)j * f2));
            BlockLog.EnumAxis blocklog$enumaxis = this.getLogAxis(p_175937_1_, blockpos1);
            this.setBlockAndNotifyAdequately(this.world, blockpos1, p_175937_3_.getDefaultState());
        }
    }

    /**
     * Returns the absolute greatest distance in the BlockPos object.
     */
    private int getGreatestDistance(BlockPos posIn)
    {
        int i = MathHelper.abs(posIn.getX());
        int j = MathHelper.abs(posIn.getY());
        int k = MathHelper.abs(posIn.getZ());

        if (k > i && k > j)
        {
            return k;
        }
        else
        {
            return j > i ? j : i;
        }
    }

    private BlockLog.EnumAxis getLogAxis(BlockPos p_175938_1_, BlockPos p_175938_2_)
    {
        BlockLog.EnumAxis blocklog$enumaxis = BlockLog.EnumAxis.Y;
        int i = Math.abs(p_175938_2_.getX() - p_175938_1_.getX());
        int j = Math.abs(p_175938_2_.getZ() - p_175938_1_.getZ());
        int k = Math.max(i, j);

        if (k > 0)
        {
            if (i == k)
            {
                blocklog$enumaxis = BlockLog.EnumAxis.X;
            }
            else if (j == k)
            {
                blocklog$enumaxis = BlockLog.EnumAxis.Z;
            }
        }

        return blocklog$enumaxis;
    }

    /**
     * Generates the leaf portion of the tree as specified by the leafNodes list.
     */
    void generateLeaves()
    {
        for (FoliageCoordinates worldgenbigtree$foliagecoordinates : this.foliageCoords)
        {
            this.generateLeafNode(worldgenbigtree$foliagecoordinates);
        }
    }

    List<BlockPos> generateBerries() {
        ArrayList<BlockPos> newBerries = new ArrayList<BlockPos>();
        ArrayList<BlockPos> bp = TreeUtil.findAllConnectedLeaves(world, basePos);
        Map<XZPair, BlockPos> heightMap = new HashMap<XZPair, BlockPos>(); //The lowest at each xz pair.
        for(BlockPos pos : bp){
            XZPair xz = new XZPair(pos);
            if(heightMap.containsKey(xz)){
                // See if this one is lower.
                BlockPos previous = heightMap.get(xz);
                heightMap.put(xz, previous.getY() > pos.getY() ? pos : previous);
            }else{
                // This one is lowest so far
                heightMap.put(xz, pos);
            }
        }
        Random rand = new Random();
        for(BlockPos pos : heightMap.values()){
            // These are the lowest leaves in the tree.
            if(rand.nextInt(100) < 10){
                // Spawn a random berry type based on biome? That sounds nifty. Force the players to get around a bit.
                Biome b = world.getBiome(pos);
                BlockBerry canGoHere = ModBlocks.getBerryForBiome(b);
                if(canGoHere != null){
                    world.setBlockState(pos, canGoHere.getDefaultState());
                    newBerries.add(pos);
                }
            }
        }
        return newBerries;
    }

    double twoDDistance(int xOne, int zOne, int xTwo, int zTwo){
        return Math.sqrt(Math.pow((double) (xOne - xTwo), 2) + Math.pow((double) (zOne - zTwo), 2));
    }

    void generateRoots() {
        // Roots can be generated for any sentient tree, but only if the ground below the tree is fertile.
        // Roots are the tree's method of interacting with the world (outside of blocks youaa place)
        // Roots are also the only way to draw life into the tree, be it by taking it from entities or from natural
        // flora
        BlockPos rootOrigin = basePos.down();
        // tabulate some directions and generate tendrils there.
        // Tendrils can only go through soft material
        world.setBlockState(rootOrigin, ModBlocks.rootsBlock.getDefaultState());
        int tendrilsAmt = rand.nextInt(3) + 2; // At least two tendrils are required.
        // Distance between tendril
        Iterable<BlockPos> possibleBlockPos = BlockPos.getAllInBox(rootOrigin.north(5).west(5), rootOrigin.south(5).east(5));
        List<BlockPos> bps = new ArrayList<BlockPos>();
        for(BlockPos b : possibleBlockPos){
            bps.add(b);
        }
        for(int i = 0; i < tendrilsAmt; i++){
            BlockPos randomlySelected = bps.get(rand.nextInt(bps.size()));
            // We need to get this blockpos to the original root block by way of a more or less random movement set
            world.setBlockState(randomlySelected, ModBlocks.rootsBlock.getDefaultState());
            int depth = 0;
            while(twoDDistance(randomlySelected.getX(), randomlySelected.getZ(), rootOrigin.getX(), rootOrigin.getZ()) > 1.0D){
                // South east = + x and z
                List<EnumFacing> possibleMoves = new ArrayList<EnumFacing>();
                if(randomlySelected.getX() > rootOrigin.getX()){
                    possibleMoves.add(EnumFacing.WEST);
                }else{
                    possibleMoves.add(EnumFacing.EAST);
                }
                if(randomlySelected.getZ() > rootOrigin.getZ()){
                    possibleMoves.add(EnumFacing.NORTH);
                }else{
                    possibleMoves.add(EnumFacing.SOUTH);
                }
                int choice = rand.nextInt(3);
                if(choice == 2){
                    for(EnumFacing options : possibleMoves){
                        randomlySelected = randomlySelected.offset(options);
                    }
                }else if(choice == 1){
                    if(possibleMoves.size() > 1){
                        randomlySelected = randomlySelected.offset(possibleMoves.get(choice));
                    }else{
                        randomlySelected = randomlySelected.offset(possibleMoves.get(0));
                    }
                }else{
                    randomlySelected = randomlySelected.offset(possibleMoves.get(0));
                }
                if(world.isAirBlock(randomlySelected)){
                    // See about the one under it?
                    boolean stillAir = true;
                    for(int j = 0; j < (depth + 1); j++){
                        randomlySelected = randomlySelected.down();
                        if(!world.isAirBlock(randomlySelected)){
                            break;
                        }
                    }
                }
                world.setBlockState(randomlySelected, ModBlocks.rootsBlock.getDefaultState());
                depth++;
            }
        }
    }

    private class XZPair {
        private int x, z;

        public XZPair(BlockPos pos){
            this.x = pos.getX();
            this.z = pos.getZ();
        }

        public boolean equals(Object other){
            if(other instanceof XZPair){
                XZPair o = (XZPair) other;
                return o.x == this.x && o.z == this.z;
            }
            return false;
        }
    }

    /**
     * Indicates whether or not a leaf node requires additional wood to be added to preserve integrity.
     */
    boolean leafNodeNeedsBase(int p_76493_1_)
    {
        return (double)p_76493_1_ >= (double)this.heightLimit * 0.2D;
    }

    /**
     * Places the trunk for the big tree that is being generated. Able to generate double-sized trunks by changing a
     * field that is always 1 to 2.
     */
    void generateTrunk()
    {
        BlockPos blockpos = this.basePos;
        BlockPos blockpos1 = this.basePos.up(this.height);
        Block block = ModBlocks.sentientLog;
        this.limb(blockpos, blockpos1, block);

        if (this.trunkSize == 2)
        {
            this.limb(blockpos.east(), blockpos1.east(), block);
            this.limb(blockpos.east().south(), blockpos1.east().south(), block);
            this.limb(blockpos.south(), blockpos1.south(), block);
        }
    }

    /**
     * Generates additional wood block to fill out the bases of different leaf nodes that would otherwise degrade.
     */
    void generateLeafNodeBases()
    {
        for (FoliageCoordinates worldgenbigtree$foliagecoordinates : this.foliageCoords)
        {
            int i = worldgenbigtree$foliagecoordinates.getBranchBase();
            BlockPos blockpos = new BlockPos(this.basePos.getX(), i, this.basePos.getZ());

            if (!blockpos.equals(worldgenbigtree$foliagecoordinates) && this.leafNodeNeedsBase(i - this.basePos.getY()))
            {
                this.limb(blockpos, worldgenbigtree$foliagecoordinates, ModBlocks.sentientLeaves);
            }
        }
    }

    /**
     * Checks a line of block in the world from the first coordinate to triplet to the second, returning the distance
     * (in block) before a non-air, non-leaf block is encountered and/or the end is encountered.
     */
    int checkBlockLine(BlockPos posOne, BlockPos posTwo)
    {
        BlockPos blockpos = posTwo.add(-posOne.getX(), -posOne.getY(), -posOne.getZ());
        int i = this.getGreatestDistance(blockpos);
        float f = (float)blockpos.getX() / (float)i;
        float f1 = (float)blockpos.getY() / (float)i;
        float f2 = (float)blockpos.getZ() / (float)i;

        if (i == 0)
        {
            return -1;
        }
        else
        {
            for (int j = 0; j <= i; ++j)
            {
                BlockPos blockpos1 = posOne.add((double)(0.5F + (float)j * f), (double)(0.5F + (float)j * f1), (double)(0.5F + (float)j * f2));

                if (!this.isReplaceable(world, blockpos1))
                {
                    return j;
                }
            }

            return -1;
        }
    }

    public void setDecorationDefaults()
    {
        this.leafDistanceLimit = 5;
    }

    public boolean generate(World worldIn, Random rand, BlockPos position)
    {
        this.world = worldIn;
        this.basePos = position;
        this.rand = new Random(rand.nextLong());

        if (this.heightLimit == 0)
        {
            this.heightLimit = 5 + this.rand.nextInt(this.heightLimitLimit);
        }

        if (!this.validTreeLocation())
        {
            this.world = null; //Fix vanilla Mem leak, holds latest world
            return false;
        }
        else
        {
            this.generateLeafNodeList();
            this.generateLeaves();
            this.generateTrunk();
            this.generateLeafNodeBases();
            List<BlockPos> berr = this.generateBerries();
            this.generateRoots();
            this.world = null; //Fix vanilla Mem leak, holds latest world
            // We've reached the final spot. Let's do the big bit.
            IBlockState prevState = worldIn.getBlockState(this.basePos);
            worldIn.setBlockState(this.basePos, ModBlocks.sentientTreeCore.getDefaultState());
            worldIn.notifyBlockUpdate(this.basePos, prevState, worldIn.getBlockState(this.basePos), 3);
            TESentientTreeCore core = (TESentientTreeCore) worldIn.getTileEntity(this.basePos);
            for(BlockPos bp : berr){
                core.addBerry(bp);
            }
            core.reIndexRoots();
            core.reIndexMaxLife();
            return true;
        }
    }

    /**
     * Returns a boolean indicating whether or not the current location for the tree, spanning basePos to to the height
     * limit, is valid.
     */
    private boolean validTreeLocation()
    {
        BlockPos down = this.basePos.down();
        net.minecraft.block.state.IBlockState state = this.world.getBlockState(down);
        boolean isSoil = state.getBlock().canSustainPlant(state, this.world, down, net.minecraft.util.EnumFacing.UP, ((net.minecraft.block.BlockSapling) Blocks.SAPLING));

        if (!isSoil)
        {
            return false;
        }
        else
        {
            int i = this.checkBlockLine(this.basePos, this.basePos.up(this.heightLimit - 1));

            if (i == -1)
            {
                return true;
            }
            else if (i < 6)
            {
                return false;
            }
            else
            {
                this.heightLimit = i;
                return true;
            }
        }
    }

    static class FoliageCoordinates extends BlockPos
    {
        private final int branchBase;

        public FoliageCoordinates(BlockPos pos, int p_i45635_2_)
        {
            super(pos.getX(), pos.getY(), pos.getZ());
            this.branchBase = p_i45635_2_;
        }

        public int getBranchBase()
        {
            return this.branchBase;
        }
    }
}
