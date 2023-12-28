package net.liopyu.entityjs.client.model;

import net.liopyu.entityjs.builders.BaseEntityBuilder;
import net.liopyu.entityjs.entities.IAnimatableJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class EntityModelJS<T extends Entity & IAnimatableJS> extends AnimatedGeoModel<T> {

    private final BaseEntityBuilder<T> builder;

    public EntityModelJS(BaseEntityBuilder<T> builder) {
        this.builder = builder;
    }

    @Override
    public ResourceLocation getModelResource(T object) {
        return builder.modelResource.apply(object);
    }

    @Override
    public ResourceLocation getTextureResource(T object) {
        return builder.textureResource.apply(object);
    }

    @Override
    public ResourceLocation getAnimationResource(T animatable) {
        return builder.animationResource.apply(animatable);
    }
}
