package net.liopyu.entityjs.client.living.model;

import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.builders.misc.CustomEntityJSBuilder;
import net.liopyu.entityjs.client.living.CustomKubeJSEntityRenderer;
import net.liopyu.entityjs.client.living.KubeJSEntityRenderer;
import net.liopyu.entityjs.entities.living.entityjs.IAnimatableJS;
import net.liopyu.entityjs.entities.living.entityjs.IAnimatableJSCustom;
import net.liopyu.entityjs.entities.living.entityjs.WrappedAnimatableEntity;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.liopyu.entityjs.util.implementation.ILivingEntityJS;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Consumer;
import java.util.function.Function;

public class CustomGeoLayerJSBuilder<T extends LivingEntity & IAnimatableJSCustom> {
    public transient Function<T, Object> textureResource;
    public CustomEntityJSBuilder builder;

    public transient Consumer<ContextUtils.PreRenderContext<T>> render;
    public transient Consumer<ContextUtils.PreRenderContext<T>> preRender;

    public CustomGeoLayerJSBuilder(CustomEntityJSBuilder builder) {
        this.builder = builder;
    }

    public CustomGeoLayerJS<T> build(CustomKubeJSEntityRenderer<T> entityRendererIn, CustomEntityJSBuilder builder) {
        return new CustomGeoLayerJS<>(entityRendererIn, this, builder);
    }

    public CustomGlowingGeoLayerJS<T> buildGlowing(CustomKubeJSEntityRenderer<T> entityRendererIn, CustomEntityJSBuilder builder) {
        return new CustomGlowingGeoLayerJS<>(entityRendererIn, this, builder);
    }

    public CustomEntityJSBuilder getBuilder() {
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
    public CustomGeoLayerJSBuilder<T> preRender(Consumer<ContextUtils.PreRenderContext<T>> preRender) {
        this.preRender = preRender;
        return this;
    }

    @Info(value = """
            Defines logic to render the newGeoLayer.
            By default this will render the flat texture set in textureResource
            onto the entity as an overlay. This method overrides the render method completely
            allowing scripters to define their own render logic.
            
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
    public CustomGeoLayerJSBuilder<T> render(Consumer<ContextUtils.PreRenderContext<T>> render) {
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
                return "kubejs:textures/entity/wyrm.png" // Some Identifier representing the texture resource;
            });
            ```
            """)
    public CustomGeoLayerJSBuilder<T> textureResource(Function<T, Object> function) {
        textureResource = entity -> {
            entity = ensureIAnimatableJS(entity);
            Object obj = function.apply(entity);
            if (obj instanceof String && !obj.toString().equals("undefined")) {
                return Identifier.parse((String) obj);
            } else if (obj instanceof Identifier) {
                return (Identifier) obj;
            } else {
                EntityJSHelperClass.logWarningMessageOnce("Invalid return value for textureResource in newGeoLayer builder: " + obj + ". Defaulting to " + entity.getBuilder().newID("textures/entity/", ".png"));
                return entity.getBuilder().newID("textures/entity/", ".png");
            }
        };
        return this;
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