package com.thelostnomad.tone.entities.nature_sprite.ai;

import com.google.common.base.Predicate;
import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import com.thelostnomad.tone.entities.nature_sprite.NatureSpriteEntity;
import com.thelostnomad.tone.util.annotation.SpriteAI;
import net.minecraft.entity.player.EntityPlayer;

import javax.annotation.Nullable;
import java.util.List;

@SpriteAI(optionalArg=long.class)
public class SelectTargetPlayerIfNone extends NatureSpriteAI {
    private final NatureSpriteEntity parentEntity;
    private long cooldown;

    public SelectTargetPlayerIfNone(NatureSpriteEntity ghast) {
        this(ghast, 60L);
    }

    public int getImportance() {
        return 5;
    }

    @Override
    public double getActionCost() {
        return 0;
    }

    public SelectTargetPlayerIfNone(NatureSpriteEntity ghast, long cooldown) {
        this.parentEntity = ghast;
        this.setMutexBits(4);
        this.cooldown = cooldown;
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute() {
        if (!this.parentEntity.flags.containsKey("targetPlayer") && getNearbyPlayers().size() != 0) {
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

    public List<EntityPlayer> getNearbyPlayers() {
        List<EntityPlayer> nearbyPlayers = parentEntity.world.getEntities(EntityPlayer.class, new Predicate<EntityPlayer>() {
            @Override
            public boolean apply(@Nullable EntityPlayer input) {
                return input.getPosition().distanceSq(parentEntity.getPosition()) < 400;
            }
        });
        return nearbyPlayers;
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting() {
        EntityPlayer closest = null;
        double dist = Double.MAX_VALUE;

        for(EntityPlayer ep : getNearbyPlayers()){
            if(ep.getPosition().distanceSq(parentEntity.getPosition()) < dist){
                closest = ep;
                dist = ep.getPosition().distanceSq(parentEntity.getPosition());
            }
        }

        parentEntity.flags.put("targetPlayer", closest.getDisplayName());
    }
}
