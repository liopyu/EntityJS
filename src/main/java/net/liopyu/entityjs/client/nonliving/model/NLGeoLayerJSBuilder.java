package net.liopyu.entityjs.client.nonliving.model;

import net.minecraft.client.renderer.rendertype.RenderTypes;

import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.builders.nonliving.BaseEntityBuilder;
import net.liopyu.entityjs.client.living.model.GeoLayerJSBuilder;
import net.liopyu.entityjs.client.nonliving.KubeJSNLEntityRenderer;
import net.liopyu.entityjs.entities.nonliving.entityjs.IAnimatableJSNL;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

import java.util.function.Consumer;
import java.util.function.Function;

public class NLGeoLayerJSBuilder<T extends Entity & IAnimatableJSNL> {
    public transient Function<T, Object> textureResource;
    public BaseEntityBuilder<T> builder;

    public transient Consumer<ContextUtils.PreRenderContext<T>> render;
    public transient Consumer<ContextUtils.PreRenderContext<T>> preRender;

    public NLGeoLayerJSBuilder(BaseEntityBuilder<T> builder) {
        this.builder = builder;
    }

    public NLGeoLayerJS<T> build(KubeJSNLEntityRenderer<T> entityRendererIn, BaseEntityBuilder<T> builder) {
        return new NLGeoLayerJS<>(entityRendererIn, this, builder);
    }

    public NLGlowingGeoLayerJS<T> buildGlowing(KubeJSNLEntityRenderer<T> entityRendererIn, BaseEntityBuilder<T> builder) {
        return new NLGlowingGeoLayerJS<>(entityRendererIn, this, builder);
    }

    public BaseEntityBuilder<T> getBuilder() {
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
    public NLGeoLayerJSBuilder<T> preRender(Consumer<ContextUtils.PreRenderContext<T>> preRender) {
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
    public NLGeoLayerJSBuilder<T> render(Consumer<ContextUtils.PreRenderContext<T>> render) {
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
    public NLGeoLayerJSBuilder<T> textureResource(Function<T, Object> function) {
        textureResource = entity -> {
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

    public transient RenderType setRenderType;

    @Info(value = """
            Sets the render type for the entity's layer.
            
            Example usage:
            ```javascript
            builder.setRenderType(RenderTypes.entityCutout("kubejs:path/to/texture", true));
            ```
            """)
    public NLGeoLayerJSBuilder<T> setRenderType(RenderType type) {
        setRenderType = type;
        return this;
    }

    public transient Function<T, RenderType> renderTypeFunction;

    @Info(value = """
            Sets the render type for the entity's layer via a function.
            
            Example usage:
            ```javascript
            builder.renderType(entity => RenderTypes.entityCutout("kubejs:path/to/texture", outlineEntityBoolean));
            ```
            """)
    public NLGeoLayerJSBuilder<T> renderType(Function<T, net.minecraft.client.renderer.rendertype.RenderType> type) {
        renderTypeFunction = type;
        return this;
    }
}