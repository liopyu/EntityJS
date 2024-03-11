package net.liopyu.entityjs.client.nonliving.model;

import net.liopyu.entityjs.builders.nonliving.ArrowEntityBuilder;
import net.liopyu.entityjs.entities.IArrowEntityJS;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.AbstractArrow;

public class ArrowModelJS<T extends AbstractArrow & IArrowEntityJS> extends ArrowRenderer<T> {
    private final ArrowEntityBuilder<T> builder;


    public ArrowModelJS(EntityRendererProvider.Context p_173917_, ArrowEntityBuilder<T> builder) {
        super(p_173917_);
        this.builder = builder;
    }


    @Override
    public ResourceLocation getTextureLocation(T p_114482_) {
        return (ResourceLocation) builder.textureLocation;
    }
}
