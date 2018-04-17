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
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemDye;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.List;

@SpriteAI(optionalArg=long.class)
public class DebugIAmA extends NatureSpriteAI
{
    private final NatureSpriteEntity parentEntity;
    private long cooldown;

    public DebugIAmA(NatureSpriteEntity ghast)
    {
        this(ghast, 60L);
    }

    public int getImportance(){
        return 5;
    }

    @Override
    public double getActionCost() {
        return 0;
    }

    public DebugIAmA(NatureSpriteEntity ghast, long cooldown){
        this.parentEntity = ghast;
        this.setMutexBits(4);
        this.cooldown = cooldown;
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        ThingsOfNaturalEnergies.logger.error("I am a " + this.parentEntity.getSpeciesHelper().getInternalName());
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
    }
}