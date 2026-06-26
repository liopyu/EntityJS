package net.liopyu.entityjs.builders.misc;

import net.liopyu.entityjs.entities.living.entityjs.IAnimatableJSCustom;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLLoader;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EntityReflection {
    private static final String MIXIN_ANNOTATION = "org.spongepowered.asm.mixin.Mixin";
    private static final String ENTITY_RENDERER_CLASS = "net.minecraft.client.renderer.entity.EntityRenderer";
    private static final ConcurrentHashMap<Class<? extends Entity>, Class<? extends Entity>> subclassCache = new ConcurrentHashMap<>();

    public static boolean isMixinClass(Class<?> type) {
        Class<?> current = type;
        while (current != null && current != Object.class) {
            for (Annotation annotation : current.getDeclaredAnnotations()) {
                if (annotation.annotationType().getName().equals(MIXIN_ANNOTATION)) {
                    return true;
                }
            }
            current = current.getSuperclass();
        }
        return false;
    }

    /**
     * Creates and stores a subclass of the provided base entity class that implements IAnimatableJSCustom.
     * If the entity already implements IAnimatableJSCustom, it returns the original class.
     *
     * @param baseClass The entity class to extend
     * @return The stored class implementing IAnimatableJSCustom
     */
    public static Class<? extends Entity> createEntityClass(Class<? extends Entity> baseClass) {
        if (IAnimatableJSCustom.class.isAssignableFrom(baseClass)) {
            return baseClass;
        }

        return subclassCache.computeIfAbsent(baseClass, EntityReflection::getSubclassInstance);
    }

    /**
     * Uses Reflection to create a subclass at runtime.
     *
     * @param baseClass The base entity class
     * @return A dynamically grab the subclass implementing IAnimatableJSCustom
     */
    public static Class<? extends Entity> getSubclassInstance(Class<? extends Entity> baseClass) {
        return baseClass;
    }

    public static Class<? extends Entity> rendererEntityClass(Class<?> rendererClass) {
        Map<TypeVariable<?>, Type> typeBindings = new HashMap<>();
        Class<?> current = rendererClass;
        while (current != null && current != Object.class) {
            Type genericSuperclass = current.getGenericSuperclass();
            Class<?> rawSuperclass = rawClass(genericSuperclass);
            if (rawSuperclass == null) {
                return null;
            }
            if (genericSuperclass instanceof ParameterizedType parameterizedType) {
                bindTypeArguments(parameterizedType, typeBindings);
                if (rawSuperclass.getName().equals(ENTITY_RENDERER_CLASS)) {
                    Type[] arguments = parameterizedType.getActualTypeArguments();
                    if (arguments.length == 0) {
                        return null;
                    }
                    Class<?> entityClass = typeClass(resolveType(arguments[0], typeBindings));
                    return entityClass != null && Entity.class.isAssignableFrom(entityClass)
                            ? entityClass.asSubclass(Entity.class)
                            : null;
                }
            } else if (rawSuperclass.getName().equals(ENTITY_RENDERER_CLASS)) {
                return null;
            }
            current = rawSuperclass;
        }
        return null;
    }

    public static boolean validateRendererClassCompatibility(Object id, Class<? extends Entity> entityClass, Class<?> rendererClass) {
        Class<? extends Entity> rendererEntityClass = rendererEntityClass(rendererClass);
        if (rendererEntityClass == null || rendererEntityClass.isAssignableFrom(entityClass)) {
            return true;
        }
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Custom entity " + id + " uses entity class " + entityClass.getName()
                + ", but renderer " + rendererClass.getName() + " is typed for " + rendererEntityClass.getName()
                + ". Use a renderer whose entity type is a superclass of the custom entity class.");
        return false;
    }

    public static boolean validateRendererTypeCompatibility(Object id, Class<? extends Entity> entityClass, EntityType<?> rendererType) {
        if (FMLLoader.getDist() == Dist.CLIENT) {
            Boolean clientResult = validateRendererTypeCompatibilityOnClient(id, entityClass, rendererType);
            if (clientResult != null) {
                return clientResult;
            }
        }
        return validateRendererTypeBaseClassCompatibility(id, entityClass, rendererType);
    }

    private static Boolean validateRendererTypeCompatibilityOnClient(Object id, Class<? extends Entity> entityClass, EntityType<?> rendererType) {
        try {
            Class<?> validatorClass = Class.forName("net.liopyu.entityjs.client.ClientEntityRendererTypeValidator");
            Object result = validatorClass
                    .getMethod("validateRendererTypeCompatibility", Object.class, Class.class, EntityType.class)
                    .invoke(null, id, entityClass, rendererType);
            return result instanceof Boolean value ? value : null;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    public static boolean validateRendererTypeBaseClassCompatibility(Object id, Class<? extends Entity> entityClass, EntityType<?> rendererType) {
        Class<? extends Entity> rendererEntityClass = entityTypeGenericClass(rendererType);
        if (rendererEntityClass == null) {
            rendererEntityClass = rendererType.getBaseClass();
        }
        if (rendererEntityClass == null || rendererEntityClass.isAssignableFrom(entityClass)) {
            return true;
        }
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Custom entity " + id + " uses entity class " + entityClass.getName()
                + ", but renderer source entity type " + rendererType + " uses entity class " + rendererEntityClass.getName()
                + ". Use a renderer source whose entity class is a superclass of the custom entity class.");
        return false;
    }

    private static Class<? extends Entity> entityTypeGenericClass(EntityType<?> entityType) {
        for (Field field : EntityType.class.getFields()) {
            if (!Modifier.isStatic(field.getModifiers()) || !EntityType.class.isAssignableFrom(field.getType())) {
                continue;
            }
            try {
                if (field.get(null) != entityType) {
                    continue;
                }
            } catch (IllegalAccessException ignored) {
                continue;
            }
            if (!(field.getGenericType() instanceof ParameterizedType parameterizedType)) {
                return null;
            }
            Type[] arguments = parameterizedType.getActualTypeArguments();
            if (arguments.length == 0) {
                return null;
            }
            Class<?> typeClass = typeClass(arguments[0]);
            return typeClass != null && Entity.class.isAssignableFrom(typeClass)
                    ? typeClass.asSubclass(Entity.class)
                    : null;
        }
        return null;
    }

    private static void bindTypeArguments(ParameterizedType parameterizedType, Map<TypeVariable<?>, Type> typeBindings) {
        if (!(parameterizedType.getRawType() instanceof Class<?> rawType)) {
            return;
        }
        TypeVariable<?>[] variables = rawType.getTypeParameters();
        Type[] arguments = parameterizedType.getActualTypeArguments();
        for (int i = 0; i < variables.length && i < arguments.length; i++) {
            typeBindings.put(variables[i], resolveType(arguments[i], typeBindings));
        }
    }

    private static Type resolveType(Type type, Map<TypeVariable<?>, Type> typeBindings) {
        while (type instanceof TypeVariable<?> variable) {
            Type resolved = typeBindings.get(variable);
            if (resolved == null || resolved == variable) {
                return variable;
            }
            type = resolved;
        }
        if (type instanceof WildcardType wildcardType) {
            Type[] upperBounds = wildcardType.getUpperBounds();
            return upperBounds.length == 0 ? Object.class : resolveType(upperBounds[0], typeBindings);
        }
        if (type instanceof ParameterizedType parameterizedType) {
            return parameterizedType.getRawType();
        }
        return type;
    }

    private static Class<?> rawClass(Type type) {
        if (type instanceof Class<?> typeClass) {
            return typeClass;
        }
        if (type instanceof ParameterizedType parameterizedType && parameterizedType.getRawType() instanceof Class<?> typeClass) {
            return typeClass;
        }
        return null;
    }

    private static Class<?> typeClass(Type type) {
        Type resolved = type instanceof ParameterizedType parameterizedType ? parameterizedType.getRawType() : type;
        return resolved instanceof Class<?> typeClass ? typeClass : null;
    }
}
