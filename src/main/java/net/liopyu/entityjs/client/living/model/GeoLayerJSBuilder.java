package net.liopyu.entityjs.client.living.model;

import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import net.liopyu.entityjs.builders.living.BaseLivingEntityBuilder;
import net.liopyu.entityjs.client.living.KubeJSEntityRenderer;
import net.liopyu.entityjs.entities.living.entityjs.IAnimatableJS;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.liopyu.liolib.core.animatable.GeoAnimatable;
import net.liopyu.liolib.renderer.GeoRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Consumer;
import java.util.function.Function;

public class GeoLayerJSBuilder<T extends LivingEntity & IAnimatableJS> {
    public transient Function<T, Object> textureResource;
    public BaseLivingEntityBuilder<T> builder;

    public transient Consumer<ContextUtils.PreRenderContext<T>> render;
    public transient Consumer<ContextUtils.PreRenderContext<T>> preRender;

    public GeoLayerJSBuilder(BaseLivingEntityBuilder<T> builder) {
        this.builder = builder;
    }

    public GeoLayerJS<T> build(KubeJSEntityRenderer<T> entityRendererIn, BaseLivingEntityBuilder<T> builder) {
        return new GeoLayerJS<>(entityRendererIn, this, builder);
    }

    public BaseLivingEntityBuilder<T> getBuilder() {
        return builder;
    }

    @Info(value = """
            Defines logic to preRender the newGeoLayer.
                        
            Example usage:
            ```javascript
            geoBuilder.preRender(context => {
                // Define logic to render the newGeoLayer
                if (context.entity.isBaby()) {
                    context.poseStack.scale(0.5, 0.5, 0.5);
                }
            });
            ```
            """)
    public GeoLayerJSBuilder<T> preRender(Consumer<ContextUtils.PreRenderContext<T>> preRender) {
        this.preRender = preRender;
        return this;
    }

    @Info(value = """
            Defines logic to render the newGeoLayer.
                        
            Example usage:
            ```javascript
            geoBuilder.render(context => {
                // Define logic to render the newGeoLayer
                if (context.entity.isBaby()) {
                    context.poseStack.scale(0.5, 0.5, 0.5);
                }
            });
            ```
            """)
    public GeoLayerJSBuilder<T> render(Consumer<ContextUtils.PreRenderContext<T>> render) {
        this.render = render;
        return this;
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
    public GeoLayerJSBuilder<T> textureResource(Function<T, Object> function) {
        textureResource = entity -> {
            Object obj = function.apply(entity);
            if (obj instanceof String && !obj.toString().equals("undefined")) {
                return new ResourceLocation((String) obj);
            } else if (obj instanceof ResourceLocation) {
                return (ResourceLocation) obj;
            } else {
                EntityJSHelperClass.logWarningMessageOnce("Invalid return value for textureResource in newGeoLayer builder: " + obj + ". Defaulting to " + entity.getBuilder().newID("textures/entity/", ".png"));
                return entity.getBuilder().newID("textures/entity/", ".png");
            }
        };
        return this;
    }
}

