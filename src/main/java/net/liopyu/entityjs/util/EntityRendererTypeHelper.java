package net.liopyu.entityjs.util;

import net.liopyu.entityjs.builders.misc.CustomEntityBuilder;
import net.liopyu.entityjs.builders.misc.EntityReflection;
import net.liopyu.entityjs.mixin.EntityRenderersAccessor;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public final class EntityRendererTypeHelper {
    private EntityRendererTypeHelper() {
    }

    public static <T extends Entity> EntityRenderer<? super T> create(EntityRendererProvider.Context context, CustomEntityBuilder builder) {
        EntityType<?> rendererType = builder.getEntityRendererType();
        EntityRendererProvider<?> provider = EntityRenderersAccessor.entityjs$getProviders().get(rendererType);
        if (provider == null) {
            throw new IllegalArgumentException("[EntityJS]: No renderer provider was found for entity type " + rendererType + " while registering custom entity " + builder.id + ".");
        }
        try {
            EntityRenderer<?> renderer = provider.create(context);
            return compatibleRendererOrThrow(builder, renderer, "renderer from entity type " + rendererType);
        } catch (Throwable throwable) {
            throw new IllegalArgumentException("[EntityJS]: Failed to create renderer from entity type " + rendererType + " for custom entity " + builder.id + ".", throwable);
        }
    }

    public static <T extends Entity> EntityRenderer<? super T> compatibleRendererOrThrow(CustomEntityBuilder builder, Object renderer, String source) {
        if (!(renderer instanceof EntityRenderer<?> entityRenderer)) {
            throw new IllegalArgumentException("[EntityJS]: Custom entity " + builder.id + " returned an invalid renderer from " + source + ".");
        }
        Class<? extends Entity> rendererEntityClass = EntityReflection.rendererEntityClass(entityRenderer.getClass());
        if (rendererEntityClass != null && !rendererEntityClass.isAssignableFrom(builder.getEntityClass())) {
            throw new IllegalArgumentException("[EntityJS]: Custom entity " + builder.id + " uses entity class " + builder.getEntityClass().getName()
                    + ", but " + source + " (" + entityRenderer.getClass().getName() + ") is typed for " + rendererEntityClass.getName()
                    + ". Use a renderer whose entity type is a superclass of the custom entity class.");
        }
        return (EntityRenderer<? super T>) entityRenderer;
    }
}
