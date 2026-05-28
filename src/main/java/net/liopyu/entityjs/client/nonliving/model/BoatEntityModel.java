package net.liopyu.entityjs.client.nonliving.model;

import net.liopyu.entityjs.builders.nonliving.vanilla.BoatEntityBuilder;
import net.liopyu.entityjs.entities.nonliving.entityjs.IAnimatableJSNL;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.vehicle.boat.Boat;
import com.geckolib.model.GeoModel;

public class BoatEntityModel<T extends Boat & IAnimatableJSNL> extends GeoModel<T> {
    private final BoatEntityBuilder<T> builder;

    public BoatEntityModel(BoatEntityBuilder<?> builder) {
        this.builder = (BoatEntityBuilder<T>) builder;
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
