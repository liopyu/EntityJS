package net.liopyu.entityjs.util.overrides;

import dev.latvian.mods.rhino.Callable;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.Scriptable;
import dev.latvian.mods.rhino.ScriptableObject;
import dev.latvian.mods.rhino.WrapFactory;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.liopyu.entityjs.util.BooleanCallback;
import net.liopyu.entityjs.util.ContextUtils;
import net.minecraft.world.entity.Entity;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.WeakHashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public final class CallbackInvoker {
    private static final Map<Object, List<Direct>> INIT_CALLBACKS = Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<Direct, Boolean> DIRECT_CALLBACKS = Collections.synchronizedMap(new WeakHashMap<>());
    private static final Object[] EMPTY_ARGS = new Object[0];

    private CallbackInvoker() {
    }

    @HideFromJS
    public static void clearCaches() {
        INIT_CALLBACKS.clear();
        synchronized (DIRECT_CALLBACKS) {
            DIRECT_CALLBACKS.keySet().forEach(Direct::clearCache);
        }
    }

    @SuppressWarnings("unchecked")
    @HideFromJS
    public static <T> BooleanCallback<T> wrapBoolean(BooleanCallback<T> callback) {
        Direct direct = Direct.create(callback);
        return direct == null ? callback : new FastBooleanCallback<>(callback, direct);
    }

    @SuppressWarnings("unchecked")
    @HideFromJS
    public static <T, R> Function<T, R> wrapFunction(Function<T, R> callback) {
        Direct direct = Direct.create(callback);
        return direct == null ? callback : new FastFunction<>(callback, direct);
    }

    @HideFromJS
    public static <T> Consumer<T> wrapConsumer(Consumer<T> callback) {
        Direct direct = Direct.create(callback);
        return direct == null ? callback : new FastConsumer<>(callback, direct);
    }

    @HideFromJS
    public static <T> Predicate<T> wrapPredicate(Predicate<T> callback) {
        Direct direct = Direct.create(callback);
        return direct == null ? callback : new FastPredicate<>(callback, direct);
    }

    @SuppressWarnings("unchecked")
    @HideFromJS
    public static <T> T wrapFunctional(T callback, Class<T> interfaceType) {
        if (callback == null || interfaceType == null || !interfaceType.isInterface()) {
            return callback;
        }

        Direct direct = Direct.create(callback);
        Method method = functionalMethod(interfaceType);
        if (direct == null || method == null) {
            return callback;
        }

        return (T) Proxy.newProxyInstance(
                interfaceType.getClassLoader(),
                new Class<?>[]{interfaceType},
                new FastFunctionalCallback(callback, direct, method)
        );
    }

    @HideFromJS
    public static void wrapCallbackFields(Object target) {
        wrapCallbackFields(target, new IdentityHashMap<>());
        if (target != null) {
            INIT_CALLBACKS.remove(target);
        }
    }

    @HideFromJS
    public static void initCallbackFields(Object target, Object value) {
        for (Direct direct : initCallbacks(target)) {
            direct.initArgument(value);
        }
    }

    private static void wrapCallbackFields(Object target, IdentityHashMap<Object, Boolean> seen) {
        if (target == null || seen.put(target, Boolean.TRUE) != null || isLeafObject(target)) {
            return;
        }

        if (target instanceof Collection<?> collection) {
            if (collection instanceof List<?> list) {
                wrapList(list, seen);
            } else {
                collection.forEach(value -> wrapCallbackFields(value, seen));
            }
            return;
        }

        if (target instanceof Map<?, ?> map) {
            wrapMap(map, seen);
            return;
        }

        Class<?> type = target.getClass();
        if (type.isArray()) {
            int length = java.lang.reflect.Array.getLength(target);
            for (int i = 0; i < length; i++) {
                wrapCallbackFields(java.lang.reflect.Array.get(target, i), seen);
            }
            return;
        }

        if (!canInspectFields(target)) {
            return;
        }

        while (type != null && type != Object.class) {
            for (Field field : type.getDeclaredFields()) {
                int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers)) {
                    continue;
                }

                try {
                    field.setAccessible(true);
                    Object value = field.get(target);
                    Object wrapped = wrapCallback(value);
                    if (wrapped != value && !Modifier.isFinal(modifiers)) {
                        field.set(target, wrapped);
                    } else {
                        wrapCallbackFields(value, seen);
                    }
                } catch (ReflectiveOperationException | RuntimeException ignored) {
                }
            }
            type = type.getSuperclass();
        }
    }

    private static List<Direct> initCallbacks(Object target) {
        if (target == null) {
            return List.of();
        }

        List<Direct> callbacks = INIT_CALLBACKS.get(target);
        if (callbacks != null) {
            return callbacks;
        }

        synchronized (INIT_CALLBACKS) {
            callbacks = INIT_CALLBACKS.get(target);
            if (callbacks == null) {
                callbacks = List.copyOf(collectInitCallbacks(target, new IdentityHashMap<>(), new java.util.ArrayList<>()));
                INIT_CALLBACKS.put(target, callbacks);
            }
        }
        return callbacks;
    }

    private static List<Direct> collectInitCallbacks(Object target, IdentityHashMap<Object, Boolean> seen, List<Direct> callbacks) {
        if (target == null || seen.put(target, Boolean.TRUE) != null) {
            return callbacks;
        }

        collectInitCallback(target, callbacks);

        if (isLeafObject(target)) {
            return callbacks;
        }

        if (target instanceof Collection<?> collection) {
            collection.forEach(entry -> collectInitCallbacks(entry, seen, callbacks));
            return callbacks;
        }

        if (target instanceof Map<?, ?> map) {
            map.values().forEach(entry -> collectInitCallbacks(entry, seen, callbacks));
            return callbacks;
        }

        Class<?> type = target.getClass();
        if (type.isArray()) {
            int length = java.lang.reflect.Array.getLength(target);
            for (int i = 0; i < length; i++) {
                collectInitCallbacks(java.lang.reflect.Array.get(target, i), seen, callbacks);
            }
            return callbacks;
        }

        if (!canInspectFields(target)) {
            return callbacks;
        }

        while (type != null && type != Object.class) {
            for (Field field : type.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }

                try {
                    field.setAccessible(true);
                    collectInitCallbacks(field.get(target), seen, callbacks);
                } catch (ReflectiveOperationException | RuntimeException ignored) {
                }
            }
            type = type.getSuperclass();
        }
        return callbacks;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void wrapList(List<?> list, IdentityHashMap<Object, Boolean> seen) {
        List raw = list;
        for (int i = 0; i < raw.size(); i++) {
            Object value = raw.get(i);
            Object wrapped = wrapCallback(value);
            if (wrapped != value) {
                try {
                    raw.set(i, wrapped);
                } catch (UnsupportedOperationException ignored) {
                }
            } else {
                wrapCallbackFields(value, seen);
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void wrapMap(Map<?, ?> map, IdentityHashMap<Object, Boolean> seen) {
        for (Map.Entry entry : ((Map<?, ?>) map).entrySet()) {
            Object value = entry.getValue();
            Object wrapped = wrapCallback(value);
            if (wrapped != value) {
                try {
                    entry.setValue(wrapped);
                } catch (UnsupportedOperationException ignored) {
                }
            } else {
                wrapCallbackFields(value, seen);
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object wrapCallback(Object value) {
        if (value instanceof FastCallback) {
            return value;
        }
        if (value instanceof BooleanCallback<?> callback) {
            return wrapBoolean((BooleanCallback) callback);
        }
        if (value instanceof Function<?, ?> callback) {
            return wrapFunction((Function) callback);
        }
        if (value instanceof Consumer<?> callback) {
            return wrapConsumer((Consumer) callback);
        }
        if (value instanceof Predicate<?> callback) {
            return wrapPredicate((Predicate) callback);
        }
        if (Proxy.isProxyClass(value.getClass())) {
            Object wrapped = wrapProxyFunctional(value);
            if (wrapped != value) {
                return wrapped;
            }
        }
        return value;
    }

    private static Object wrapProxyFunctional(Object value) {
        InvocationHandler handler = Proxy.getInvocationHandler(value);
        if (handler instanceof FastFunctionalCallback) {
            return value;
        }

        for (Class<?> interfaceType : value.getClass().getInterfaces()) {
            Method method = functionalMethod(interfaceType);
            if (method != null && isEntityJsInterface(interfaceType)) {
                return wrapFunctional(value, (Class) interfaceType);
            }
        }
        return value;
    }

    private static void collectInitCallback(Object value, List<Direct> callbacks) {
        if (value instanceof FastBooleanCallback<?> callback) {
            callbacks.add(callback.direct());
        } else if (value instanceof FastFunction<?, ?> callback) {
            callbacks.add(callback.direct());
        } else if (value instanceof FastConsumer<?> callback) {
            callbacks.add(callback.direct());
        } else if (value instanceof FastPredicate<?> callback) {
            callbacks.add(callback.direct());
        } else if (Proxy.isProxyClass(value.getClass())) {
            InvocationHandler handler = Proxy.getInvocationHandler(value);
            if (handler instanceof FastFunctionalCallback callback) {
                callbacks.add(callback.direct());
            }
        }
    }

    private static boolean isEntityJsInterface(Class<?> type) {
        Package pkg = type.getPackage();
        return pkg != null && pkg.getName().startsWith("net.liopyu.entityjs.");
    }

    private static Method functionalMethod(Class<?> interfaceType) {
        Method found = null;
        for (Method method : interfaceType.getMethods()) {
            if (method.isDefault() || method.isBridge() || method.isSynthetic() || method.getDeclaringClass() == Object.class) {
                continue;
            }
            if (!Modifier.isAbstract(method.getModifiers())) {
                continue;
            }
            if (found != null && !sameSignature(found, method)) {
                return null;
            }
            found = method;
        }
        return found;
    }

    private static boolean sameSignature(Method left, Method right) {
        return left.getName().equals(right.getName()) && java.util.Arrays.equals(left.getParameterTypes(), right.getParameterTypes());
    }

    private static boolean isLeafObject(Object value) {
        Class<?> type = value.getClass();
        if (type.isPrimitive() || type.isEnum() || type.isRecord()) {
            return true;
        }
        Package pkg = type.getPackage();
        if (pkg == null) {
            return false;
        }
        String name = pkg.getName();
        return name.startsWith("java.")
                || name.startsWith("javax.")
                || name.startsWith("jdk.")
                || name.startsWith("sun.")
                || name.startsWith("net.minecraft.")
                || name.startsWith("net.neoforged.")
                || name.startsWith("dev.latvian.mods.");
    }

    private static boolean canInspectFields(Object value) {
        Package pkg = value.getClass().getPackage();
        if (pkg == null) {
            return false;
        }

        String name = pkg.getName();
        return name.startsWith("net.liopyu.entityjs.builders.")
                || name.startsWith("net.liopyu.entityjs.client.")
                || name.startsWith("net.liopyu.entityjs.util.overrides.data.");
    }

    private interface FastCallback {
    }

    private record FastBooleanCallback<T>(BooleanCallback<T> fallback, Direct direct) implements BooleanCallback<T>, FastCallback {
        @Override
        public Object test(T value) {
            return direct.invokeObject(value);
        }
    }

    private record FastFunction<T, R>(Function<T, R> fallback, Direct direct) implements Function<T, R>, FastCallback {
        @Override
        @SuppressWarnings("unchecked")
        public R apply(T value) {
            return (R) direct.invokeObject(value);
        }
    }

    private record FastConsumer<T>(Consumer<T> fallback, Direct direct) implements Consumer<T>, FastCallback {
        @Override
        public void accept(T value) {
            direct.invokeVoid(value);
        }
    }

    private record FastPredicate<T>(Predicate<T> fallback, Direct direct) implements Predicate<T>, FastCallback {
        @Override
        public boolean test(T value) {
            return direct.invokeBoolean(value);
        }
    }

    private record FastFunctionalCallback(Object fallback, Direct direct, Method function) implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getDeclaringClass() == Object.class) {
                return switch (method.getName()) {
                    case "toString" -> fallback.toString();
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == (args == null ? null : args[0]);
                    default -> method.invoke(fallback, args);
                };
            }
            if (method.isDefault()) {
                return InvocationHandler.invokeDefault(proxy, method, args == null ? EMPTY_ARGS : args);
            }
            if (sameSignature(function, method)) {
                return direct.invoke(method, args == null ? EMPTY_ARGS : args);
            }
            return defaultValue(method.getReturnType());
        }
    }

    private static final class Direct {
        private final Context cx;
        private final Scriptable topScope;
        private final Scriptable thisObj;
        private final Callable callable;
        private final WrapFactory wrapFactory;
        private final Map<Object, WeakReference<Object>> argumentWrappers = Collections.synchronizedMap(new WeakHashMap<>());
        private final ThreadLocal<Object[]> arguments = ThreadLocal.withInitial(() -> new Object[1]);

        private Direct(Context cx, Scriptable topScope, Scriptable thisObj, Callable callable) {
            this.cx = cx;
            this.topScope = topScope;
            this.thisObj = thisObj;
            this.callable = callable;
            this.wrapFactory = cx.getWrapFactory();
        }

        private static Direct create(Object callback) {
            if (callback == null || callback instanceof FastCallback || !Proxy.isProxyClass(callback.getClass())) {
                return null;
            }

            InvocationHandler handler = Proxy.getInvocationHandler(callback);
            Context cx = null;
            Scriptable topScope = null;
            Callable callable = null;
            Class<?> type = handler.getClass();
            while (type != null && type != Object.class) {
                for (Field field : type.getDeclaredFields()) {
                    try {
                        field.setAccessible(true);
                        Object value = field.get(handler);
                        if (value instanceof Context context) {
                            cx = context;
                        } else if (value instanceof Callable function) {
                            callable = function;
                        } else if (value instanceof Scriptable scriptable) {
                            topScope = scriptable;
                        }
                    } catch (ReflectiveOperationException | RuntimeException ignored) {
                    }
                }
                type = type.getSuperclass();
            }

            if (cx == null || topScope == null || callable == null) {
                return null;
            }

            Scriptable thisObj = cx.getWrapFactory().wrapAsJavaObject(cx, topScope, callback, null);
            Direct direct = new Direct(cx, topScope, thisObj, callable);
            DIRECT_CALLBACKS.put(direct, Boolean.TRUE);
            return direct;
        }

        private void clearCache() {
            argumentWrappers.clear();
            arguments.remove();
        }

        private void initArgument(Object value) {
            wrapArgument(value);
            if (value instanceof Entity entity) {
                wrapArgument(entity.getTags());
            }
        }

        private Object invokeObject(Object value) {
            Object result = invokeRaw(value);
            String method = CallbackProfiler.currentMethod();
            if (CallbackProfiler.enabled() && method != null) {
                return CallbackProfiler.profileLayer(method, "directConvertObject", () -> Context.jsToJava(cx, result, Object.class));
            }
            return Context.jsToJava(cx, result, Object.class);
        }

        private boolean invokeBoolean(Object value) {
            Object result = invokeRaw(value);
            String method = CallbackProfiler.currentMethod();
            if (CallbackProfiler.enabled() && method != null) {
                return CallbackProfiler.profileLayer(method, "directConvertBoolean", (java.util.function.BooleanSupplier) () -> (boolean) Context.jsToJava(cx, result, boolean.class));
            }
            return (boolean) Context.jsToJava(cx, result, boolean.class);
        }

        private void invokeVoid(Object value) {
            invokeRaw(value);
        }

        private Object invoke(Method method, Object[] values) {
            if (method.getReturnType() == void.class) {
                invokeRaw(values);
                return null;
            }

            Object result = invokeRaw(values);
            Object converted = Context.jsToJava(cx, result, method.getReturnType());
            return converted == null && method.getReturnType().isPrimitive() ? defaultValue(method.getReturnType()) : converted;
        }

        private Object invokeRaw(Object value) {
            String method = CallbackProfiler.currentMethod();
            Object arg;
            if (CallbackProfiler.enabled() && method != null) {
                arg = CallbackProfiler.profileLayer(method, "directWrapArg", () -> wrapArgument(value));
                Object[] args = args(arg);
                try (var ignored = CallbackProfiler.beginStackProbe(method, "directCallSync", describe(value))) {
                    return CallbackProfiler.profileLayer(method, "directCallSync", () -> cx.callSync(callable, topScope, thisObj, args));
                } finally {
                    args[0] = null;
                }
            }
            arg = wrapArgument(value);
            Object[] args = args(arg);
            try {
                return cx.callSync(callable, topScope, thisObj, args);
            } finally {
                args[0] = null;
            }
        }

        private Object[] args(Object arg) {
            Object[] args = arguments.get();
            if (args.length != 1) {
                args = new Object[1];
                arguments.set(args);
            }
            args[0] = arg;
            return args;
        }

        private Object invokeRaw(Object[] values) {
            Object[] args = args(values);
            try {
                return cx.callSync(callable, topScope, thisObj, args);
            } finally {
                for (int i = 0; i < values.length; i++) {
                    args[i] = null;
                }
            }
        }

        private Object[] args(Object[] values) {
            Object[] args = arguments.get();
            if (args.length != values.length) {
                args = new Object[values.length];
                arguments.set(args);
            }
            for (int i = 0; i < values.length; i++) {
                args[i] = wrapArgument(values[i]);
            }
            return args;
        }

        private Object wrapArgument(Object value) {
            if (value instanceof ContextUtils.DynamicOverrideContext<?> context) {
                if (context.getParentScope() == null) {
                    context.entityJs$attachScriptScope(
                            topScope,
                            ScriptableObject.getObjectPrototype(topScope, cx),
                            ScriptableObject.getFunctionPrototype(topScope, cx)
                    );
                }
                return context;
            }
            if (value == null || value instanceof String || value instanceof Number || value instanceof Boolean || value instanceof Scriptable) {
                return value;
            }

            if (value instanceof ICallbackWrapperCache cache) {
                Object cached = cache.entityJs$getCachedCallbackWrapper(this);
                if (cached != null) {
                    return cached;
                }

                Object wrapped = wrapFactory.wrap(cx, topScope, value, null);
                cache.entityJs$putCachedCallbackWrapper(this, wrapped);
                return wrapped;
            }

            WeakReference<Object> reference = argumentWrappers.get(value);
            Object cached = reference == null ? null : reference.get();
            if (cached != null) {
                return cached;
            }

            Object wrapped = wrapFactory.wrap(cx, topScope, value, null);
            argumentWrappers.put(value, new WeakReference<>(wrapped));
            return wrapped;
        }

        private String describe(Object value) {
            StringBuilder description = new StringBuilder()
                    .append("callable=")
                    .append(callable.getClass().getName());
            if (value == null) {
                return description.append(", arg=null").toString();
            }
            description.append(", argClass=").append(value.getClass().getName());
            if (value instanceof Entity entity) {
                description.append(", entityType=").append(entity.getType())
                        .append(", entityId=").append(entity.getId())
                        .append(", entityUuid=").append(entity.getUUID())
                        .append(", level=").append(entity.level().dimension().location())
                        .append(", clientSide=").append(entity.level().isClientSide);
                if (!entity.level().isClientSide && entity.level().getServer() != null) {
                    description.append(", serverThread=").append(entity.level().getServer().isSameThread());
                }
            }
            return description.toString();
        }
    }

    private static Object defaultValue(Class<?> type) {
        if (type == void.class) {
            return null;
        }
        if (type == boolean.class) {
            return false;
        }
        if (type == char.class) {
            return (char) 0;
        }
        if (type == byte.class) {
            return (byte) 0;
        }
        if (type == short.class) {
            return (short) 0;
        }
        if (type == int.class) {
            return 0;
        }
        if (type == long.class) {
            return 0L;
        }
        if (type == float.class) {
            return 0F;
        }
        if (type == double.class) {
            return 0D;
        }
        return null;
    }
}
