package net.liopyu.entityjs.client.living;

import net.minecraft.client.renderer.rendertype.RenderTypes;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.liopyu.entityjs.builders.living.BaseLivingEntityBuilder;
import net.liopyu.entityjs.client.living.model.*;
import net.liopyu.entityjs.entities.living.entityjs.IAnimatableJS;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import com.geckolib.animatable.GeoItem;
import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import com.geckolib.renderer.layer.builtin.BlockAndItemGeoLayer;
import com.geckolib.renderer.layer.builtin.ItemArmorGeoLayer;
import com.geckolib.util.RenderUtil;

import javax.annotation.Nullable;

/**
 * The default implementation of GeckoLib's {@link GeoEntityRenderer} which delegates to the entity
 * type builder and {@link EntityModelJS} where it makes sense
 */
public class KubeJSEntityRenderer<T extends LivingEntity & IAnimatableJS> extends GeoEntityRenderer<T> {

    private final BaseLivingEntityBuilder<T> builder;

    public KubeJSEntityRenderer(EntityRendererProvider.Context renderManager, BaseLivingEntityBuilder<T> builder) {
        super(renderManager, new EntityModelJS<>(builder));
        this.builder = builder;
        this.scaleHeight = getScaleHeight();
        this.scaleWidth = getScaleWidth();
        for (GeoLayerJSBuilder<T> geoBuilder : builder.layerList) {
            GeoLayerJS<T> layerPart = geoBuilder.build(this, builder);
            addRenderLayer(layerPart);
        }
        for (GeoLayerJSBuilder<T> geoBuilder : builder.glowingLayerList) {
            GlowingGeoLayerJS<T> layerPart = geoBuilder.buildGlowing(this, builder);
            addRenderLayer(layerPart);
        }
        if (builder.itemArmorJSBuilder != null) {
            var b = new ItemArmorJSBuilder<>(animatable);
            builder.itemArmorJSBuilder.accept(b);
            ItemArmorGeoLayer<T> armorLayer = new ItemArmorGeoLayer<>(this) {

                @Override
                protected <I extends Item & GeoItem> void renderVanillaArmorPiece(
                        PoseStack poseStack, T animatable, GeoBone bone, EquipmentSlot slot,
                        ItemStack armorStack, ModelPart modelPart,
                        MultiBufferSource bufferSource, float partialTick,
                        int packedLight, int packedOverlay) {

                    if (b.vanillaArmorRenderConsumer != null) {
                        ContextUtils.VanillaArmorRenderContext<T> context = new ContextUtils.VanillaArmorRenderContext<>(
                                this, poseStack, animatable, bone, slot, armorStack,
                                modelPart, bufferSource, partialTick, packedLight, packedOverlay);
                        b.vanillaArmorRenderConsumer.accept(context);
                    }

                    super.renderVanillaArmorPiece(poseStack, animatable, bone, slot,
                            armorStack, modelPart, bufferSource, partialTick, packedLight, packedOverlay);
                }

                @Override
                public void renderForBone(PoseStack poseStack, T animatable, GeoBone bone, RenderType renderType,
                                          MultiBufferSource bufferSource, VertexConsumer buffer,
                                          float partialTick, int packedLight, int packedOverlay) {
                    if (b.renderBone != null) {
                        final ContextUtils.RenderBoneContext<T> context =
                                new ContextUtils.RenderBoneContext<>(this, poseStack, animatable, bone, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
                        EntityJSHelperClass.consumerCallback(b.renderBone, context,
                                "[EntityJS]: Error in " + entityName() + " builder for field: renderBone.");
                    }

                    super.renderForBone(poseStack, animatable, bone, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
                }

                @Override
                protected ModelPart getModelPartForBone(GeoBone bone, EquipmentSlot slot,
                                                        ItemStack stack, T animatable,
                                                        HumanoidModel<?> baseModel) {
                    String boneName = bone.getName();

                    return switch (slot) {
                        case HEAD -> baseModel.head;
                        case CHEST -> {
                            if (boneName.contains("left_shoulder")) yield baseModel.leftArm;
                            if (boneName.contains("right_shoulder")) yield baseModel.rightArm;
                            yield baseModel.body;
                        }
                        case MAINHAND -> baseModel.rightArm;
                        case OFFHAND -> baseModel.leftArm;
                        case LEGS -> boneName.contains("left_leg") ? baseModel.leftLeg : baseModel.rightLeg;
                        case FEET -> boneName.contains("left_foot") ? baseModel.leftLeg : baseModel.rightLeg;
                        default -> baseModel.body;
                    };
                }

                @Override
                protected ItemStack getArmorItemForBone(GeoBone bone, T animatable) {
                    try {
                        if (!b.armorBoneToSlotMap.isEmpty()) {
                            EquipmentSlot slot = b.armorBoneToSlotMap.get(bone.getName());
                            if (slot != null) {
                                switch (slot) {
                                    case MAINHAND -> {
                                        return animatable.getMainHandItem();
                                    }
                                    case OFFHAND -> {
                                        return animatable.getOffhandItem();
                                    }
                                    case HEAD, CHEST, LEGS, FEET -> {
                                        return animatable.getItemBySlot(slot);
                                    }
                                }
                            }
                        }
                    } catch (RuntimeException e) {
                        EntityJSHelperClass.logErrorMessageOnceCatchable("[Entityjs]: ", e);
                    }

                    return super.getArmorItemForBone(bone, animatable);
                }


            };
            this.addRenderLayer(armorLayer);
        }


        if (builder.addRenderItemLayer != null && builder.itemModelJSBuilder != null) {
            this.addRenderLayer(new BlockAndItemGeoLayer<>(this,
                    (bone, entity) -> {
                        if (builder.addRenderItemLayer.apply(entity).equals(bone.getName())) {
                            return entity.getMainHandItem();
                        }
                        return null;
                    },
                    (bone, entity) -> null
            ) {
                @Override
                protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack, T animatable,
                                                  MultiBufferSource bufferSource, float partialTick,
                                                  int packedLight, int packedOverlay) {
                    if (builder.itemModelJSBuilder.renderItem != null) {
                        final ContextUtils.ItemBoneRenderContext<T> context =
                                new ContextUtils.ItemBoneRenderContext<>(this, poseStack, bone, stack, animatable, bufferSource, partialTick, packedLight, packedOverlay);
                        EntityJSHelperClass.consumerCallback(builder.itemModelJSBuilder.renderItem, context,
                                "[EntityJS]: Error in " + entityName() + " builder for field: renderItem.");
                    }
                    super.renderStackForBone(poseStack, bone, stack, animatable, bufferSource, partialTick, packedLight, packedOverlay);
                }
            });
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
            final ContextUtils.ScaleModelRenderContext context = new ContextUtils.ScaleModelRenderContext(this, widthScale, heightScale, poseStack, animatable, model, isReRender, partialTick, packedLight, packedOverlay);
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
        try {
            if (builder.renderTypeFunction != null) {
                return builder.renderTypeFunction.apply(animatable);
            }
        } catch (Exception e) {
            EntityJSHelperClass.logErrorMessageOnceCatchable("[EntityJS]: Error in " + entityName() + "builder for field: renderTypeFunction.", e);
        }
        return switch (animatable.getBuilder().renderType) {
            case SOLID -> RenderTypes.entitySolid(texture);
            case CUTOUT -> RenderTypes.entityCutout(texture);
            case TRANSLUCENT -> RenderTypes.entityTranslucent(texture);

        };
    }

    @Override
    public void renderFinal(PoseStack poseStack, T animatable, BakedGeoModel model, MultiBufferSource bufferSource, @org.jetbrains.annotations.Nullable VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay, int colour) {
        if (builder.renderFinal != null && this.animatable != null) {
            final ContextUtils.FinalRenderContext<T> context = new ContextUtils.FinalRenderContext<>(this, poseStack, animatable, model, bufferSource, buffer, partialTick, packedLight, packedOverlay, colour);
            EntityJSHelperClass.consumerCallback(builder.renderFinal, context, "[EntityJS]: Error in " + entityName() + "builder for field: renderFinal.");
            super.renderFinal(poseStack, animatable, model, bufferSource, buffer, partialTick, packedLight, packedOverlay, colour);
        }
        super.renderFinal(poseStack, animatable, model, bufferSource, buffer, partialTick, packedLight, packedOverlay, colour);
    }

    @Override
    public void render(T animatable, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {

        if (builder.render != null && this.animatable != null) {
            final ContextUtils.RenderContext<T> context = new ContextUtils.RenderContext<>(this, animatable, entityYaw, partialTick, poseStack, bufferSource, packedLight);
            EntityJSHelperClass.consumerCallback(builder.render, context, "[EntityJS]: Error in " + entityName() + "builder for field: render.");
            super.render(animatable, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        } else {
            super.render(animatable, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        }
    }

    @Override
    protected void applyRotations(T animatable, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick, float nativeScale) {
        if (isShaking(animatable))
            rotationYaw += (float) (Math.cos(animatable.tickCount * 3.25d) * Math.PI * 0.4d);

        if (!animatable.hasPose(Pose.SLEEPING))
            poseStack.mulPose(Axis.YP.rotationDegrees(180f - rotationYaw));

        if (animatable instanceof LivingEntity livingEntity) {
            if (livingEntity.deathTime > 0 && builder.defaultDeathPose) {
                float deathRotation = (livingEntity.deathTime + partialTick - 1f) / 20f * 1.6f;

                poseStack.mulPose(Axis.ZP.rotationDegrees(Math.min(Mth.sqrt(deathRotation), 1) * getDeathMaxRotation(animatable)));
            } else if (livingEntity.isAutoSpinAttack()) {
                poseStack.mulPose(Axis.XP.rotationDegrees(-90f - livingEntity.getXRot()));
                poseStack.mulPose(Axis.YP.rotationDegrees((livingEntity.tickCount + partialTick) * -75f));
            } else if (animatable.hasPose(Pose.SLEEPING)) {
                Direction bedOrientation = livingEntity.getBedOrientation();

                poseStack.mulPose(Axis.YP.rotationDegrees(bedOrientation != null ? RenderUtil.getDirectionAngle(bedOrientation) : rotationYaw));
                poseStack.mulPose(Axis.ZP.rotationDegrees(getDeathMaxRotation(animatable)));
                poseStack.mulPose(Axis.YP.rotationDegrees(270f));
            } else if (LivingEntityRenderer.isEntityUpsideDown(livingEntity)) {
                poseStack.translate(0, (animatable.getBbHeight() + 0.1f) / nativeScale, 0);
                poseStack.mulPose(Axis.ZP.rotationDegrees(180f));
            }
        }
        if (builder.applyRotations != null) {
            ContextUtils.ApplyRotationsContext<T> context = new ContextUtils.ApplyRotationsContext<>(this, animatable, poseStack, ageInTicks, rotationYaw, partialTick);
            EntityJSHelperClass.consumerCallback(builder.applyRotations, context, "[EntityJS]: Error in " + entityName() + " builder for field: applyRotations.");
        }
    }

}

