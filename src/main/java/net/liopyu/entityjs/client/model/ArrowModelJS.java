package net.liopyu.entityjs.client.model;

import net.liopyu.entityjs.builders.ArrowEntityBuilder;
import net.liopyu.entityjs.builders.BaseEntityBuilder;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;

public class ArrowModelJS<T extends AbstractArrow> extends ArrowRenderer<T> {
    private final ArrowEntityBuilder<T> builder;

    public ArrowModelJS(ArrowEntityBuilder<T> builder, EntityRendererProvider.Context context) {
        super(context);
        this.builder = builder;
    }


    @Override
    public ResourceLocation getTextureLocation(T p_114482_) {
        return (ResourceLocation) builder.getTextureLocation;
    }
}
