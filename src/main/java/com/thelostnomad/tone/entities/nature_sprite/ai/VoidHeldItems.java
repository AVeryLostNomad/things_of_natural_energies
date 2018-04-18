package com.thelostnomad.tone.entities.nature_sprite.ai;

import com.google.common.base.Predicate;
import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import com.thelostnomad.tone.entities.nature_sprite.NatureSpriteEntity;
import com.thelostnomad.tone.network.SpriteSingPacket;
import com.thelostnomad.tone.network.TonePacketHandler;
import com.thelostnomad.tone.util.annotation.SpriteAI;
import com.thelostnomad.tone.util.sound.LocalSoundCrafting;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

@SpriteAI(optionalArg=int.class)
public class VoidHeldItems extends NatureSpriteAI {

    private final NatureSpriteEntity parentEntity;
    private int carryCapacity;
    private long lastStaminaIncrease = 0L;

    public VoidHeldItems(NatureSpriteEntity ghast) {
        this(ghast, 1);
    }

    public VoidHeldItems(NatureSpriteEntity ghast, int carryCapacity){
        this.parentEntity = ghast;
        this.carryCapacity = carryCapacity;
        this.setMutexBits(4);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute() {
        if(holdingAnyItems()) return true;
        return false;
    }

    public boolean holdingAnyItems() {
        for(int i = 0; i < carryCapacity; i++){
            ItemStack inSlot = parentEntity.getHeldItemstacks()[i];
            if(inSlot != null && !inSlot.isEmpty()){
                return true;
            }
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
        ThingsOfNaturalEnergies.logger.error("Voiding all items");
        parentEntity.setHeldItemstacks(new ItemStack[27]);
    }

    /**
     * Keep ticking a continuous task that has already been started
     */
    public void updateTask() {
    }

    @Override
    public int getImportance() {
        return 7;
    }

    @Override
    public double getActionCost() {
        return 1;
    }
}

