package net.liopyu.entityjs.client;


import net.liopyu.entityjs.builders.ArrowEntityBuilder;
import net.liopyu.entityjs.builders.BaseEntityBuilder;
import net.liopyu.entityjs.builders.BaseProjectileBuilder;
import net.liopyu.entityjs.builders.ProjectileEntityBuilder;
import net.liopyu.entityjs.client.model.ArrowModelJS;
import net.liopyu.entityjs.entities.IArrowEntityJS;
import net.liopyu.entityjs.entities.IProjectileEntityJS;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.Projectile;


public class KubeJSProjectileEntityRenderer<T extends AbstractHurtingProjectile & IProjectileEntityJS> extends EntityRenderer<T> {

    private final ProjectileEntityBuilder<T> builder;

    public KubeJSProjectileEntityRenderer(EntityRendererProvider.Context renderManager, ProjectileEntityBuilder<T> builder) {
        super(renderManager);
        this.builder = builder;
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return builder.getTextureLocation.apply(entity);
    }
}

