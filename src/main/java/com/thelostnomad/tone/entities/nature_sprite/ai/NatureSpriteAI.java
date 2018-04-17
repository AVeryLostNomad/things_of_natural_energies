package com.thelostnomad.tone.entities.nature_sprite.ai;

import net.minecraft.entity.ai.EntityAIBase;

public abstract class NatureSpriteAI extends EntityAIBase {

    public abstract int getImportance();

    public abstract double getActionCost();

}
