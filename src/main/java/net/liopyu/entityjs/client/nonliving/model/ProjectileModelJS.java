package net.liopyu.entityjs.client.nonliving.model;

import net.liopyu.entityjs.builders.nonliving.entityjs.ProjectileEntityBuilder;
import net.liopyu.entityjs.entities.nonliving.entityjs.IAnimatableJSNL;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;

public class ProjectileModelJS<T extends ThrowableItemProjectile & IAnimatableJSNL> extends EntityRenderer<T> {
    private final ProjectileEntityBuilder<T> builder;


    public ProjectileModelJS(EntityRendererProvider.Context p_173917_, ProjectileEntityBuilder<T> builder) {
        super(p_173917_);
        this.builder = builder;
    }


    @Override
    public ResourceLocation getTextureLocation(T p_114482_) {
        return (ResourceLocation) builder.textureLocation;
    }
}
