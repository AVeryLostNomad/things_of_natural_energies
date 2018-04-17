package com.thelostnomad.tone.teisr;

import com.google.common.base.Predicate;
import com.thelostnomad.tone.ThingsOfNaturalEnergies;
import com.thelostnomad.tone.entities.nature_sprite.NatureSpriteEntity;
import com.thelostnomad.tone.entities.nature_sprite.SpeciesHelper;
import com.thelostnomad.tone.registry.ModItems;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

public class SpecialSnowballRender extends RenderSnowball<NatureSpriteEntity> {

    public SpecialSnowballRender(RenderManager renderManagerIn, Item itemIn, RenderItem itemRendererIn) {
        super(renderManagerIn, itemIn, itemRendererIn);
    }

    public ItemStack getStackToRender(NatureSpriteEntity entityIn)
    {
        SpeciesHelper speciesHelper = entityIn.getSpeciesHelper();
        String itemField = speciesHelper.getItemField();

        if(itemField.isEmpty() || itemField.equals("")){
            speciesHelper.reload();
        }

        try {
            Item i = (Item) ModItems.class.getField(itemField).get(null);
            return new ItemStack(i);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

}
