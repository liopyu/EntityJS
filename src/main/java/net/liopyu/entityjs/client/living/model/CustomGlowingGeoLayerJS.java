package net.liopyu.entityjs.client.living.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.liopyu.entityjs.builders.living.BaseLivingEntityBuilder;
import net.liopyu.entityjs.builders.misc.CustomEntityJSBuilder;
import net.liopyu.entityjs.client.living.CustomKubeJSEntityRenderer;
import net.liopyu.entityjs.client.living.KubeJSEntityRenderer;
import net.liopyu.entityjs.entities.living.entityjs.IAnimatableJS;
import net.liopyu.entityjs.entities.living.entityjs.IAnimatableJSCustom;
import net.liopyu.entityjs.entities.living.entityjs.WrappedAnimatableEntity;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import java.util.function.Function;

public class CustomGlowingGeoLayerJS<T extends LivingEntity & IAnimatableJSCustom> extends AutoGlowingGeoLayer<T> {
    public T entity;
    public final CustomGeoLayerJSBuilder<T> geoBuilder;
    public final CustomKubeJSEntityRenderer<T> renderer;
    public final CustomEntityJSBuilder builder;

    public CustomGlowingGeoLayerJS(CustomKubeJSEntityRenderer<T> entityRendererIn, CustomGeoLayerJSBuilder<T> geoBuilder, CustomEntityJSBuilder builder) {
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
    protected ResourceLocation getTextureResource(T animatable) {
        if (geoBuilder.textureResource != null) {
            Object obj = geoBuilder.textureResource.apply(animatable);
            if (obj instanceof ResourceLocation r) return r;
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for textureResource in newGeoLayer builder. Value: " + obj + ". Must be a ResourceLocation. Defaulting to " + super.getTextureResource(animatable));
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
            super.render(poseStack, animatable, bakedModel, renderType, bufferSource, buffer, partialTicks, packedLightIn, packedOverlay);
        } else {
            RenderType renderLayer = RenderType.entityCutoutNoCull(getTextureResource(animatable));
            getRenderer().reRender(getDefaultBakedModel(animatable), poseStack, bufferSource, animatable, renderLayer, bufferSource.getBuffer(renderLayer), partialTicks, 15728880, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        }
    }

    /**
     * Ensures that the given entity is always an instance of IAnimatableJS.
     */
    private T ensureIAnimatableJS(LivingEntity entity) {
        if (entity instanceof IAnimatableJS animatableJS) {
            return (T) animatableJS;
        }

        // Wrap the entity in our custom subclass that implements IAnimatableJS
        return (T) new WrappedAnimatableEntity(entity, builder);
    }
}