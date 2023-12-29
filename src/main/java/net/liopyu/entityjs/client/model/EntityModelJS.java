package net.liopyu.entityjs.client.model;

import net.liopyu.entityjs.builders.BaseEntityBuilder;
import net.liopyu.entityjs.entities.IAnimatableJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import software.bernie.geckolib3.model.AnimatedGeoModel;

/**
 * The default implementation of GeckoLib's {@link AnimatedGeoModel} which delegates model, texture,
 * and animation handling to {@code Function<T, ResourceLocation>}s in the entity type's builder
 */
public class EntityModelJS<T extends LivingEntity & IAnimatableJS> extends AnimatedGeoModel<T> {

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
