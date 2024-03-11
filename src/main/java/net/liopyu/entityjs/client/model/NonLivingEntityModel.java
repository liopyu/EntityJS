package net.liopyu.entityjs.client.model;

import net.liopyu.entityjs.builders.BaseEntityBuilder;
import net.liopyu.entityjs.builders.BaseLivingEntityBuilder;
import net.liopyu.entityjs.entities.IAnimatableJS;
import net.liopyu.entityjs.entities.IAnimatableJSNL;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import software.bernie.geckolib.model.GeoModel;

public class NonLivingEntityModel<T extends Entity & IAnimatableJSNL> extends GeoModel<T> {
    private final BaseEntityBuilder<T> builder;

    public NonLivingEntityModel(BaseEntityBuilder<T> builder) {
        this.builder = builder;
    }

    @Override
    public ResourceLocation getModelResource(T object) {
        return (ResourceLocation) builder.modelResource.apply(object);
    }

    @Override
    public ResourceLocation getTextureResource(T object) {
        return (ResourceLocation) builder.textureResource.apply(object);
    }

    @Override
    public ResourceLocation getAnimationResource(T animatable) {
        return (ResourceLocation) builder.animationResource.apply(animatable);
    }
}
