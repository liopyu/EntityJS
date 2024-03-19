package net.liopyu.entityjs.client.nonliving.model;

import net.liopyu.entityjs.builders.nonliving.vanilla.BoatEntityBuilder;
import net.liopyu.entityjs.entities.nonliving.entityjs.IAnimatableJSNL;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.vehicle.Boat;
import software.bernie.geckolib.model.GeoModel;

public class BoatEntityModel<T extends Boat & IAnimatableJSNL> extends GeoModel<T> {
    private final BoatEntityBuilder<T> builder;

    public BoatEntityModel(BoatEntityBuilder<?> builder) {
        this.builder = (BoatEntityBuilder<T>) builder;
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
