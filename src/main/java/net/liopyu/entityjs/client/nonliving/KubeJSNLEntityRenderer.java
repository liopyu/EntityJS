package net.liopyu.entityjs.client.nonliving;

import net.minecraft.client.renderer.rendertype.RenderTypes;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.liopyu.entityjs.builders.nonliving.BaseEntityBuilder;
import net.liopyu.entityjs.builders.nonliving.entityjs.ProjectileAnimatableJSBuilder;
import net.liopyu.entityjs.client.nonliving.model.NLGeoLayerJS;
import net.liopyu.entityjs.client.nonliving.model.NLGeoLayerJSBuilder;
import net.liopyu.entityjs.client.nonliving.model.NLGlowingGeoLayerJS;
import net.liopyu.entityjs.client.nonliving.model.NonLivingEntityModel;
import net.liopyu.entityjs.entities.nonliving.entityjs.IAnimatableJSNL;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.renderer.GeoEntityRenderer;

import javax.annotation.Nullable;


public class KubeJSNLEntityRenderer<T extends Entity & IAnimatableJSNL> extends GeoEntityRenderer<T> {

    private final BaseEntityBuilder<T> builder;

    public KubeJSNLEntityRenderer(EntityRendererProvider.Context renderManager, BaseEntityBuilder<T> builder) {
        super(renderManager, new NonLivingEntityModel<>(builder));
        this.builder = builder;
        this.scaleHeight = getScaleHeight();
        this.scaleWidth = getScaleWidth();
        for (NLGeoLayerJSBuilder<T> geoBuilder : builder.layerList) {
            NLGeoLayerJS<T> layerPart = geoBuilder.build(this, builder);
            addRenderLayer(layerPart);
        }
        for (NLGeoLayerJSBuilder<T> geoBuilder : builder.glowingLayerList) {
            NLGlowingGeoLayerJS<T> layerPart = geoBuilder.buildGlowing(this, builder);
            addRenderLayer(layerPart);
        }
    }

    public String entityName() {
        return this.animatable.getType().toString();
    }

    public float getScaleHeight() {
        return builder.scaleHeight;
    }

    public float getScaleWidth() {
        return builder.scaleWidth;
    }

    @Override
    public void scaleModelForRender(float widthScale, float heightScale, PoseStack poseStack, T animatable, BakedGeoModel model, boolean isReRender, float partialTick, int packedLight, int packedOverlay) {

        if (builder.scaleModelForRender != null && this.animatable != null) {
            final ContextUtils.ScaleModelRenderContextNL<T> context = new ContextUtils.ScaleModelRenderContextNL<>(widthScale, heightScale, poseStack, animatable, model, isReRender, partialTick, packedLight, packedOverlay);
            EntityJSHelperClass.consumerCallback(builder.scaleModelForRender, context, "[EntityJS]: Error in " + entityName() + "builder for field: scaleModelForRender.");
            super.scaleModelForRender(widthScale, heightScale, poseStack, animatable, model, isReRender, partialTick, packedLight, packedOverlay);
        } else
            super.scaleModelForRender(widthScale, heightScale, poseStack, animatable, model, isReRender, partialTick, packedLight, packedOverlay);
    }

    @Override
    public Identifier getTextureLocation(T entity) {
        return (Identifier) builder.textureResource.apply(entity);
    }

    @Override
    public RenderType getRenderType(T animatable, Identifier texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        if (builder.renderTypeFunction != null) {
            try {
                return builder.renderTypeFunction.apply(animatable);
            } catch (RuntimeException e) {
                EntityJSHelperClass.logErrorMessageOnceCatchable("[EntityJS]: Error in renderTypeFunction.", e);
            }
        }
        return switch (animatable.getBuilder().renderType) {
            case SOLID -> RenderTypes.entitySolid(texture);
            case CUTOUT -> RenderTypes.entityCutout(texture);
            case TRANSLUCENT -> RenderTypes.entityTranslucent(texture);

        };
    }

    @Override
    public void render(T animatable, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        if (builder.facesTrajectory) {
            Vec3 velocity = animatable.getDeltaMovement();
            double velX = velocity.x();
            double velY = velocity.y();
            double velZ = velocity.z();
            float yaw = (float) (Math.atan2(velZ, velX) * (180 / Math.PI) - 90);
            float pitch = (float) (Math.atan2(velY, Math.sqrt(velX * velX + velZ * velZ)) * (180 / Math.PI));
            poseStack.mulPose(Axis.YP.rotationDegrees(-yaw));
            poseStack.mulPose(Axis.XP.rotationDegrees(-pitch));
        }
        if (builder.render != null && this.animatable != null) {
            final ContextUtils.NLRenderContext<T> context = new ContextUtils.NLRenderContext<>(animatable, entityYaw, partialTick, poseStack, bufferSource, packedLight);
            EntityJSHelperClass.consumerCallback(builder.render, context, "[EntityJS]: Error in " + entityName() + "builder for field: render.");
            super.render(animatable, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        } else {
            super.render(animatable, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        }
    }
}

