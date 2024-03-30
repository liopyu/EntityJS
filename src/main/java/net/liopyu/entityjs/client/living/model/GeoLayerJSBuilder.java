package net.liopyu.entityjs.client.living.model;

import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import net.liopyu.entityjs.builders.living.BaseLivingEntityBuilder;
import net.liopyu.entityjs.client.living.KubeJSEntityRenderer;
import net.liopyu.entityjs.entities.living.entityjs.IAnimatableJS;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.liopyu.liolib.core.animatable.GeoAnimatable;
import net.liopyu.liolib.renderer.GeoRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Function;

public class GeoLayerJSBuilder<T extends LivingEntity & IAnimatableJS> {
    public transient ResourceLocation texture;
    public BaseLivingEntityBuilder<T> builder;

    public GeoLayerJSBuilder(BaseLivingEntityBuilder<T> builder) {
        this.builder = builder;
        //this.textureResource = t -> new ResourceLocation("kubejs:textures/entity/wyrm.png");
    }

    public GeoLayerJS<T> build(KubeJSEntityRenderer<T> entityRendererIn, BaseLivingEntityBuilder<T> builder) {
        return new GeoLayerJS<>(entityRendererIn, this, builder);
    }

    public BaseLivingEntityBuilder<T> getBuilder() {
        return builder;
    }

    @Info(value = """
            Sets a function to determine the texture resource for the entity.
            The provided Function accepts a parameter of type T (the entity),
            allowing changing the texture based on information about the entity.
            The default behavior returns <namespace>:textures/entity/<path>.png.
                        
            Example usage:
            ```javascript
            entityBuilder.textureResource(entity => {
                // Define logic to determine the texture resource for the entity
                // Use information about the entity provided by the context.
                return "kubejs:textures/entity/wyrm.png" // Some ResourceLocation representing the texture resource;
            });
            ```
            """)
    public GeoLayerJSBuilder<T> texture(ResourceLocation function) {
        texture = function;
        return this;
    }
}

