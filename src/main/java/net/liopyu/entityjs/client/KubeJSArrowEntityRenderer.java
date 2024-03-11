package net.liopyu.entityjs.client;


import net.liopyu.entityjs.builders.nonliving.ArrowEntityBuilder;
import net.liopyu.entityjs.entities.nonliving.entityjs.IAnimatableJSNL;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.AbstractArrow;


public class KubeJSArrowEntityRenderer<T extends AbstractArrow & IAnimatableJSNL> extends ArrowRenderer<T> {

    private final ArrowEntityBuilder<T> builder;

    public KubeJSArrowEntityRenderer(EntityRendererProvider.Context renderManager, ArrowEntityBuilder<T> builder) {
        super(renderManager);
        this.builder = builder;
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return (ResourceLocation) builder.textureLocation.apply(entity);
    }
}

