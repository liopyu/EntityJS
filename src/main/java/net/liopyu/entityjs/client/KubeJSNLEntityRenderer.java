package net.liopyu.entityjs.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.liopyu.entityjs.builders.BaseEntityBuilder;
import net.liopyu.entityjs.client.model.NonLivingEntityModel;
import net.liopyu.entityjs.entities.IAnimatableJSNL;
import net.liopyu.entityjs.util.ContextUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import javax.annotation.Nullable;


public class KubeJSNLEntityRenderer<T extends Entity & IAnimatableJSNL> extends GeoEntityRenderer<T> {

    private final BaseEntityBuilder<T> builder;

    public KubeJSNLEntityRenderer(EntityRendererProvider.Context renderManager, BaseEntityBuilder<T> builder) {
        super(renderManager, new NonLivingEntityModel<>(builder));
        this.builder = builder;
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
        if (builder.render != null) {
            final ContextUtils.NLRenderContext<T> context = new ContextUtils.NLRenderContext<>(animatable, entityYaw, partialTick, poseStack, bufferSource, packedLight);
            builder.render.accept(context);
            super.render(animatable, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        } else {
            super.render(animatable, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        }
    }
}

