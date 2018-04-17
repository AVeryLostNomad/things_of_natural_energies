package com.thelostnomad.tone.entities.nature_sprite.ai;

import com.google.common.base.Predicate;
import com.thelostnomad.tone.entities.nature_sprite.NatureSpriteEntity;
import com.thelostnomad.tone.network.SpriteSingPacket;
import com.thelostnomad.tone.network.TonePacketHandler;
import com.thelostnomad.tone.util.annotation.SpriteAI;
import com.thelostnomad.tone.util.sound.LocalSoundCrafting;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayerMP;

import javax.annotation.Nullable;
import java.util.List;

@SpriteAI(optionalArg=int.class)
public class NormalSingWhenRested extends NatureSpriteAI {

    private final NatureSpriteEntity parentEntity;
    private long lastStaminaIncrease = 0L;

    public NormalSingWhenRested(NatureSpriteEntity ghast) {
        this.parentEntity = ghast;
        this.setMutexBits(4);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute() {
        if (parentEntity.getStamina() >= (this.parentEntity.getSpeciesHelper().getStamina() * 0.75)) {
            return true;
        }
        return false;
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean shouldContinueExecuting() {
        return false;
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting() {
        updateTask();
    }

    /**
     * Keep ticking a continuous task that has already been started
     */
    public void updateTask() {
        if (parentEntity.world.rand.nextInt(100) < 15) {
            List<EntityPlayerMP> entities = this.parentEntity.getEntityWorld().getEntities(EntityPlayerMP.class, new Predicate<EntityPlayerMP>() {
                @Override
                public boolean apply(@Nullable EntityPlayerMP input) {
                    return input.getPosition().distanceSq(parentEntity.posX, parentEntity.posY, parentEntity.posZ) < 64;
                }
            });
            for (EntityPlayerMP ep : entities) {
                TonePacketHandler.sendTo(new SpriteSingPacket(this.parentEntity.getPosition()), ep);
            }
            LocalSoundCrafting.addSoundEvent(new LocalSoundCrafting.SoundEvent(this.parentEntity, parentEntity.getPosition(), parentEntity.world));
        }
    }

    @Override
    public int getImportance() {
        return 7;
    }

    @Override
    public double getActionCost() {
        return 0;
    }
}

