package net.liopyu.entityjs.client.living.model;

import net.minecraft.client.renderer.rendertype.RenderTypes;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.liopyu.entityjs.builders.misc.CustomEntityJSBuilder;
import net.liopyu.entityjs.client.living.CustomKubeJSEntityRenderer;
import net.liopyu.entityjs.client.living.KubeJSEntityRenderer;
import net.liopyu.entityjs.entities.living.entityjs.IAnimatableJS;
import net.liopyu.entityjs.entities.living.entityjs.IAnimatableJSCustom;
import net.liopyu.entityjs.entities.living.entityjs.WrappedAnimatableEntity;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.liopyu.entityjs.util.implementation.ILivingEntityJS;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.renderer.layer.GeoRenderLayer;

import java.util.function.Function;

public class CustomGeoLayerJS<T extends LivingEntity & IAnimatableJSCustom> extends GeoRenderLayer<T> {
    public T entity;
    public final CustomGeoLayerJSBuilder<T> geoBuilder;
    public final CustomKubeJSEntityRenderer<T> renderer;
    public final CustomEntityJSBuilder builder;

    public CustomGeoLayerJS(CustomKubeJSEntityRenderer<T> entityRendererIn, CustomGeoLayerJSBuilder<T> geoBuilder, CustomEntityJSBuilder builder) {
        super(entityRendererIn);
        this.geoBuilder = geoBuilder;
        this.renderer = entityRendererIn;
        this.builder = builder;
        //this.entity = entityRendererIn.getAnimatable();
    }

    public String entityName() {
        return builder.get().toString();
    }

    @Override
    protected Identifier getTextureResource(T animatable) {
        animatable = ensureIAnimatableJS(animatable);
        if (geoBuilder.textureResource != null) {
            Object obj = geoBuilder.textureResource.apply(animatable);
            if (obj instanceof Identifier r) return r;
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for textureResource in newGeoLayer builder. Value: " + obj + ". Must be a Identifier. Defaulting to " + super.getTextureResource(animatable));
        }
        return super.getTextureResource(animatable);
    }

    @Override
    public void preRender(PoseStack poseStack, T animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        animatable = ensureIAnimatableJS(animatable);
        if (geoBuilder.preRender != null && animatable != null) {
            final ContextUtils.PreRenderContext<T> context = new ContextUtils.PreRenderContext<>(poseStack, animatable, bakedModel, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
            EntityJSHelperClass.consumerCallback(geoBuilder.preRender, context, "[EntityJS]: Error in " + entityName() + "builder for field: preRender");
            super.preRender(poseStack, animatable, bakedModel, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
        } else {
            super.preRender(poseStack, animatable, bakedModel, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
        }
    }

    @Override
    public void render(PoseStack poseStack, T animatable, BakedGeoModel bakedModel, RenderType renderType,
                       MultiBufferSource bufferSource, VertexConsumer buffer, float partialTicks,
                       int packedLightIn, int packedOverlay) {
        animatable = ensureIAnimatableJS(animatable);
        if (geoBuilder.render != null && animatable != null) {
            final ContextUtils.PreRenderContext<T> context = new ContextUtils.PreRenderContext<>(poseStack, animatable, bakedModel, renderType, bufferSource, buffer, partialTicks, packedLightIn, packedOverlay);
            EntityJSHelperClass.consumerCallback(geoBuilder.render, context, "[EntityJS]: Error in " + entityName() + "builder for field: render");
            RenderType renderLayer = RenderTypes.entityCutout(getTextureResource(animatable));
            getRenderer().reRender(getDefaultBakedModel(animatable), poseStack, bufferSource, animatable, renderLayer, bufferSource.getBuffer(renderLayer), partialTicks, packedLightIn, OverlayTexture.NO_OVERLAY, getRenderer().getRenderColor(animatable, partialTicks, packedLightIn).argbInt());
        } else {
            RenderType renderLayer = RenderTypes.entityCutout(getTextureResource(animatable));
            getRenderer().reRender(getDefaultBakedModel(animatable), poseStack, bufferSource, animatable, renderLayer, bufferSource.getBuffer(renderLayer), partialTicks, packedLightIn, OverlayTexture.NO_OVERLAY, getRenderer().getRenderColor(animatable, partialTicks, packedLightIn).argbInt());
        }
    }

    /**
     * Ensures that the given entity is always an instance of IAnimatableJS.
     */
    private T ensureIAnimatableJS(LivingEntity entity) {
        if (entity instanceof IAnimatableJSCustom animatableJS) {
            return (T) animatableJS;
        } else if (entity instanceof ILivingEntityJS iLivingEntityJS) {
            return (T) iLivingEntityJS.entityJs$getAnimatableEntity();
        }

        // Wrap the entity in our custom subclass that implements IAnimatableJS
        return (T) new WrappedAnimatableEntity(entity, builder);
    }
}