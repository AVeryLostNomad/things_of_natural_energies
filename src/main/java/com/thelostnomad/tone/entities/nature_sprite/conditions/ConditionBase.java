package com.thelostnomad.tone.entities.nature_sprite.conditions;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class ConditionBase {

    public abstract boolean doesApply(World world, BlockPos position);

    public abstract void consume(World world, BlockPos position);

}
