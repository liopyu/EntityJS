package net.liopyu.entityjs.client.living.model;

import net.liopyu.entityjs.builders.living.BaseLivingEntityBuilder;
import net.liopyu.entityjs.builders.misc.CustomEntityBuilder;
import net.liopyu.entityjs.builders.misc.CustomEntityJSBuilder;
import net.liopyu.entityjs.entities.living.entityjs.IAnimatableJS;
import net.liopyu.entityjs.entities.living.entityjs.IAnimatableJSCustom;
import net.liopyu.entityjs.entities.living.entityjs.WrappedAnimatableEntity;
import net.liopyu.entityjs.util.implementation.ILivingEntityJS;
import com.geckolib.model.GeoModel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

/**
 * The default implementation of GeckoLib's {@link GeoModel} which delegates model, texture,
 * and animation handling to {@code Function<T, Identifier>}s in the entity type's builder
 */
public class CustomEntityModelJS<T extends LivingEntity & IAnimatableJSCustom> extends GeoModel<T> {

    private final CustomEntityJSBuilder builder;

    public CustomEntityModelJS(CustomEntityJSBuilder builder) {
        this.builder = builder;
    }

    public Identifier getModelResource(T animatable) {
        var a = ensureIAnimatableJS(animatable);
        return (Identifier) builder.modelResource.apply((WrappedAnimatableEntity) a);
    }

    public Identifier getTextureResource(T animatable) {
        var a = ensureIAnimatableJS(animatable);
        return (Identifier) builder.textureResource.apply((WrappedAnimatableEntity) a);
    }

    @Override
    public Identifier getAnimationResource(T animatable) {
        var a = ensureIAnimatableJS(animatable);
        return (Identifier) builder.animationResource.apply((WrappedAnimatableEntity) a);
    }

    /**
     * Ensures that the given entity is always an instance of IAnimatableJS.
     */
    private T ensureIAnimatableJS(LivingEntity entity) {
        if (entity instanceof IAnimatableJSCustom animatableJS) {
            return (T) animatableJS;
        } else if (entity instanceof ILivingEntityJS iLivingEntityJS) {
            return (T) iLivingEntityJS.entityJs$getAnimatableEntity();
        }

        // Wrap the entity in our custom subclass that implements IAnimatableJS
        return (T) new WrappedAnimatableEntity(entity, builder);
    }
}