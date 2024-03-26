package net.liopyu.entityjs.client.living;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import net.liopyu.entityjs.builders.living.BaseLivingEntityBuilder;
import net.liopyu.entityjs.client.living.model.CustomGeoRenderLayer;
import net.liopyu.entityjs.client.living.model.EntityModelJS;
import net.liopyu.entityjs.entities.living.entityjs.IAnimatableJS;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.liolib.renderer.GeoEntityRenderer;
import net.liopyu.liolib.renderer.layer.GeoRenderLayer;
import net.liopyu.liolib.util.RenderUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;

import javax.annotation.Nullable;
import java.util.List;

/**
 * The default implementation of GeckoLib's {@link GeoEntityRenderer} which delegates to the entity
 * type builder and {@link EntityModelJS} where it makes sense
 */
public class KubeJSEntityRenderer<T extends LivingEntity & IAnimatableJS> extends GeoEntityRenderer<T> {

    private final BaseLivingEntityBuilder<T> builder;

    public KubeJSEntityRenderer(EntityRendererProvider.Context renderManager, BaseLivingEntityBuilder<T> builder) {
        super(renderManager, new EntityModelJS<>(builder));
        this.builder = builder;
        addRenderLayer(new CustomGeoRenderLayer<>(this, builder));
    }


    @Override
    public GeoEntityRenderer<T> addRenderLayer(GeoRenderLayer<T> renderLayer) {
        return super.addRenderLayer(renderLayer);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return (ResourceLocation) builder.textureResource.apply(entity);
    }

    @Override
    public RenderType getRenderType(T animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return switch (animatable.getBuilder().renderType) {
            case SOLID -> RenderType.entitySolid(texture);
            case CUTOUT -> RenderType.entityCutout(texture);
            case TRANSLUCENT -> RenderType.entityTranslucent(texture);

        };
    }

    @Override
    public void render(T animatable, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        if (animatable.hurtTime > 0) {
            ConsoleJS.STARTUP.info(getRenderLayers());
        }
        if (builder.render != null) {
            final ContextUtils.RenderContext<T> context = new ContextUtils.RenderContext<>(animatable, entityYaw, partialTick, poseStack, bufferSource, packedLight);
            builder.render.accept(context);
            super.render(animatable, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        } else {
            if (animatable.isBaby()) {
                poseStack.scale(0.5F, 0.5F, 0.5F);
            }
            super.render(animatable, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        }
    }

    @Override
    protected void applyRotations(T animatable, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick) {
        Pose pose = animatable.getPose();

        if (pose != Pose.SLEEPING) {
            poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - rotationYaw));
        }

        if (animatable.deathTime > 0 && builder.defaultDeathPose) {
            float deathRotation = ((float) animatable.deathTime + partialTick - 1.0F) / 20.0F * 1.6F;
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(Math.min(Mth.sqrt(deathRotation), 1.0F) * this.getDeathMaxRotation(animatable)));
        } else if (animatable.isAutoSpinAttack()) {
            poseStack.mulPose(Vector3f.XP.rotationDegrees(-90.0F - animatable.getXRot()));
            poseStack.mulPose(Vector3f.YP.rotationDegrees(((float) animatable.tickCount + partialTick) * -75.0F));
        } else if (pose == Pose.SLEEPING) {
            Direction bedOrientation = animatable.getBedOrientation();
            poseStack.mulPose(Vector3f.YP.rotationDegrees(bedOrientation != null ? RenderUtils.getDirectionAngle(bedOrientation) : rotationYaw));
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(this.getDeathMaxRotation(animatable)));
            poseStack.mulPose(Vector3f.YP.rotationDegrees(270.0F));
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
                poseStack.translate(0.0, (animatable.getBbHeight() + 0.1F), 0.0);
                poseStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
            }
            if (name != null && (name.equalsIgnoreCase("liopyu") || name.equalsIgnoreCase("toomuchmail"))) {
                poseStack.translate(0.0F, animatable.getBbHeight() + 0.1F, 0.0F);
                poseStack.mulPose(Vector3f.ZP.rotationDegrees(90.0F));
            }
        }

    }
}

