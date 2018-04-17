package com.thelostnomad.tone.teisr;

import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import com.thelostnomad.tone.entities.nature_sprite.NatureSpriteEntity;
import com.thelostnomad.tone.entities.nature_sprite.ai.NatureSpriteAI;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.lang.reflect.Field;

public class SpriteRenderer extends RenderItem {

    public SpriteRenderer(RenderItem existing){
        this(getTextureManagerFromExisting(existing), existing.getItemModelMesher().getModelManager(), getItemColorsFromExisting(existing));
    }

    public IBakedModel getItemModelWithOverrides(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entitylivingbaseIn)
    {
        return new SpriteBakedWrapper(super.getItemModelWithOverrides(stack, worldIn, entitylivingbaseIn));
    }

    private static TextureManager getTextureManagerFromExisting(RenderItem ri){
        try{
            Field f = ri.getClass().getDeclaredField("textureManager");
            f.setAccessible(true);
            return (TextureManager) f.get(ri);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static ItemColors getItemColorsFromExisting(RenderItem ri){
        try{
            Field f = ri.getClass().getDeclaredField("itemColors");
            f.setAccessible(true);
            return (ItemColors) f.get(ri);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

    public SpriteRenderer(TextureManager p_i46552_1_, ModelManager p_i46552_2_, ItemColors p_i46552_3_) {
        super(p_i46552_1_, p_i46552_2_, p_i46552_3_);
    }
}
