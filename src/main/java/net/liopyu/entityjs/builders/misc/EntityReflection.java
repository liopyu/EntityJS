package net.liopyu.entityjs.builders.misc;

import net.liopyu.entityjs.entities.living.entityjs.IAnimatableJSCustom;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLLoader;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Reflection utility for custom entity registration, generated typings, and dynamic
 * subclass support. It centralizes checks for mixin classes, renderer compatibility,
 * renderer entity generic extraction, and the hook EntityJS uses when a custom entity
 * class may need a runtime subclass.
 */
public class EntityReflection {
    private static final String MIXIN_ANNOTATION = "org.spongepowered.asm.mixin.Mixin";
    private static final String ENTITY_RENDERER_CLASS = "net.minecraft.client.renderer.entity.EntityRenderer";
    private static final String ENTITY_RENDERER_CONTEXT_CLASS = "net.minecraft.client.renderer.entity.EntityRendererProvider$Context";
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
     * Resolves the runtime entity class EntityJS should register for a custom entity.
     * This is cached because older implementations generated subclasses here; the cache keeps that
     * reflection-heavy path from being repeated if subclass generation is reintroduced.
     *
     * @param baseClass the entity class requested by the script
     * @return the class to register for the custom entity
     */
    public static Class<? extends Entity> createEntityClass(Class<? extends Entity> baseClass) {
        if (IAnimatableJSCustom.class.isAssignableFrom(baseClass)) {
            return baseClass;
        }

        return subclassCache.computeIfAbsent(baseClass, EntityReflection::getSubclassInstance);
    }

    /**
     * Hook for runtime subclass generation.
     * Current branches return the original class, but callers still go through this method so the
     * unsafe bytecode/reflection work stays isolated if generated subclasses are needed again.
     *
     * @param baseClass the base entity class
     * @return the entity class to register
     */
    public static Class<? extends Entity> getSubclassInstance(Class<? extends Entity> baseClass) {
        return baseClass;
    }

    /**
     * Best-effort extraction of the entity type parameter from an EntityRenderer subclass.
     * Raw renderers, wildcards, or erased generic chains return null so callers can avoid rejecting
     * valid renderers that Java reflection cannot prove safe.
     */
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
        if (!isRendererClassCompatible(rendererClass)) {
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Custom entity " + id + " renderer class "
                    + safeClassName(rendererClass) + " " + rendererClassCompatibilityReason(rendererClass) + ".");
            return false;
        }
        Class<? extends Entity> rendererEntityClass = rendererEntityClass(rendererClass);
        if (rendererEntityClass == null || rendererEntityClass.isAssignableFrom(entityClass)) {
            return true;
        }
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Custom entity " + id + " uses entity class " + entityClass.getName()
                + ", but renderer " + rendererClass.getName() + " is typed for " + rendererEntityClass.getName()
                + ". Use a renderer whose entity type is a superclass of the custom entity class.");
        return false;
    }

    public static boolean isRendererClassCompatible(Class<?> rendererClass) {
        return rendererClass != null
                && extendsClassNamed(rendererClass, ENTITY_RENDERER_CLASS)
                && isInstantiablePublicClass(rendererClass)
                && hasRendererContextConstructor(rendererClass);
    }

    public static Constructor<?> findRendererConstructor(Class<?> rendererClass, Class<?> contextClass) {
        if (rendererClass == null || contextClass == null) {
            return null;
        }
        for (Constructor<?> constructor : rendererClass.getDeclaredConstructors()) {
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            if (parameterTypes.length == 1 && parameterTypes[0].isAssignableFrom(contextClass)) {
                return constructor;
            }
        }
        return null;
    }

    public static Class<?> resolveClassName(Object id, String role, String className) {
        return resolveClassName(id, role, className, FMLLoader.getDist() == Dist.CLIENT);
    }

    /**
     * Loads a script-provided class name without initializing the target class.
     * This is used for renderer and entity class strings so startup scripts can avoid direct
     * Java.loadClass references to client-only classes on a dedicated server.
     */
    public static Class<?> resolveClassName(Object id, String role, String className, boolean logLoadFailure) {
        String resolvedClassName = normalizeClassName(className);
        if (resolvedClassName == null || resolvedClassName.isBlank()) {
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Custom entity " + id + " tried to use a blank " + role + " class name.");
            return null;
        }
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader == null) {
                classLoader = EntityReflection.class.getClassLoader();
            }
            return Class.forName(resolvedClassName, false, classLoader);
        } catch (ClassNotFoundException exception) {
            if (logLoadFailure) {
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Custom entity " + id + " tried to use an unknown " + role + " class name: " + resolvedClassName + ".");
            }
            return null;
        } catch (LinkageError error) {
            if (logLoadFailure) {
                EntityJSHelperClass.logErrorMessageOnceCatchable("[EntityJS]: Custom entity " + id + " " + role + " class could not be loaded: " + resolvedClassName + ".", error);
            }
            return null;
        }
    }

    public static boolean isClientEnvironment() {
        return FMLLoader.getDist() == Dist.CLIENT;
    }

    public static String normalizeClassName(String className) {
        if (className == null) {
            return null;
        }
        String normalizedClassName = className.trim();
        if (normalizedClassName.startsWith("class ")) {
            return normalizedClassName.substring("class ".length()).trim();
        }
        if (normalizedClassName.startsWith("interface ")) {
            return normalizedClassName.substring("interface ".length()).trim();
        }
        return normalizedClassName;
    }

    private static boolean extendsClassNamed(Class<?> type, String className) {
        Class<?> current = type;
        while (current != null && current != Object.class) {
            if (current.getName().equals(className)) {
                return true;
            }
            current = current.getSuperclass();
        }
        return false;
    }

    private static boolean hasRendererContextConstructor(Class<?> type) {
        for (Constructor<?> constructor : type.getDeclaredConstructors()) {
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            if (parameterTypes.length == 1
                    && parameterTypes[0].getName().equals(ENTITY_RENDERER_CONTEXT_CLASS)
                    && Modifier.isPublic(constructor.getModifiers())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isInstantiablePublicClass(Class<?> type) {
        int modifiers = type.getModifiers();
        return Modifier.isPublic(modifiers) && !Modifier.isAbstract(modifiers) && !type.isInterface();
    }

    private static String rendererClassCompatibilityReason(Class<?> rendererClass) {
        if (!extendsClassNamed(rendererClass, ENTITY_RENDERER_CLASS)) {
            return "does not extend " + ENTITY_RENDERER_CLASS;
        }
        if (!isInstantiablePublicClass(rendererClass)) {
            return "is not a public concrete class";
        }
        return "does not provide a public EntityRendererProvider.Context constructor";
    }

    private static String safeClassName(Class<?> type) {
        return type == null ? "null" : type.getName();
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

    /**
     * Calls the client renderer registry validator reflectively so this common class does not link
     * client-only renderer classes while loading on a dedicated server.
     */
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
