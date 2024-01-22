package net.liopyu.entityjs.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.liopyu.entityjs.builders.BaseLivingEntityBuilder;
import net.liopyu.entityjs.client.model.EntityModelJS;
import net.liopyu.entityjs.entities.IAnimatableJS;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

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
    public RenderType getRenderType(T animatable, float partialTick, PoseStack poseStack, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, int packedLight, ResourceLocation texture) {
        return switch (animatable.getBuilder().renderType) {
            case SOLID -> RenderType.entitySolid(texture);
            case CUTOUT -> RenderType.entityCutout(texture);
            case TRANSLUCENT -> RenderType.entityTranslucent(texture);
        };
    }
}

