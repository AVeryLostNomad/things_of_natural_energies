package com.thelostnomad.tone.entities.nature_sprite.ai;

import com.google.common.base.Predicate;
import com.thelostnomad.tone.entities.nature_sprite.NatureSpriteEntity;
import com.thelostnomad.tone.registry.ModItems;
import com.thelostnomad.tone.util.annotation.SpriteAI;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nullable;
import java.util.List;

@SpriteAI(optionalArg=long.class)
public class LookForStorageChestIfNeeded extends NatureSpriteAI {
    private final NatureSpriteEntity parentEntity;
    private long cooldown;

    public LookForStorageChestIfNeeded(NatureSpriteEntity ghast) {
        this(ghast, 60L);
    }

    public int getImportance() {
        return 5;
    }

    @Override
    public double getActionCost() {
        return 0;
    }

    public LookForStorageChestIfNeeded(NatureSpriteEntity ghast, long cooldown) {
        this.parentEntity = ghast;
        this.setMutexBits(4);
        this.cooldown = cooldown;
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute() {
        if (!this.parentEntity.flags.containsKey("targetInventory") && getNearbyShard().size() == 1) {
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

    public List<EntityItem> getNearbyShard() {
        List<EntityItem> nearbyPlayers = parentEntity.world.getEntities(EntityItem.class, new Predicate<EntityItem>() {
            @Override
            public boolean apply(@Nullable EntityItem input) {
                return input.getPosition().distanceSq(parentEntity.getPosition()) < 64 && input.getItem().getItem() == ModItems.shardOfSentience;
            }
        });
        return nearbyPlayers;
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting() {
        EntityItem shardNearby = getNearbyShard().get(0);

        BlockPos below = shardNearby.getPosition().down();
        IBlockState there = parentEntity.world.getBlockState(below);

        TileEntity teThere = parentEntity.world.getTileEntity(below);
        if(teThere == null) return;
        boolean hasItemCapability = teThere.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
        if(!hasItemCapability) return;

        getNearbyShard().get(0).setDead();
        parentEntity.flags.put("targetInventory", below);
    }
}
