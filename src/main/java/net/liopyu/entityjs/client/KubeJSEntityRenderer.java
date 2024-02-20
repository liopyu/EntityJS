package net.liopyu.entityjs.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.liopyu.entityjs.builders.BaseLivingEntityBuilder;
import net.liopyu.entityjs.client.model.EntityModelJS;
import net.liopyu.entityjs.entities.IAnimatableJS;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.liolib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

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
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return builder.textureResource.apply(entity);
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
}

