package net.liopyu.entityjs.client.nonliving.model;

import net.liopyu.entityjs.builders.nonliving.BaseEntityBuilder;
import net.liopyu.entityjs.entities.nonliving.entityjs.IAnimatableJSNL;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import com.geckolib.model.GeoModel;

public class NonLivingEntityModel<T extends Entity & IAnimatableJSNL> extends GeoModel<T> {
    private final BaseEntityBuilder<T> builder;

    public NonLivingEntityModel(BaseEntityBuilder<T> builder) {
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
