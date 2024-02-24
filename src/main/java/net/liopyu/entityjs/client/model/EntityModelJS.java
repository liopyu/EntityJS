package net.liopyu.entityjs.client.model;

import net.liopyu.entityjs.builders.BaseLivingEntityBuilder;
import net.liopyu.entityjs.entities.IAnimatableJS;
import software.bernie.geckolib.model.GeoModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

/**
 * The default implementation of GeckoLib's {@link GeoModel} which delegates model, texture,
 * and animation handling to {@code Function<T, ResourceLocation>}s in the entity type's builder
 */
public class EntityModelJS<T extends LivingEntity & IAnimatableJS> extends GeoModel<T> {

    private final BaseLivingEntityBuilder<T> builder;

    public EntityModelJS(BaseLivingEntityBuilder<T> builder) {
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
