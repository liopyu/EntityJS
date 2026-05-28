package net.liopyu.entityjs.client.living;

import net.minecraft.client.renderer.rendertype.RenderTypes;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.liopyu.entityjs.builders.misc.CustomEntityJSBuilder;
import net.liopyu.entityjs.client.living.model.*;
import net.liopyu.entityjs.entities.living.entityjs.IAnimatableJS;
import net.liopyu.entityjs.entities.living.entityjs.IAnimatableJSCustom;
import net.liopyu.entityjs.entities.living.entityjs.WrappedAnimatableEntity;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.liopyu.entityjs.util.implementation.ILivingEntityJS;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import com.geckolib.util.RenderUtil;

import javax.annotation.Nullable;

/**
 * The default implementation of GeckoLib's {@link GeoEntityRenderer} which delegates to the entity
 * type builder and {@link EntityModelJS} where it makes sense
 */
public class CustomKubeJSEntityRenderer<T extends LivingEntity & IAnimatableJSCustom> extends GeoEntityRenderer<T> {

    private final CustomEntityJSBuilder builder;

    public CustomKubeJSEntityRenderer(EntityRendererProvider.Context renderManager, CustomEntityJSBuilder builder) {
        super(renderManager, new CustomEntityModelJS<>(builder));
        this.builder = builder;
        this.scaleHeight = getScaleHeight();
        this.scaleWidth = getScaleWidth();
        for (CustomGeoLayerJSBuilder geoBuilder : builder.layerList) {
            CustomGeoLayerJS layerPart = geoBuilder.build(this, builder);
            addRenderLayer(layerPart);
        }
        for (CustomGeoLayerJSBuilder geoBuilder : builder.glowingLayerList) {
            CustomGlowingGeoLayerJS layerPart = geoBuilder.buildGlowing(this, builder);
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
    public void scaleModelForRender(float widthScale, float heightScale, PoseStack poseStack, T a, BakedGeoModel model, boolean isReRender, float partialTick, int packedLight, int packedOverlay) {
        LivingEntity animatable = a;
        if (animatable instanceof WrappedAnimatableEntity wrappedAnimatableEntity) {
            animatable = wrappedAnimatableEntity.getOriginalEntity();
        }
        if (builder.scaleModelForRender != null && this.animatable != null) {
            final ContextUtils.ScaleModelRenderContext context = new ContextUtils.ScaleModelRenderContext(this, widthScale, heightScale, poseStack, animatable, model, isReRender, partialTick, packedLight, packedOverlay);
            EntityJSHelperClass.consumerCallback(builder.scaleModelForRender, context, "[EntityJS]: Error in " + entityName() + "builder for field: scaleModelForRender.");
            super.scaleModelForRender(widthScale, heightScale, poseStack, ensureIAnimatableJS(animatable), model, isReRender, partialTick, packedLight, packedOverlay);
        } else
            super.scaleModelForRender(widthScale, heightScale, poseStack, ensureIAnimatableJS(animatable), model, isReRender, partialTick, packedLight, packedOverlay);
    }

    @Override
    public Identifier getTextureLocation(T entity) {
        return (Identifier) builder.textureResource.apply(entity);
    }

    @Override
    public RenderType getRenderType(T animatable, Identifier texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        try {
            if (builder.renderTypeFunction != null) {
                return builder.renderTypeFunction.apply(ensureIAnimatableJS(animatable));
            }
        } catch (Exception e) {
            EntityJSHelperClass.logErrorMessageOnceCatchable("[EntityJS]: Error in " + entityName() + "builder for field: renderTypeFunction.", e);
        }
        return switch (ensureIAnimatableJS(animatable).getBuilder().renderType) {
            case SOLID -> RenderTypes.entitySolid(texture);
            case CUTOUT -> RenderTypes.entityCutout(texture);
            case TRANSLUCENT -> RenderTypes.entityTranslucent(texture);
        };
    }


    @Override
    public void render(T a, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        LivingEntity animatable = a;
        if (animatable instanceof WrappedAnimatableEntity wrappedAnimatableEntity) {
            animatable = wrappedAnimatableEntity.getOriginalEntity();
        }
        if (builder.render != null && this.animatable != null) {
            final ContextUtils.RenderContextCustom<T> context = new ContextUtils.RenderContextCustom<>(ensureIAnimatableJS(animatable), entityYaw, partialTick, poseStack, bufferSource, packedLight);
            EntityJSHelperClass.consumerCallback(builder.render, context, "[EntityJS]: Error in " + entityName() + "builder for field: render.");
            super.render(ensureIAnimatableJS(animatable), entityYaw, partialTick, poseStack, bufferSource, packedLight);
        } else {
            super.render(ensureIAnimatableJS(animatable), entityYaw, partialTick, poseStack, bufferSource, packedLight);
        }
    }

    @Override
    protected void applyRotations(T a, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick, float nativeScale) {
        LivingEntity animatable = a;
        if (animatable instanceof WrappedAnimatableEntity wrappedAnimatableEntity) {
            animatable = wrappedAnimatableEntity.getOriginalEntity();
        }
        if (isShaking(ensureIAnimatableJS(animatable)))
            rotationYaw += (float) (Math.cos(animatable.tickCount * 3.25d) * Math.PI * 0.4d);

        if (!animatable.hasPose(Pose.SLEEPING))
            poseStack.mulPose(Axis.YP.rotationDegrees(180f - rotationYaw));

        if (animatable instanceof LivingEntity livingEntity) {
            if (livingEntity.deathTime > 0 && builder.defaultDeathPose) {
                float deathRotation = (livingEntity.deathTime + partialTick - 1f) / 20f * 1.6f;

                poseStack.mulPose(Axis.ZP.rotationDegrees(Math.min(Mth.sqrt(deathRotation), 1) * getDeathMaxRotation(ensureIAnimatableJS(animatable))));
            } else if (livingEntity.isAutoSpinAttack()) {
                poseStack.mulPose(Axis.XP.rotationDegrees(-90f - livingEntity.getXRot()));
                poseStack.mulPose(Axis.YP.rotationDegrees((livingEntity.tickCount + partialTick) * -75f));
            } else if (animatable.hasPose(Pose.SLEEPING)) {
                Direction bedOrientation = livingEntity.getBedOrientation();

                poseStack.mulPose(Axis.YP.rotationDegrees(bedOrientation != null ? RenderUtil.getDirectionAngle(bedOrientation) : rotationYaw));
                poseStack.mulPose(Axis.ZP.rotationDegrees(getDeathMaxRotation(ensureIAnimatableJS(animatable))));
                poseStack.mulPose(Axis.YP.rotationDegrees(270f));
            } else if (LivingEntityRenderer.isEntityUpsideDown(livingEntity)) {
                poseStack.translate(0, (animatable.getBbHeight() + 0.1f) / nativeScale, 0);
                poseStack.mulPose(Axis.ZP.rotationDegrees(180f));
            }
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

        return (T) new WrappedAnimatableEntity(entity, builder);
    }
}
