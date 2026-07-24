package net.liopyu.entityjs.client.living;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.liopyu.entityjs.builders.misc.CustomEntityJSBuilder;
import net.liopyu.entityjs.client.living.model.*;
import net.liopyu.entityjs.entities.living.entityjs.IAnimatableJSCustom;
import net.liopyu.entityjs.entities.living.entityjs.WrappedAnimatableEntity;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.common.util.EntityJSHelperClass;
import net.liopyu.entityjs.util.implementation.ILivingEntityJS;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import software.bernie.geckolib.util.RenderUtils;

import org.jetbrains.annotations.Nullable;

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
    public ResourceLocation getTextureLocation(T entity) {

        return (ResourceLocation) builder.textureResource.apply(entity);
    }

    @Override
    public RenderType getRenderType(T animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        try {
            if (builder.renderTypeFunction != null) {
                return EntityJSHelperClass.convertToRenderType(builder.renderTypeFunction.apply(ensureIAnimatableJS(animatable)), defaultRenderType(animatable, texture));
            }
        } catch (Exception e) {
            EntityJSHelperClass.logErrorMessageOnceCatchable("[EntityJS]: Error in " + entityName() + "builder for field: renderTypeFunction.", e);
        }
        return defaultRenderType(animatable, texture);
    }

    private RenderType defaultRenderType(T animatable, ResourceLocation texture) {
        return switch (ensureIAnimatableJS(animatable).getBuilder().renderType) {
            case SOLID -> RenderType.entitySolid(texture);
            case CUTOUT -> RenderType.entityCutout(texture);
            case TRANSLUCENT -> RenderType.entityTranslucent(texture);
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
    protected void applyRotations(T a, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick) {
        LivingEntity animatable = a;
        if (animatable instanceof WrappedAnimatableEntity wrappedAnimatableEntity) {
            animatable = wrappedAnimatableEntity.getOriginalEntity();
        }
        Pose pose = animatable.getPose();
        if (this.isShaking(ensureIAnimatableJS(animatable))) {
            rotationYaw += (float) (Math.cos((double) animatable.tickCount * 3.25) * Math.PI * 0.4);
        }
        if (pose != Pose.SLEEPING) {
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - rotationYaw));
        }
        if (animatable.deathTime > 0 && builder.defaultDeathPose) {
            float deathRotation = ((float) animatable.deathTime + partialTick - 1.0F) / 20.0F * 1.6F;
            poseStack.mulPose(Axis.ZP.rotationDegrees(Math.min(Mth.sqrt(deathRotation), 1.0F) * this.getDeathMaxRotation(ensureIAnimatableJS(animatable))));
        } else if (animatable.isAutoSpinAttack()) {
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F - animatable.getXRot()));
            poseStack.mulPose(Axis.YP.rotationDegrees(((float) animatable.tickCount + partialTick) * -75.0F));
        } else if (pose == Pose.SLEEPING) {
            Direction bedOrientation = animatable.getBedOrientation();
            poseStack.mulPose(Axis.YP.rotationDegrees(bedOrientation != null ? RenderUtils.getDirectionAngle(bedOrientation) : rotationYaw));
            poseStack.mulPose(Axis.ZP.rotationDegrees(this.getDeathMaxRotation(ensureIAnimatableJS(animatable))));
            poseStack.mulPose(Axis.YP.rotationDegrees(270.0F));
        } else if (animatable.hasCustomName() || animatable instanceof Player) {
            String name = animatable.getName().getString();
            if (animatable instanceof Player player) {
                if (!player.isModelPartShown(PlayerModelPart.CAPE)) {
                    return;
                }
            } else {
                name = ChatFormatting.stripFormatting(name);
            }

            if (name != null && (name.equals("Dinnerbone") || name.equalsIgnoreCase("Grumm"))) {
                poseStack.translate(0.0F, animatable.getBbHeight() + 0.1F, 0.0F);
                poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
            }
            if (name != null && (name.equalsIgnoreCase("liopyu") || name.equalsIgnoreCase("toomuchmail"))) {
                poseStack.translate(0.0F, animatable.getBbHeight() + 0.1F, 0.0F);
                poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
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

        // Wrap the entity in our custom subclass that implements IAnimatableJS
        return (T) new WrappedAnimatableEntity(entity, builder);
    }
}

