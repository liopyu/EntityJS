package net.liopyu.entityjs.client.nonliving;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.liopyu.entityjs.builders.misc.CustomEntityBuilder;
import net.liopyu.entityjs.entities.nonliving.entityjs.WrappedNonLivingEntity;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;
import java.util.WeakHashMap;

public class CustomEntityModelRenderer<T extends Entity> extends EntityRenderer<T> {
    private final CustomEntityBuilder builder;
    private final EntityModel<Entity> model;
    private final Map<Entity, WrappedNonLivingEntity> wrapperCache = new WeakHashMap<>();

    public CustomEntityModelRenderer(EntityRendererProvider.Context context, CustomEntityBuilder builder) {
        super(context);
        this.builder = builder;
        this.model = builder.createEntityModel(context);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return (ResourceLocation) builder.textureResource.apply(unwrap(entity));
    }

    @Override
    public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        Entity renderEntity = wrap(entity);
        ResourceLocation texture = getTextureLocation(entity);
        RenderType renderType = getRenderType(renderEntity, texture);
        VertexConsumer buffer = bufferSource.getBuffer(renderType);
        int packedOverlay = OverlayTexture.NO_OVERLAY;

        poseStack.pushPose();
        if (renderEntity instanceof LivingEntity livingEntity) {
            boolean shouldSit = livingEntity.isPassenger() && livingEntity.getVehicle() != null && livingEntity.getVehicle().shouldRiderSit();
            model.attackTime = livingEntity.getAttackAnim(partialTick);
            model.riding = shouldSit;
            model.young = livingEntity.isBaby();

            float bodyYaw = Mth.rotLerp(partialTick, livingEntity.yBodyRotO, livingEntity.yBodyRot);
            float headYaw = Mth.rotLerp(partialTick, livingEntity.yHeadRotO, livingEntity.yHeadRot);
            float netHeadYaw = headYaw - bodyYaw;
            float headPitch = Mth.lerp(partialTick, livingEntity.xRotO, livingEntity.getXRot());
            float ageInTicks = livingEntity.tickCount + partialTick;

            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - bodyYaw));
            poseStack.scale(-1.0F, -1.0F, 1.0F);
            poseStack.translate(0.0F, -1.501F, 0.0F);

            float limbSwing = 0.0F;
            float limbSwingAmount = 0.0F;
            if (!shouldSit && livingEntity.isAlive()) {
                limbSwingAmount = livingEntity.walkAnimation.speed(partialTick);
                limbSwing = livingEntity.walkAnimation.position(partialTick);
                if (livingEntity.isBaby()) {
                    limbSwing *= 3.0F;
                }
                if (limbSwingAmount > 1.0F) {
                    limbSwingAmount = 1.0F;
                }
            }

            model.prepareMobModel(livingEntity, limbSwing, limbSwingAmount, partialTick);
            model.setupAnim(livingEntity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            packedOverlay = LivingEntityRenderer.getOverlayCoords(livingEntity, 0.0F);
        } else {
            float ageInTicks = renderEntity.tickCount + partialTick;
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));
            poseStack.scale(-1.0F, -1.0F, 1.0F);
            poseStack.translate(0.0F, -1.501F, 0.0F);
            model.prepareMobModel(renderEntity, 0.0F, 0.0F, partialTick);
            model.setupAnim(renderEntity, 0.0F, 0.0F, ageInTicks, 0.0F, renderEntity.getXRot());
        }

        model.renderToBuffer(poseStack, buffer, packedLight, packedOverlay);
        if (builder.render != null) {
            final ContextUtils.NLRenderContext<Entity> context = new ContextUtils.NLRenderContext<>(unwrap(renderEntity), entityYaw, partialTick, poseStack, bufferSource, packedLight);
            EntityJSHelperClass.consumerCallback(builder.render, context, "[EntityJS]: Error in " + entity.getType() + "builder for field: render.");
        }
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private RenderType getRenderType(Entity entity, ResourceLocation texture) {
        try {
            if (builder.renderTypeFunction != null) {
                return builder.renderTypeFunction.apply(entity);
            }
        } catch (Exception e) {
            EntityJSHelperClass.logErrorMessageOnceCatchable("[EntityJS]: Error in custom EntityModel renderTypeFunction.", e);
        }
        return switch (builder.renderType) {
            case SOLID -> RenderType.entitySolid(texture);
            case CUTOUT -> RenderType.entityCutout(texture);
            case TRANSLUCENT -> RenderType.entityTranslucent(texture);
        };
    }

    private Entity wrap(Entity entity) {
        if (entity instanceof LivingEntity) {
            return entity;
        }
        if (entity instanceof WrappedNonLivingEntity) {
            return entity;
        }
        return wrapperCache.computeIfAbsent(entity, key -> new WrappedNonLivingEntity(key, builder)).syncFromOriginal();
    }

    private Entity unwrap(Entity entity) {
        if (entity instanceof WrappedNonLivingEntity wrappedEntity) {
            return wrappedEntity.getOriginalEntity();
        }
        return entity;
    }
}
