package com.thelostnomad.tone.registry;

import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import com.thelostnomad.tone.entities.nature_sprite.NatureSpriteEntity;
import com.thelostnomad.tone.teisr.SpecialSnowballRender;
import com.thelostnomad.tone.teisr.SpriteRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ModEntities {

    public static void init() {
        // Every entity in our mod has an ID (local to this mod)
        int id = 1;
        EntityRegistry.registerModEntity(new ResourceLocation(ThingsOfNaturalEnergies.MODID, "entities/nature_sprite"), NatureSpriteEntity.class, "nature_sprite", id++, ThingsOfNaturalEnergies.instance, 64, 3, true, 0x996600, 0x00ff00);

        // We want our mob to spawn in Plains and ice plains biomes. If you don't add this then it will not spawn automatically
        // but you can of course still make it spawn manually
        //EntityRegistry.addSpawn(NatureSpriteEntity.class, 100, 3, 5, EnumCreatureType.MONSTER, Biomes.PLAINS, Biomes.ICE_PLAINS);

        // This is the loot table for our mob
        LootTableList.register(NatureSpriteEntity.LOOT);
    }

    @SideOnly(Side.CLIENT)
    public static void initModels() {
        RenderingRegistry.registerEntityRenderingHandler(NatureSpriteEntity.class, renderManager -> new SpecialSnowballRender(renderManager, ModItems.natureSpriteBaseItem, new SpriteRenderer(Minecraft.getMinecraft().getRenderItem())));
    }

}
