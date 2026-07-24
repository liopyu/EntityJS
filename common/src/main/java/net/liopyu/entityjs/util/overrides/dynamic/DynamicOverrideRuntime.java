package net.liopyu.entityjs.util.overrides.dynamic;

import dev.latvian.mods.rhino.util.HideFromJS;
import net.liopyu.entityjs.builders.misc.CustomEntityBuilder;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.common.util.EntityJSHelperClass;
import net.liopyu.entityjs.util.EntityJSUtils;
import net.liopyu.entityjs.common.util.overrides.OverrideUtils;
import net.liopyu.entityjs.common.util.overrides.catalog.DynamicOverrideMethodCatalog;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Runtime dispatcher called by generated dynamic override subclasses.
 * ASM-generated methods pass the entity instance, override key, optional super-bridge
 * name, and Java arguments here. The dispatcher finds the registered
 * {@link CustomEntityBuilder}, creates the script-facing dynamic override context, invokes
 * the callback, handles lazy super-call fallback, and coerces the script result back to
 * the Java return type expected by the overridden method.
 */
public final class DynamicOverrideRuntime {
    private static final Map<EntityType<?>, CustomEntityBuilder> BUILDERS = new ConcurrentHashMap<>();
    private static final Map<BridgeKey, Method> BRIDGES = new ConcurrentHashMap<>();

    private DynamicOverrideRuntime() {
    }

    @HideFromJS
    public static void register(EntityType<?> type, CustomEntityBuilder builder) {
        BUILDERS.put(type, builder);
    }

    @HideFromJS
    public static void clearCaches() {
        BUILDERS.clear();
        BRIDGES.clear();
    }

    @HideFromJS
    public static void invokeVoid(Entity entity, String method, String bridgeName, Object[] args) {
        invokeResult(entity, method, bridgeName, args);
    }

    @HideFromJS
    public static boolean invokeBoolean(Entity entity, String method, String bridgeName, Object[] args) {
        InvocationResult result = invokeResult(entity, method, bridgeName, args);
        Object value = result.value();
        if (value instanceof Boolean bool) {
            return bool;
        }
        Object fallback = result.callFallback();
        return fallback instanceof Boolean bool ? bool : false;
    }

    @HideFromJS
    public static byte invokeByte(Entity entity, String method, String bridgeName, Object[] args) {
        InvocationResult result = invokeResult(entity, method, bridgeName, args);
        Object value = result.value();
        if (value instanceof Number number) {
            return number.byteValue();
        }
        Object fallback = result.callFallback();
        return fallback instanceof Number number ? number.byteValue() : 0;
    }

    @HideFromJS
    public static char invokeChar(Entity entity, String method, String bridgeName, Object[] args) {
        InvocationResult result = invokeResult(entity, method, bridgeName, args);
        Object value = result.value();
        if (value instanceof Character character) {
            return character;
        }
        if (value instanceof String string && string.length() == 1) {
            return string.charAt(0);
        }
        Object fallback = result.callFallback();
        if (fallback instanceof Character character) {
            return character;
        }
        if (fallback instanceof String string && string.length() == 1) {
            return string.charAt(0);
        }
        return 0;
    }

    @HideFromJS
    public static short invokeShort(Entity entity, String method, String bridgeName, Object[] args) {
        InvocationResult result = invokeResult(entity, method, bridgeName, args);
        Object value = result.value();
        if (value instanceof Number number) {
            return number.shortValue();
        }
        Object fallback = result.callFallback();
        return fallback instanceof Number number ? number.shortValue() : 0;
    }

    @HideFromJS
    public static int invokeInt(Entity entity, String method, String bridgeName, Object[] args) {
        InvocationResult result = invokeResult(entity, method, bridgeName, args);
        Object value = result.value();
        if (value instanceof Number number) {
            return number.intValue();
        }
        Object fallback = result.callFallback();
        return fallback instanceof Number number ? number.intValue() : 0;
    }

    @HideFromJS
    public static long invokeLong(Entity entity, String method, String bridgeName, Object[] args) {
        InvocationResult result = invokeResult(entity, method, bridgeName, args);
        Object value = result.value();
        if (value instanceof Number number) {
            return number.longValue();
        }
        Object fallback = result.callFallback();
        return fallback instanceof Number number ? number.longValue() : 0L;
    }

    @HideFromJS
    public static float invokeFloat(Entity entity, String method, String bridgeName, Object[] args) {
        InvocationResult result = invokeResult(entity, method, bridgeName, args);
        Object value = result.value();
        if (value instanceof Number number) {
            return number.floatValue();
        }
        Object fallback = result.callFallback();
        return fallback instanceof Number number ? number.floatValue() : 0F;
    }

