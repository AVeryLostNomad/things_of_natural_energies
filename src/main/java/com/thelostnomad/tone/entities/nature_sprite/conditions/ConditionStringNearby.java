package com.thelostnomad.tone.entities.nature_sprite.conditions;

import com.google.common.base.Predicate;
import com.thelostnomad.tone.util.annotation.RecipeCondition;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

@RecipeCondition(optionalArg=double.class)
public class ConditionStringNearby extends ConditionBase {

    private double range;

    public ConditionStringNearby(){
        this(5D);
    }

    public ConditionStringNearby(double i){
        this.range = i;
    }

    @Override
    public boolean doesApply(World world, BlockPos position) {
        List<EntityItem> itemEntities = world.getEntities(EntityItem.class, new Predicate<EntityItem>() {
            @Override
            public boolean apply(@Nullable EntityItem input) {
                if(input.getItem().getItem() == Items.STRING){
                    if(input.getPosition().distanceSq(position.getX(), position.getY(), position.getZ()) < (range * range)){
                        return true;
                    }
                }
                return false;
            }
        });
        return itemEntities.size() != 0;
    }

    @Override
    public void consume(World world, BlockPos position) {
        // Eat one single gold block nearby
        List<EntityItem> itemEntities = world.getEntities(EntityItem.class, new Predicate<EntityItem>() {
            @Override
            public boolean apply(@Nullable EntityItem input) {
                if(input.getItem().getItem() == Items.STRING){
                    if(input.getPosition().distanceSq(position.getX(), position.getY(), position.getZ()) < (range * range)){
                        return true;
                    }
                }
                return false;
            }
        });
        itemEntities.get(0).setDead();
    }

}
