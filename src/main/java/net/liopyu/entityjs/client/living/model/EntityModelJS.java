package net.liopyu.entityjs.client.living.model;

import net.liopyu.entityjs.builders.living.BaseLivingEntityBuilder;
import net.liopyu.entityjs.entities.living.entityjs.IAnimatableJS;
import com.geckolib.model.GeoModel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

/**
 * The default implementation of GeckoLib's {@link GeoModel} which delegates model, texture,
 * and animation handling to {@code Function<T, Identifier>}s in the entity type's builder
 */
public class EntityModelJS<T extends LivingEntity & IAnimatableJS> extends GeoModel<T> {

    private final BaseLivingEntityBuilder<T> builder;

    public EntityModelJS(BaseLivingEntityBuilder<T> builder) {
        this.builder = builder;
    }

    @Override
    public Identifier getModelResource(T object) {
        return (Identifier) builder.modelResource.apply(object);
    }

    @Override
    public Identifier getTextureResource(T object) {
        return (Identifier) builder.textureResource.apply(object);
    }

    @Override
    public Identifier getAnimationResource(T animatable) {
        return (Identifier) builder.animationResource.apply(animatable);
    }
}
