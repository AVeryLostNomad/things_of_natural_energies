package com.thelostnomad.tone.util.sound;

import com.google.common.base.Predicate;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import com.thelostnomad.tone.entities.nature_sprite.NatureSpriteEntity;
import com.thelostnomad.tone.entities.nature_sprite.SpeciesHelper;
import com.thelostnomad.tone.entities.nature_sprite.conditions.ConditionBase;
import com.thelostnomad.tone.entities.nature_sprite.recipe.SongRecipe;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class LocalSoundCrafting {

    private static final Cache<SoundEvent, Integer> SOUND_CACHE = CacheBuilder.newBuilder().expireAfterWrite(5L, TimeUnit.SECONDS).build();
    private static List<SongRecipe> allSongRecipesInMemory = new ArrayList<SongRecipe>();

    private static List<NatureSpriteEntity> singing = new ArrayList<>();

    public static void addSoundEvent(SoundEvent se) {
        SOUND_CACHE.put(se, 0);
        singing.clear();
        for (SongRecipe sr : allSongRecipesInMemory) {
            boolean canDoThisSong = true;
            List<String> singingSprites = new ArrayList<>(sr.getSingingSprites());
            for (Map.Entry<SoundEvent, Integer> entry : SOUND_CACHE.asMap().entrySet()) {
                NatureSpriteEntity event = entry.getKey().cause;
                if (event == null) continue;
                if (!singingSprites.contains(event.getSpeciesHelper().getInternalName())) {
                    singing.clear();
                    canDoThisSong = false;
                    break;
                }else{
                    // It does contain
                    singingSprites.remove(event.getSpeciesHelper().getInternalName());
                    singing.add(event);
                    if(singingSprites.size() == 0){
                        break;
                    }
                }
            }

            if (!canDoThisSong) {
                singing.clear();
                continue;
            }

            for (ConditionBase cb : sr.getConditions()) {
                if (!cb.doesApply(se.world, se.position)) {
                    singing.clear();
                    canDoThisSong = false;
                    break;
                }
            }

            if (!canDoThisSong) {
                singing.clear();
                continue;
            }

            // We got here, so the song can happen.
            doCraft(sr, se.world, se.position);
        }
    }

    public static void doCraft(SongRecipe sr, World world, BlockPos where){
        for(NatureSpriteEntity nse : singing){
            nse.setDead();
        }

        for(ConditionBase cb : sr.getConditions()){
            cb.consume(world, where);
        }

        SpeciesHelper sh = SpeciesHelper.fromInternalName(sr.getTarget());
        System.out.println(sh.getStamina());
        NatureSpriteEntity nse = new NatureSpriteEntity(world, SpeciesHelper.fromInternalName(sr.getTarget()));
        nse.setPosition(where.getX(), where.getY(), where.getZ());
        nse.setStamina(nse.getStamina() / 2D);
        world.spawnEntity(nse);
        // Play a sound effect indicating we got here
        removeAllLocalSoundEvents(world, where);
    }

    private static void removeAllLocalSoundEvents(World world, BlockPos where){
        for (Map.Entry<SoundEvent, Integer> entry : SOUND_CACHE.asMap().entrySet()) {
            if(entry.getKey().world == world){
                if(entry.getKey().position.distanceSq(where.getX(), where.getY(), where.getZ()) <= 64){
                    SOUND_CACHE.invalidate(entry.getKey());
                }
            }
        }
    }

    public static void addSongRecipe(SongRecipe sr) {
        allSongRecipesInMemory.add(sr);
    }

    public static class SoundEvent {

        private NatureSpriteEntity cause;
        private BlockPos blockCause;
        private BlockPos position;
        private World world;

        public SoundEvent(NatureSpriteEntity cause, BlockPos where, World world) {
            this.cause = cause;
            this.position = where;
            this.world = world;
        }

        public SoundEvent(BlockPos cause, BlockPos where, World world) {
            this.blockCause = cause;
            this.position = where;
            this.world = world;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SoundEvent that = (SoundEvent) o;
            if (cause == null && that.cause == null) {
                return Objects.equals(cause, that.cause) &&
                        Objects.equals(position, that.position) &&
                        Objects.equals(world, that.world);
            }
            if (blockCause == null && that.blockCause == null) {
                return Objects.equals(blockCause, that.blockCause) &&
                        Objects.equals(position, that.position) &&
                        Objects.equals(world, that.world);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(cause, blockCause, position);
        }
    }

}
