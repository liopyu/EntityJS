package net.liopyu.entityjs.client;

import net.liopyu.entityjs.builders.misc.EntityReflection;
import net.liopyu.entityjs.mixin.EntityRenderersAccessor;
import net.liopyu.entityjs.common.util.EntityJSHelperClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public final class ClientEntityRendererTypeValidator {
    private ClientEntityRendererTypeValidator() {
    }

    public static boolean validateRendererTypeCompatibility(Object id, Class<? extends Entity> entityClass, EntityType<?> rendererType) {
        EntityRendererProvider<?> provider = EntityRenderersAccessor.entityjs$getProviders().get(rendererType);
        if (provider == null) {
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: No renderer provider was found for entity type " + rendererType
                    + " while validating custom entity " + id + ".");
            return false;
        }

        EntityRendererProvider.Context context = rendererContext();
        if (context == null) {
            return EntityReflection.validateRendererTypeBaseClassCompatibility(id, entityClass, rendererType);
        }

        try {
            EntityRenderer<?> renderer = provider.create(context);
            return validateRendererClass(id, entityClass, rendererType, renderer.getClass());
        } catch (Throwable ignored) {
            return EntityReflection.validateRendererTypeBaseClassCompatibility(id, entityClass, rendererType);
        }
    }

    private static boolean validateRendererClass(Object id, Class<? extends Entity> entityClass, EntityType<?> rendererType, Class<?> rendererClass) {
        Class<? extends Entity> rendererEntityClass = EntityReflection.rendererEntityClass(rendererClass);
        if (rendererEntityClass == null || rendererEntityClass.isAssignableFrom(entityClass)) {
            return true;
        }
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Custom entity " + id + " uses entity class " + entityClass.getName()
                + ", but renderer from entity type " + rendererType + " (" + rendererClass.getName()
                + ") is typed for " + rendererEntityClass.getName()
                + ". Use a renderer source whose renderer entity type is a superclass of the custom entity class.");
        return false;
    }

    private static EntityRendererProvider.Context rendererContext() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return null;
        }
        EntityRenderDispatcher dispatcher = minecraft.getEntityRenderDispatcher();
        if (dispatcher == null) {
            return null;
        }
        return new EntityRendererProvider.Context(
                dispatcher,
                minecraft.getItemRenderer(),
                minecraft.getBlockRenderer(),
                dispatcher.getItemInHandRenderer(),
                minecraft.getResourceManager(),
                minecraft.getEntityModels(),
                minecraft.font
        );
    }
}
