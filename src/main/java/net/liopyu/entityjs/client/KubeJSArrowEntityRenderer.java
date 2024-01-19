package net.liopyu.entityjs.client;


import net.liopyu.entityjs.builders.ProjectileEntityBuilder;
import net.liopyu.entityjs.entities.IArrowEntityJS;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.AbstractArrow;


public class KubeJSArrowEntityRenderer<T extends AbstractArrow & IArrowEntityJS> extends ArrowRenderer<T> {

    private final ProjectileEntityBuilder<T> builder;

    public KubeJSArrowEntityRenderer(EntityRendererProvider.Context renderManager, ProjectileEntityBuilder<?> builder) {
        super(renderManager);
        this.builder = (ProjectileEntityBuilder<T>) builder;
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return builder.getTextureLocation.apply(entity);
    }
}

