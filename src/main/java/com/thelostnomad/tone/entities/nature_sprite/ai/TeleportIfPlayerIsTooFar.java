package com.thelostnomad.tone.entities.nature_sprite.ai;

import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import com.thelostnomad.tone.entities.nature_sprite.NatureSpriteEntity;
import com.thelostnomad.tone.util.annotation.SpriteAI;

@SpriteAI(optionalArg=long.class)
public class TeleportIfPlayerIsTooFar extends NatureSpriteAI
{
    private final NatureSpriteEntity parentEntity;
    private long cooldown;

    public TeleportIfPlayerIsTooFar(NatureSpriteEntity ghast)
    {
        this(ghast, 60L);
    }

    public int getImportance(){
        return 12;
    }

    @Override
    public double getActionCost() {
        return 20;
    }

    public TeleportIfPlayerIsTooFar(NatureSpriteEntity ghast, long cooldown){
        this.parentEntity = ghast;
        this.setMutexBits(4);
        this.cooldown = cooldown;
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        if(parentEntity.isResting()) return false;
        if(parentEntity.flags.containsKey("targetPlayer") && parentEntity.getTargetPlayerIsOnline()){
            if(areTooFar()){
                ThingsOfNaturalEnergies.logger.error("You're too far, player");
                return true;
            }
        }
        return false;
    }

    public boolean areTooFar(){
        if(inDifferentWorlds()){
            return true;
        }
        if(parentEntity.getPosition().distanceSq(parentEntity.getTargetPlayer().getPosition()) >= 400){
            return true;
        }
        return false;
    }

    public boolean inDifferentWorlds(){
        return !parentEntity.getEntityWorld().getWorldInfo().getWorldName().equals(parentEntity.getTargetPlayer().getEntityWorld().getWorldInfo().getWorldName());
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
        parentEntity.setWorld(parentEntity.getTargetPlayer().getEntityWorld());
        parentEntity.attemptTeleport(parentEntity.getTargetPlayer().getPosition().getX(), parentEntity.getTargetPlayer().getPosition().getY(), parentEntity.getTargetPlayer().getPosition().getZ());
        this.parentEntity.setStamina(this.parentEntity.getStamina() - getActionCost());
    }
}