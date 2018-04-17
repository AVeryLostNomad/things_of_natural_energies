package com.thelostnomad.tone.teisr;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

import java.util.List;

public class SpriteBakedWrapper implements IBakedModel {

    private final IBakedModel internal;

    public SpriteBakedWrapper(IBakedModel model){
        this.internal = model;
    }

    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        return internal.getQuads(state, side, rand);
    }

    @Override
    public boolean isAmbientOcclusion() {
        return internal.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return internal.isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return true;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return internal.getParticleTexture();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return internal.getOverrides();
    }

    @Override
    public org.apache.commons.lang3.tuple.Pair handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
        return internal.handlePerspective(cameraTransformType);
    }
}
