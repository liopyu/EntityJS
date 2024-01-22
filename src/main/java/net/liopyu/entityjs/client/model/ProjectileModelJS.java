package net.liopyu.entityjs.client.model;

import net.liopyu.entityjs.builders.ArrowEntityBuilder;
import net.liopyu.entityjs.builders.ProjectileEntityBuilder;
import net.liopyu.entityjs.entities.IAnimatableJS;
import net.liopyu.entityjs.entities.IArrowEntityJS;
import net.liopyu.entityjs.entities.IProjectileEntityJS;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.*;

public class ProjectileModelJS<T extends ThrowableItemProjectile & IProjectileEntityJS> extends EntityRenderer<T> {
    private final ProjectileEntityBuilder<T> builder;


    public ProjectileModelJS(EntityRendererProvider.Context p_173917_, ProjectileEntityBuilder<T> builder) {
        super(p_173917_);
        this.builder = builder;
    }


    @Override
    public ResourceLocation getTextureLocation(T p_114482_) {
        return (ResourceLocation) builder.getTextureLocation;
    }
}