    @HideFromJS
    public static double invokeDouble(Entity entity, String method, String bridgeName, Object[] args) {
        InvocationResult result = invokeResult(entity, method, bridgeName, args);
        Object value = result.value();
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        Object fallback = result.callFallback();
        return fallback instanceof Number number ? number.doubleValue() : 0D;
    }

    @HideFromJS
    public static Object invokeObject(Entity entity, String method, String bridgeName, Object[] args) {
        InvocationResult result = invokeResult(entity, method, bridgeName, args);
        Class<?> returnType = returnType(entity, method);
        Object value = result.value();
        if (returnType == null || returnType == void.class || value == null || returnType.isInstance(value)) {
            return value;
        }

        Object fallback = result.callFallback();
        return fallback == null || returnType.isInstance(fallback) ? fallback : null;
    }

    private static InvocationResult invokeResult(Entity entity, String method, String bridgeName, Object[] args) {
        CustomEntityBuilder builder = builderFor(entity);
        if (builder == null) {
            Supplier<Object> fallback = () -> invokeSuperBridge(entity, null, method, bridgeName, args);
            return new InvocationResult(fallback.get(), fallback);
        }

        var callback = builder.getDynamicOverride(method);
        DynamicOverrideMethodCatalog.MethodSpec methodSpec = DynamicOverrideMethodCatalog.resolve(builder.getEntityClass(), method);
        boolean abstractMethod = methodSpec != null && methodSpec.abstractMethod();
        String scriptMethod = methodSpec == null ? method : methodSpec.scriptKey();
        if (callback == null) {
            Supplier<Object> fallback = () -> abstractMethod ? defaultValue(builder.getEntityClass(), method) : invokeSuperBridge(entity, builder.getEntityClass(), method, bridgeName, args);
            return new InvocationResult(fallback.get(), fallback);
        }

        ContextUtils.DynamicOverrideContext.SuperCall superCall = abstractMethod
                ? overrideArgs -> {
                    throw new IllegalStateException("[EntityJS]: Dynamic override '" + scriptMethod + "' is abstract and has no super implementation.");
                }
                : overrideArgs -> invokeSuperBridge(entity, builder.getEntityClass(), method, bridgeName, superArgs(args, overrideArgs));
        ContextUtils.DynamicOverrideContext<Entity> context = new ContextUtils.DynamicOverrideContext<>(
                entity,
                scriptMethod,
                buildArgs(builder.getEntityClass(), method, args),
                superCall
        );
        Supplier<Object> fallback = abstractMethod ? () -> defaultValue(builder.getEntityClass(), method) : context::superCall;

        try {
            return new InvocationResult(OverrideUtils.with(context::superCall, () -> callback.apply(context)), fallback);
        } catch (Throwable throwable) {
            EntityJSHelperClass.logErrorMessageOnceCatchable("[EntityJS]: Error in dynamic override '" + scriptMethod + "' for " + safeEntityName(entity) + ".", throwable);
            return new InvocationResult(fallback.get(), fallback);
        }
    }

    private static CustomEntityBuilder builderFor(Entity entity) {
        CustomEntityBuilder builder = BUILDERS.get(entity.getType());
        if (builder != null) {
            return builder;
        }

        Object found = EntityJSUtils.getEntityBuilder(entity.getType());
        if (found instanceof CustomEntityBuilder customBuilder) {
            BUILDERS.put(entity.getType(), customBuilder);
            return customBuilder;
        }
        return null;
    }

    private static Map<String, Object> buildArgs(Class<? extends Entity> baseClass, String method, Object[] args) {
        Map<String, Object> values = new LinkedHashMap<>();
        String[] names = baseClass == null
                ? fallbackParameterNames(args.length)
                : DynamicOverrideMethodCatalog.parameterNames(baseClass, method, args.length);
        for (int i = 0; i < args.length; i++) {
            values.put(names[i], args[i]);
            values.put("arg" + i, args[i]);
        }
        return values;
    }

    private static String[] fallbackParameterNames(int count) {
        String[] names = new String[count];
        for (int i = 0; i < count; i++) {
            names[i] = "arg" + i;
        }
        return names;
    }

