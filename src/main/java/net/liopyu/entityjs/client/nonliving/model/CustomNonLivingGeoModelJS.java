package net.liopyu.entityjs.client.nonliving.model;

import net.liopyu.entityjs.builders.misc.CustomEntityJSBuilder;
import net.liopyu.entityjs.entities.living.entityjs.IAnimatableJSCustom;
import net.liopyu.entityjs.entities.nonliving.entityjs.WrappedNonLivingAnimatableEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import software.bernie.geckolib.model.GeoModel;

public class CustomNonLivingGeoModelJS<T extends Entity & IAnimatableJSCustom> extends GeoModel<T> {
    private final CustomEntityJSBuilder builder;

    public CustomNonLivingGeoModelJS(CustomEntityJSBuilder builder) {
        this.builder = builder;
    }

    @Override
    public ResourceLocation getModelResource(T animatable) {
        return (ResourceLocation) builder.modelResource.apply(unwrap(animatable));
    }

    @Override
    public ResourceLocation getTextureResource(T animatable) {
        return (ResourceLocation) builder.textureResource.apply(unwrap(animatable));
    }

    @Override
    public ResourceLocation getAnimationResource(T animatable) {
        return (ResourceLocation) builder.animationResource.apply(unwrap(animatable));
    }

    private Entity unwrap(T entity) {
        if (entity instanceof WrappedNonLivingAnimatableEntity wrappedEntity) {
            return wrappedEntity.getOriginalEntity();
        }
        return entity;
    }
}
