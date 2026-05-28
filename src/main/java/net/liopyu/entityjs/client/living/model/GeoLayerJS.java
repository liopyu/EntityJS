package net.liopyu.entityjs.client.living.model;

import net.minecraft.client.renderer.rendertype.RenderTypes;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.liopyu.entityjs.builders.living.BaseLivingEntityBuilder;
import net.liopyu.entityjs.client.living.KubeJSEntityRenderer;
import net.liopyu.entityjs.entities.living.entityjs.IAnimatableJS;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.renderer.layer.GeoRenderLayer;

import java.awt.*;
import java.util.function.Function;

public class GeoLayerJS<T extends LivingEntity & IAnimatableJS> extends GeoRenderLayer<T> {
    public T entity;
    public final GeoLayerJSBuilder<T> geoBuilder;
    public final KubeJSEntityRenderer<T> renderer;
    public final BaseLivingEntityBuilder<T> builder;

    public GeoLayerJS(KubeJSEntityRenderer<T> entityRendererIn, GeoLayerJSBuilder<T> geoBuilder, BaseLivingEntityBuilder<T> builder) {
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
        if (geoBuilder.textureResource != null) {
            Object obj = geoBuilder.textureResource.apply(animatable);
            if (obj instanceof Identifier r) return r;
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for textureResource in newGeoLayer builder. Value: " + obj + ". Must be a Identifier. Defaulting to " + super.getTextureResource(animatable));
        }
        return super.getTextureResource(animatable);
    }

    @Override
    public void preRender(PoseStack poseStack, T animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        if (geoBuilder.preRender != null && animatable != null) {
            try {
                if (geoBuilder.renderTypeFunction != null) {
                    renderType = geoBuilder.renderTypeFunction.apply(animatable);
                }
            } catch (Exception e) {
                EntityJSHelperClass.logErrorMessageOnceCatchable("[EntityJS]: Error in " + entityName() + "builder for field: renderType.", e);
            }
            if (geoBuilder.setRenderType != null)
                renderType = geoBuilder.setRenderType;

            if (geoBuilder.setRenderType == null && geoBuilder.renderTypeFunction == null) {
                renderType = RenderTypes.entityCutout(getTextureResource(animatable));
            }
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
        try {
            if (geoBuilder.renderTypeFunction != null) {
                renderType = geoBuilder.renderTypeFunction.apply(animatable);
            }
        } catch (Exception e) {
            EntityJSHelperClass.logErrorMessageOnceCatchable("[EntityJS]: Error in " + entityName() + "builder for field: renderType.", e);
        }
        if (geoBuilder.setRenderType != null)
            renderType = geoBuilder.setRenderType;

        if (geoBuilder.setRenderType == null && geoBuilder.renderTypeFunction == null) {
            renderType = RenderTypes.entityCutout(getTextureResource(animatable));
        }
        if (geoBuilder.render != null && animatable != null) {
            final ContextUtils.PreRenderContext<T> context = new ContextUtils.PreRenderContext<>(poseStack, animatable, bakedModel, renderType, bufferSource, buffer, partialTicks, packedLightIn, packedOverlay);
            EntityJSHelperClass.consumerCallback(geoBuilder.render, context, "[EntityJS]: Error in " + entityName() + "builder for field: render");
            getRenderer().reRender(getDefaultBakedModel(animatable), poseStack, bufferSource, animatable, renderType, bufferSource.getBuffer(renderType), partialTicks, packedLightIn, OverlayTexture.NO_OVERLAY, getRenderer().getRenderColor(animatable, partialTicks, packedLightIn).argbInt());
        } else {
            getRenderer().reRender(getDefaultBakedModel(animatable), poseStack, bufferSource, animatable, renderType, bufferSource.getBuffer(renderType), partialTicks, packedLightIn, OverlayTexture.NO_OVERLAY, getRenderer().getRenderColor(animatable, partialTicks, packedLightIn).argbInt());
        }
    }
}