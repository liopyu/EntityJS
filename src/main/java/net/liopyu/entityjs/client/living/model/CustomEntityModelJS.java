package net.liopyu.entityjs.client.living.model;

import net.liopyu.entityjs.builders.living.BaseLivingEntityBuilder;
import net.liopyu.entityjs.builders.misc.CustomEntityBuilder;
import net.liopyu.entityjs.builders.misc.CustomEntityJSBuilder;
import net.liopyu.entityjs.entities.living.entityjs.IAnimatableJS;
import net.liopyu.entityjs.entities.living.entityjs.IAnimatableJSCustom;
import software.bernie.geckolib.model.GeoModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

/**
 * The default implementation of GeckoLib's {@link GeoModel} which delegates model, texture,
 * and animation handling to {@code Function<T, ResourceLocation>}s in the entity type's builder
 */
public class CustomEntityModelJS<T extends LivingEntity & IAnimatableJSCustom> extends GeoModel<T> {

    private final CustomEntityJSBuilder builder;

    public CustomEntityModelJS(CustomEntityJSBuilder builder) {
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