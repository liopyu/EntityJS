package net.liopyu.entityjs.util.overrides.dynamic;

import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityAccess;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class DynamicOverrideMethodCatalog {
    private static final String HIDDEN_METHOD_REASON = "Changes to this method may cause stability issues and should not be overridden";
    private static final Set<Class<?>> HIDDEN_DECLARING_CLASSES = Set.of(
            Object.class
    );
    private static final Set<Class<?>> HIDDEN_SIGNATURE_TYPES = Set.of(
            Class.class,
            ClassLoader.class,
            Module.class,
            Process.class,
            ProcessBuilder.class,
            Runtime.class,
            System.class,
            Thread.class
    );
    private static final Set<String> HIDDEN_SIGNATURE_PACKAGES = Set.of(
            "java.io",
            "java.lang.invoke",
            "java.lang.reflect",
            "java.net",
            "java.nio.file",
            "javax.script",
            "javax.tools",
            "jdk.internal",
            "com.llamalad7.mixinextras",
            "org.spongepowered.asm.mixin",
            "org.objectweb.asm",
            "sun.misc",
            "sun.reflect"
    );
    private static final Set<Class<?>> HIDDEN_METHOD_SURFACES = Set.of(
            EntityAccess.class,
            Nameable.class
    );
    private static final Set<String> HIDDEN_METHOD_NAMES = Set.of(
            "addAdditionalSaveData",
            "captureDrops",
            "createCommandSourceStack",
            "displayFireAnimation",
            "fillCrashReportCategory",
            "fixupDimensions",
            "getAddEntityPacket",
            "getCommandSenderWorld",
            "getPersistentData",
            "getServer",
            "getStringUUID",
            "getType",
            "getTypeName",
            "is",
            "isCustomNameVisible",
            "level",
            "load",
            "readAdditionalSaveData",
            "recreateFromPacket",
            "registryAccess",
            "save",
            "saveAsPassenger",
            "saveWithoutId",
            "setCustomName",
            "setCustomNameVisible",
            "setId",
            "setLevel",
            "setUUID",
            "unsetRemoved"
    );
    private static final Map<Class<? extends Entity>, Catalog> CATALOGS = new ConcurrentHashMap<>();

    private DynamicOverrideMethodCatalog() {
    }

    @HideFromJS
    public static MethodSpec resolve(Class<? extends Entity> baseClass, String key) {
        return catalog(baseClass).methods().get(key);
    }

    @HideFromJS
    public static void validateOverrideKey(Class<? extends Entity> baseClass, String key) {
        if (Modifier.isFinal(baseClass.getModifiers())) {
            throw new IllegalArgumentException("Dynamic override '" + key + "' cannot be registered because " + baseClass.getSimpleName() + " is final.");
        }
        if (resolve(baseClass, key) == null) {
            throw new IllegalArgumentException("Dynamic override '" + key + "' cannot be registered because " + rejectionReason(baseClass, key) + ".");
        }
    }

    @HideFromJS
    public static String rejectionReason(Class<? extends Entity> baseClass, String key) {
        return catalog(baseClass).ineligibleMethods().getOrDefault(key, "it is not an overridable instance method on the " + baseClass.getSimpleName() + " class tree");
    }

    @HideFromJS
    public static String[] parameterNames(Class<? extends Entity> baseClass, String key, int count) {
        MethodSpec methodSpec = resolve(baseClass, key);
        if (methodSpec != null && methodSpec.parameterNames().length == count) {
            return methodSpec.parameterNames();
        }

        String[] names = new String[count];
        for (int i = 0; i < count; i++) {
            names[i] = "arg" + i;
        }
        return names;
    }

    @HideFromJS
    public static List<String> overrideKeys(Class<? extends Entity> baseClass) {
        if (Modifier.isFinal(baseClass.getModifiers())) {
            return List.of();
        }
        return List.copyOf(catalog(baseClass).methods().keySet());
    }

    @HideFromJS
    public static List<MethodSpec> abstractMethods(Class<? extends Entity> baseClass) {
        return List.copyOf(catalog(baseClass).abstractMethods().values());
    }

    private static Catalog catalog(Class<? extends Entity> baseClass) {
        return CATALOGS.computeIfAbsent(baseClass, DynamicOverrideMethodCatalog::discoverCatalog);
    }

    private static Catalog discoverCatalog(Class<? extends Entity> baseClass) {
        Map<String, MethodSpec> methods = new LinkedHashMap<>();
        Map<String, String> ineligibleMethods = new LinkedHashMap<>();
        Map<String, MethodSpec> abstractMethods = new LinkedHashMap<>();
        Set<String> seenSignatures = new HashSet<>();
        Class<?> type = baseClass;
        while (type != null) {
            for (Method method : type.getDeclaredMethods()) {
                String key = key(method);
                if (!seenSignatures.add(key)) {
                    continue;
                }

                if (isImplementableAbstractMethod(baseClass, method)) {
                    abstractMethods.put(key, MethodSpec.from(method, key));
                }
                String reason = ineligibleReason(baseClass, method);
                if (reason == null) {
                    methods.put(key, MethodSpec.from(method, key));
                } else {
                    ineligibleMethods.put(key, reason);
                }
            }
            type = type.getSuperclass();
        }
        addInterfaceAbstractMethods(baseClass, baseClass, seenSignatures, methods, ineligibleMethods, abstractMethods);
        return new Catalog(methods, ineligibleMethods, abstractMethods);
    }

    private static String ineligibleReason(Class<?> baseClass, Method method) {
        int modifiers = method.getModifiers();
        if (method.isSynthetic() || method.isBridge()) {
            return "it is a compiler-generated bridge/synthetic method";
        }
        if (Modifier.isFinal(modifiers)) {
            return methodLabel(method) + " is final";
        }
        if (Modifier.isPrivate(modifiers)) {
            return methodLabel(method) + " is private";
        }
        if (Modifier.isStatic(modifiers)) {
            return methodLabel(method) + " is static and cannot participate in virtual dispatch";
        }
        if (isGeneratedMixinName(method.getName())) {
            return methodLabel(method) + " is a generated mixin handler method";
        }
        if (!Modifier.isPublic(modifiers)
                && !Modifier.isProtected(modifiers)
                && !method.getDeclaringClass().getPackageName().equals(baseClass.getPackageName())) {
            return methodLabel(method) + " is package-private outside the generated subclass package";
        }
        return hiddenReason(method);
    }

    private static boolean isImplementableAbstractMethod(Class<?> baseClass, Method method) {
        int modifiers = method.getModifiers();
        return Modifier.isAbstract(modifiers)
                && !method.isSynthetic()
                && !method.isBridge()
                && !Modifier.isPrivate(modifiers)
                && !Modifier.isStatic(modifiers)
                && !Modifier.isFinal(modifiers)
                && !isGeneratedMixinName(method.getName())
                && (Modifier.isPublic(modifiers)
                || Modifier.isProtected(modifiers)
                || method.getDeclaringClass().getPackageName().equals(baseClass.getPackageName()));
    }

    private static void addInterfaceAbstractMethods(Class<?> baseClass, Class<?> type, Set<String> seenSignatures, Map<String, MethodSpec> methods, Map<String, String> ineligibleMethods, Map<String, MethodSpec> abstractMethods) {
        if (type == null) {
            return;
        }
        for (Class<?> interfaceType : type.getInterfaces()) {
            addInterfaceAbstractMethods(baseClass, interfaceType, seenSignatures, methods, ineligibleMethods, abstractMethods);
            for (Method method : interfaceType.getMethods()) {
                String key = key(method);
                if (isConcreteInterfaceMethod(method)) {
                    seenSignatures.add(key);
                    methods.remove(key);
                    ineligibleMethods.remove(key);
                    abstractMethods.remove(key);
                    continue;
                }
                if (!seenSignatures.add(key) || !isImplementableAbstractMethod(baseClass, method)) {
                    continue;
                }
                abstractMethods.put(key, MethodSpec.from(method, key));
                String reason = ineligibleReason(baseClass, method);
                if (reason == null) {
                    methods.put(key, MethodSpec.from(method, key));
                } else {
                    ineligibleMethods.put(key, reason);
                }
            }
        }
        addInterfaceAbstractMethods(baseClass, type.getSuperclass(), seenSignatures, methods, ineligibleMethods, abstractMethods);
    }

    private static boolean isConcreteInterfaceMethod(Method method) {
        int modifiers = method.getModifiers();
        return method.getDeclaringClass().isInterface()
                && !Modifier.isAbstract(modifiers)
                && !Modifier.isStatic(modifiers);
    }

    private static String hiddenReason(Method method) {
        return isIntentionallyHidden(method)
                ? methodLabel(method) + " is intentionally hidden from dynamic overrides. " + HIDDEN_METHOD_REASON
                : null;
    }

    private static String methodLabel(Method method) {
        return method.getDeclaringClass().getSimpleName() + "#" + method.getName();
    }

    private static boolean isIntentionallyHidden(Method method) {
        String name = method.getName();
        return HIDDEN_DECLARING_CLASSES.contains(method.getDeclaringClass())
                || hasHiddenSignatureType(method)
                || isHiddenSurfaceMethod(method)
                || HIDDEN_METHOD_NAMES.contains(name)
                || isEntityJSInternalName(name)
                || name.startsWith("getKnown")
                || name.startsWith("setRequiresPrecisePosition")
                || name.startsWith("getRequiresPrecisePosition")
                || name.contains("SynchedData")
                || name.contains("SyncedData")
                || name.contains("EntityData");
    }

    private static boolean hasHiddenSignatureType(Method method) {
        if (isHiddenSignatureType(method.getReturnType())) {
            return true;
        }
        for (Class<?> parameterType : method.getParameterTypes()) {
            if (isHiddenSignatureType(parameterType)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isHiddenSignatureType(Class<?> type) {
        while (type.isArray()) {
            type = type.getComponentType();
        }
        if (type.isPrimitive()) {
            return false;
        }
        if (HIDDEN_SIGNATURE_TYPES.contains(type)) {
            return true;
        }
        String packageName = type.getPackageName();
        for (String hiddenPackage : HIDDEN_SIGNATURE_PACKAGES) {
            if (packageName.equals(hiddenPackage) || packageName.startsWith(hiddenPackage + ".")) {
                return true;
            }
        }
        return false;
    }

    private static boolean isEntityJSInternalName(String name) {
        return name.regionMatches(true, 0, "entityjs$", 0, "entityjs$".length());
    }

    private static boolean isGeneratedMixinName(String name) {
        return name.startsWith("handler$")
                || name.startsWith("wrapOperation$")
                || name.startsWith("wrapWithCondition$")
                || name.startsWith("modifyExpressionValue$")
                || name.startsWith("modifyReturnValue$")
                || name.startsWith("redirect$");
    }

    private static boolean isHiddenSurfaceMethod(Method method) {
        for (Class<?> surface : HIDDEN_METHOD_SURFACES) {
            if (declaresCompatibleMethod(surface, method)) {
                return true;
            }
        }
        return false;
    }

    private static boolean declaresCompatibleMethod(Class<?> surface, Method method) {
        for (Method surfaceMethod : surface.getMethods()) {
            if (surfaceMethod.getName().equals(method.getName()) && Arrays.equals(surfaceMethod.getParameterTypes(), method.getParameterTypes())) {
                return true;
            }
        }
        return false;
    }

    private static String key(Method method) {
        StringBuilder builder = new StringBuilder(method.getName()).append('(');
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(displayType(parameterTypes[i]));
        }
        return builder.append(')').toString();
    }

    private static String displayType(Class<?> type) {
        String canonicalName = type.getCanonicalName();
        return canonicalName == null ? type.getName() : canonicalName;
    }

    static String descriptor(Method method) {
        StringBuilder builder = new StringBuilder("(");
        for (Class<?> parameterType : method.getParameterTypes()) {
            builder.append(descriptor(parameterType));
        }
        return builder.append(')').append(descriptor(method.getReturnType())).toString();
    }

    private static String descriptor(Class<?> type) {
        if (type == void.class) {
            return "V";
        } else if (type == boolean.class) {
            return "Z";
        } else if (type == byte.class) {
            return "B";
        } else if (type == char.class) {
            return "C";
        } else if (type == short.class) {
            return "S";
        } else if (type == int.class) {
            return "I";
        } else if (type == long.class) {
            return "J";
        } else if (type == float.class) {
            return "F";
        } else if (type == double.class) {
            return "D";
        } else if (type.isArray()) {
            return type.getName().replace('.', '/');
        }
        return "L" + internalName(type) + ";";
    }

    private static String internalName(Class<?> type) {
        return type.getName().replace('.', '/');
    }

    private static String[] parameterNames(Method method) {
        Parameter[] parameters = method.getParameters();
        String[] names = new String[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            String reflectedName = parameters[i].isNamePresent() ? parameters[i].getName() : null;
            names[i] = isUsefulParameterName(reflectedName)
                    ? reflectedName
                    : "arg" + i;
        }
        return ParchmentParameterNameIndex.parameterNames(method, names);
    }

    private static boolean isUsefulParameterName(String name) {
        return name != null && !name.matches("arg\\d+") && !name.matches("p_\\d+_?");
    }

    private static String bridgeName(String key) {
        StringBuilder builder = new StringBuilder("entityjs$super$");
        for (int i = 0; i < key.length(); i++) {
            char character = key.charAt(i);
            builder.append(Character.isLetterOrDigit(character) || character == '_' || character == '$' ? character : '$');
        }
        return builder.append('$').append(Integer.toUnsignedString(key.hashCode())).toString();
    }

    private record Catalog(Map<String, MethodSpec> methods, Map<String, String> ineligibleMethods, Map<String, MethodSpec> abstractMethods) {
    }

    public record MethodSpec(String key, String name, String descriptor, Class<?> returnType, Class<?>[] parameterTypes, String[] parameterNames, String superBridgeName, int access, boolean abstractMethod) {
        static MethodSpec from(Method method, String key) {
            return new MethodSpec(
                    key,
                    method.getName(),
                    DynamicOverrideMethodCatalog.descriptor(method),
                    method.getReturnType(),
                    method.getParameterTypes(),
                    DynamicOverrideMethodCatalog.parameterNames(method),
                    DynamicOverrideMethodCatalog.bridgeName(key),
                    access(method),
                    Modifier.isAbstract(method.getModifiers())
            );
        }

        private static int access(Method method) {
            int modifiers = method.getModifiers();
            if (Modifier.isPublic(modifiers)) {
                return Modifier.PUBLIC;
            }
            if (Modifier.isProtected(modifiers)) {
                return Modifier.PROTECTED;
            }
            return 0;
        }

        public String returnDescriptor() {
            return DynamicOverrideMethodCatalog.descriptor(returnType);
        }

        public String returnInternalName() {
            return internalName(returnType);
        }

        public int maxLocals() {
            int maxLocals = 1;
            for (Class<?> parameterType : parameterTypes) {
                maxLocals += parameterType == long.class || parameterType == double.class ? 2 : 1;
            }
            return maxLocals;
        }
    }
}
