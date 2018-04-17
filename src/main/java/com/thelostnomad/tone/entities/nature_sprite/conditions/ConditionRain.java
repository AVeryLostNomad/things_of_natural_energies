package com.thelostnomad.tone.entities.nature_sprite.conditions;

import com.thelostnomad.tone.util.annotation.RecipeCondition;
import com.thelostnomad.tone.util.annotation.SpriteAI;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@RecipeCondition(optionalArg=boolean.class)
public class ConditionRain extends ConditionBase {

    private boolean requisiteRain = false;

    public ConditionRain(){
        this(false);
    }

    public ConditionRain(boolean val){
        this.requisiteRain = val;
    }

    public boolean doesApply(World world, BlockPos position){
        return requisiteRain ? world.isRainingAt(position) : !world.isRainingAt(position);
    }

    @Override
    public void consume(World world, BlockPos position) {
        return;
    }

}