    private static Object invokeSuperBridge(Entity entity, Class<? extends Entity> baseClass, String method, String bridgeName, Object[] args) {
        DynamicOverrideMethodCatalog.MethodSpec methodSpec = baseClass == null ? null : DynamicOverrideMethodCatalog.resolve(baseClass, method);
        String scriptMethod = methodSpec == null ? method : methodSpec.scriptKey();
        try {
            Class<?>[] parameterTypes = parameterTypes(baseClass, method);
            Method bridge = BRIDGES.computeIfAbsent(new BridgeKey(entity.getClass(), bridgeName), key -> findBridge(key.entityClass(), key.bridgeName(), parameterTypes));
            return bridge.invoke(entity, prepareBridgeArgs(scriptMethod, parameterTypes, args));
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause() == null ? exception : exception.getCause();
            EntityJSHelperClass.logErrorMessageOnceCatchable("[EntityJS]: Error calling dynamic override super bridge '" + scriptMethod + "' for " + safeEntityName(entity) + ".", cause);
        } catch (Throwable throwable) {
            EntityJSHelperClass.logErrorMessageOnceCatchable("[EntityJS]: Error finding dynamic override super bridge '" + scriptMethod + "' for " + safeEntityName(entity) + ".", throwable);
        }
        return defaultValue(baseClass, method);
    }

    private static Object[] superArgs(Object[] originalArgs, Object[] overrideArgs) {
        if (overrideArgs == null || overrideArgs.length == 0) {
            return originalArgs;
        }
        return overrideArgs.clone();
    }

    private static Object[] prepareBridgeArgs(String method, Class<?>[] parameterTypes, Object[] args) {
        Object[] values = args == null ? new Object[0] : args;
        if (values.length != parameterTypes.length) {
            throw new IllegalArgumentException("Dynamic override super bridge '" + method + "' expected " + parameterTypes.length + " argument(s), got " + values.length + ".");
        }
        Object[] converted = new Object[values.length];
        for (int i = 0; i < values.length; i++) {
            converted[i] = convertArgument(parameterTypes[i], values[i]);
        }
        return converted;
    }

    private static Object convertArgument(Class<?> parameterType, Object value) {
        if (!parameterType.isPrimitive() || value == null) {
            return value;
        }
        if (parameterType == boolean.class) {
            return value instanceof Boolean bool ? bool : Boolean.parseBoolean(String.valueOf(value));
        }
        if (parameterType == char.class) {
            if (value instanceof Character character) {
                return character;
            }
            if (value instanceof Number number) {
                return (char) number.intValue();
            }
            String string = String.valueOf(value);
            return string.isEmpty() ? (char) 0 : string.charAt(0);
        }
        if (!(value instanceof Number number)) {
            return value;
        }
        if (parameterType == byte.class) {
            return number.byteValue();
        }
        if (parameterType == short.class) {
            return number.shortValue();
        }
        if (parameterType == int.class) {
            return number.intValue();
        }
        if (parameterType == long.class) {
            return number.longValue();
        }
        if (parameterType == float.class) {
            return number.floatValue();
        }
        if (parameterType == double.class) {
            return number.doubleValue();
        }
        return value;
    }

    private static Class<?>[] parameterTypes(Class<? extends Entity> baseClass, String method) {
        DynamicOverrideMethodCatalog.MethodSpec methodSpec = baseClass == null ? null : DynamicOverrideMethodCatalog.resolve(baseClass, method);
        return methodSpec == null ? new Class<?>[0] : methodSpec.parameterTypes();
    }

    private static Class<?> returnType(Entity entity, String method) {
        CustomEntityBuilder builder = builderFor(entity);
        DynamicOverrideMethodCatalog.MethodSpec methodSpec = builder == null ? null : DynamicOverrideMethodCatalog.resolve(builder.getEntityClass(), method);
        return methodSpec == null ? null : methodSpec.returnType();
    }

    private static Method findBridge(Class<?> entityClass, String bridgeName, Class<?>[] parameterTypes) {
        try {
            Method bridge = entityClass.getDeclaredMethod(bridgeName, parameterTypes);
            bridge.setAccessible(true);
            return bridge;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Object defaultValue(Class<? extends Entity> baseClass, String method) {
        DynamicOverrideMethodCatalog.MethodSpec methodSpec = baseClass == null ? null : DynamicOverrideMethodCatalog.resolve(baseClass, method);
        if (methodSpec == null || !methodSpec.returnType().isPrimitive()) {
            return null;
        }
        Class<?> returnType = methodSpec.returnType();
        if (returnType == boolean.class) {
            return false;
        }
        if (returnType == char.class) {
            return (char) 0;
        }
        if (returnType == long.class) {
            return 0L;
        }
        if (returnType == float.class) {
            return 0F;
        }
        if (returnType == double.class) {
            return 0D;
        }
        return 0;
    }

    private static String safeEntityName(Entity entity) {
        try {
            return String.valueOf(entity.getType());
        } catch (Throwable ignored) {
            return entity.getClass().getName();
        }
    }

    private record BridgeKey(Class<?> entityClass, String bridgeName) {
    }

    private record InvocationResult(Object value, Supplier<Object> fallback) {
        private Object callFallback() {
            return fallback.get();
        }
    }
}
