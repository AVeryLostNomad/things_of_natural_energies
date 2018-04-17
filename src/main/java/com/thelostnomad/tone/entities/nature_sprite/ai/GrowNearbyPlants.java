package com.thelostnomad.tone.entities.nature_sprite.ai;

import com.google.common.base.Predicate;
import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import com.thelostnomad.tone.entities.nature_sprite.NatureSpriteEntity;
import com.thelostnomad.tone.network.SpritePlayEffectPacket;
import com.thelostnomad.tone.network.SpriteSingPacket;
import com.thelostnomad.tone.network.TonePacketHandler;
import com.thelostnomad.tone.util.annotation.SpriteAI;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemDye;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.List;

@SpriteAI(optionalArg=long.class)
public class GrowNearbyPlants extends NatureSpriteAI
{
    private final NatureSpriteEntity parentEntity;
    private long cooldown;

    public GrowNearbyPlants(NatureSpriteEntity ghast)
    {
        this(ghast, 60L);
    }

    public int getImportance(){
        return 5;
    }

    @Override
    public double getActionCost() {
        return 0.75;
    }

    public GrowNearbyPlants(NatureSpriteEntity ghast, long cooldown){
        this.parentEntity = ghast;
        this.setMutexBits(4);
        this.cooldown = cooldown;
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        if(this.parentEntity.isResting()) return false;
        long nowTime = this.parentEntity.getEntityWorld().getTotalWorldTime();

        if(this.parentEntity.getTimeLastActionMap().containsKey(0)){
            long lastTime = this.parentEntity.getTimeLastActionMap().get(0);
            if((nowTime - lastTime) < cooldown){
                return false;
            }
        }

        BlockPos leftCorner = new BlockPos(this.parentEntity.getPosition()).south().west().down();
        BlockPos rightCorner = new BlockPos(this.parentEntity.getPosition()).north().east().up();
        Iterable<BlockPos> surrounding = BlockPos.getAllInBox(leftCorner, rightCorner);
        for(BlockPos bp : surrounding){
            if(this.parentEntity.getEntityWorld().getBlockState(bp).getBlock() instanceof BlockCrops){
                // Grow this?
                IBlockState bc = (IBlockState) this.parentEntity.getEntityWorld().getBlockState(bp);
                BlockCrops blockCrops = (BlockCrops) bc.getBlock();
                if(!blockCrops.isMaxAge(bc)){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean shouldContinueExecuting()
    {
        return false;
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
        BlockPos leftCorner = new BlockPos(this.parentEntity.getPosition()).south().west().down();
        BlockPos rightCorner = new BlockPos(this.parentEntity.getPosition()).north().east().up();
        Iterable<BlockPos> surrounding = BlockPos.getAllInBox(leftCorner, rightCorner);
        for(BlockPos bp : surrounding){
            if(this.parentEntity.getEntityWorld().getBlockState(bp).getBlock() instanceof BlockCrops){
                // Grow this?
                IBlockState bc = (IBlockState) this.parentEntity.getEntityWorld().getBlockState(bp);
                BlockCrops blockCrops = (BlockCrops) bc.getBlock();
                if(!blockCrops.isMaxAge(bc)){
                    if(parentEntity.world.isRemote){
                        ItemDye.spawnBonemealParticles(this.parentEntity.world, bp, 5);
                    }else{
                        blockCrops.grow(this.parentEntity.world, bp, bc);
                        List<EntityPlayerMP> entities = this.parentEntity.getEntityWorld().getEntities(EntityPlayerMP.class, new Predicate<EntityPlayerMP>() {
                            @Override
                            public boolean apply(@Nullable EntityPlayerMP input) {
                                return input.getPosition().distanceSq(parentEntity.posX, parentEntity.posY, parentEntity.posZ) < 64;
                            }
                        });
                        for(EntityPlayerMP ep : entities){
                            TonePacketHandler.sendTo(new SpriteSingPacket(bp), ep);
                            TonePacketHandler.sendTo(new SpritePlayEffectPacket(bp, EnumParticleTypes.VILLAGER_HAPPY, 5), ep);
                        }
                    }
                    this.parentEntity.getTimeLastActionMap().put(0, this.parentEntity.world.getTotalWorldTime());
                    this.parentEntity.setStamina(this.parentEntity.getStamina() - getActionCost());
                    break;
                }
            }
        }
    }
}