package net.liopyu.entityjs.util.overrides.dynamic;

import dev.latvian.mods.rhino.util.HideFromJS;
import net.liopyu.entityjs.builders.misc.CustomEntityBuilder;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class DynamicOverrideEntityFactory implements Opcodes {
    private static final String ENTITY_TYPE_DESCRIPTOR = Type.getDescriptor(EntityType.class);
    private static final String LEVEL_DESCRIPTOR = Type.getDescriptor(Level.class);
    private static final String ENTITY_DESCRIPTOR = Type.getDescriptor(Entity.class);
    private static final String RUNTIME_OWNER = Type.getInternalName(DynamicOverrideRuntime.class);
    private static final String OBJECT_DESCRIPTOR = Type.getDescriptor(Object.class);
    private static final String OBJECT_ARRAY_DESCRIPTOR = Type.getDescriptor(Object[].class);
    private static final Map<String, Constructor<? extends Entity>> CONSTRUCTORS = new ConcurrentHashMap<>();
    private static final AtomicInteger DYNAMIC_TYPE_COUNTER = new AtomicInteger();

    private DynamicOverrideEntityFactory() {
    }

    @HideFromJS
    public static Entity create(CustomEntityBuilder builder, EntityType<?> entityType, Level level) {
        try {
            DynamicOverrideRuntime.register(entityType, builder);
            Constructor<? extends Entity> constructor = dynamicConstructor(builder);
            return constructor.newInstance(entityType, level);
        } catch (Exception e) {
            throw new RuntimeException("Failed to dynamically instantiate overridden custom entity: " + builder.id, e);
        }
    }

    private static Constructor<? extends Entity> dynamicConstructor(CustomEntityBuilder builder) {
        String key = constructorKey(builder);
        return CONSTRUCTORS.computeIfAbsent(key, ignored -> defineDynamicConstructor(builder));
    }

    private static Constructor<? extends Entity> defineDynamicConstructor(CustomEntityBuilder builder) {
        try {
            Class<? extends Entity> baseClass = builder.getEntityClass();
            String baseInternalName = Type.getInternalName(baseClass);
            String dynamicTypeName = baseClass.getName() + "$EntityJSDynamicOverride$" + DYNAMIC_TYPE_COUNTER.incrementAndGet();
            String dynamicTypeInternalName = dynamicTypeName.replace('.', '/');
            List<DynamicOverrideMethodCatalog.MethodSpec> overrideMethods = resolvedMethods(builder);
            List<DynamicOverrideMethodCatalog.MethodSpec> defaultAbstractMethods = defaultAbstractMethods(builder, overrideMethods);
            warnDefaultAbstractMethods(builder, defaultAbstractMethods);
            byte[] bytes = buildDynamicTypeBytes(baseInternalName, dynamicTypeInternalName, overrideMethods, defaultAbstractMethods);
            Class<?> dynamicType = MethodHandles.privateLookupIn(baseClass, MethodHandles.lookup()).defineClass(bytes);
            Constructor<? extends Entity> constructor = dynamicType.asSubclass(Entity.class).getDeclaredConstructor(EntityType.class, Level.class);
            constructor.setAccessible(true);
            return constructor;
        } catch (Throwable throwable) {
            throw new RuntimeException("Failed to prepare dynamic override entity type for entity: " + builder.id, throwable);
        }
    }

    private static byte[] buildDynamicTypeBytes(String baseInternalName, String dynamicTypeInternalName, List<DynamicOverrideMethodCatalog.MethodSpec> methods, List<DynamicOverrideMethodCatalog.MethodSpec> defaultAbstractMethods) {
        ClassWriter dynamicType = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        dynamicType.visit(V17, ACC_PUBLIC | ACC_SUPER, dynamicTypeInternalName, null, baseInternalName, null);
        defineConstructor(dynamicType, baseInternalName);
        for (DynamicOverrideMethodCatalog.MethodSpec methodSpec : methods) {
            defineOverride(dynamicType, methodSpec);
            if (!methodSpec.abstractMethod()) {
                defineSuperBridge(dynamicType, baseInternalName, methodSpec);
            }
        }
        for (DynamicOverrideMethodCatalog.MethodSpec methodSpec : defaultAbstractMethods) {
            defineDefaultStub(dynamicType, methodSpec);
        }
        dynamicType.visitEnd();
        return dynamicType.toByteArray();
    }

    private static void defineConstructor(ClassWriter dynamicType, String baseInternalName) {
        MethodVisitor method = dynamicType.visitMethod(ACC_PUBLIC, "<init>", "(" + ENTITY_TYPE_DESCRIPTOR + LEVEL_DESCRIPTOR + ")V", null, null);
        method.visitCode();
        method.visitVarInsn(ALOAD, 0);
        method.visitVarInsn(ALOAD, 1);
        method.visitVarInsn(ALOAD, 2);
        method.visitMethodInsn(INVOKESPECIAL, baseInternalName, "<init>", "(" + ENTITY_TYPE_DESCRIPTOR + LEVEL_DESCRIPTOR + ")V", false);
        method.visitInsn(RETURN);
        method.visitMaxs(0, 0);
        method.visitEnd();
    }

    private static void defineOverride(ClassWriter dynamicType, DynamicOverrideMethodCatalog.MethodSpec methodSpec) {
        MethodVisitor method = dynamicType.visitMethod(overrideAccess(methodSpec), methodSpec.name(), methodSpec.descriptor(), null, null);
        method.visitCode();
        method.visitVarInsn(ALOAD, 0);
        method.visitLdcInsn(methodSpec.key());
        method.visitLdcInsn(methodSpec.abstractMethod() ? "" : methodSpec.superBridgeName());
        buildArgumentArray(method, methodSpec);
        method.visitMethodInsn(INVOKESTATIC, RUNTIME_OWNER, runtimeMethodName(methodSpec.returnType()), runtimeDescriptor(methodSpec.returnType()), false);
        emitReturn(method, methodSpec.returnType());
        method.visitMaxs(0, 0);
        method.visitEnd();
    }

    private static void defineDefaultStub(ClassWriter dynamicType, DynamicOverrideMethodCatalog.MethodSpec methodSpec) {
        MethodVisitor method = dynamicType.visitMethod(overrideAccess(methodSpec), methodSpec.name(), methodSpec.descriptor(), null, null);
        method.visitCode();
        emitDefaultValue(method, methodSpec.returnType());
        emitReturn(method, methodSpec.returnType());
        method.visitMaxs(0, 0);
        method.visitEnd();
    }

    private static void defineSuperBridge(ClassWriter dynamicType, String baseInternalName, DynamicOverrideMethodCatalog.MethodSpec methodSpec) {
        MethodVisitor method = dynamicType.visitMethod(ACC_PUBLIC | ACC_SYNTHETIC, methodSpec.superBridgeName(), methodSpec.descriptor(), null, null);
        method.visitCode();
        method.visitVarInsn(ALOAD, 0);
        loadArguments(method, methodSpec);
        method.visitMethodInsn(INVOKESPECIAL, baseInternalName, methodSpec.name(), methodSpec.descriptor(), false);
        emitReturn(method, methodSpec.returnType());
        method.visitMaxs(0, 0);
        method.visitEnd();
    }

    private static void buildArgumentArray(MethodVisitor method, DynamicOverrideMethodCatalog.MethodSpec methodSpec) {
        Class<?>[] parameterTypes = methodSpec.parameterTypes();
        pushInt(method, parameterTypes.length);
        method.visitTypeInsn(ANEWARRAY, Type.getInternalName(Object.class));
        int localIndex = 1;
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            method.visitInsn(DUP);
            pushInt(method, i);
            method.visitVarInsn(loadOpcode(parameterType), localIndex);
            box(method, parameterType);
            method.visitInsn(AASTORE);
            localIndex += localSize(parameterType);
        }
    }

    private static void loadArguments(MethodVisitor method, DynamicOverrideMethodCatalog.MethodSpec methodSpec) {
        int localIndex = 1;
        for (Class<?> parameterType : methodSpec.parameterTypes()) {
            method.visitVarInsn(loadOpcode(parameterType), localIndex);
            localIndex += localSize(parameterType);
        }
    }

    private static void emitReturn(MethodVisitor method, Class<?> returnType) {
        if (returnType == void.class) {
            method.visitInsn(RETURN);
        } else if (returnType == boolean.class || returnType == byte.class || returnType == char.class || returnType == short.class || returnType == int.class) {
            method.visitInsn(IRETURN);
        } else if (returnType == long.class) {
            method.visitInsn(LRETURN);
        } else if (returnType == float.class) {
            method.visitInsn(FRETURN);
        } else if (returnType == double.class) {
            method.visitInsn(DRETURN);
        } else {
            method.visitTypeInsn(CHECKCAST, checkcastName(returnType));
            method.visitInsn(ARETURN);
        }
    }

    private static void emitDefaultValue(MethodVisitor method, Class<?> returnType) {
        if (returnType == void.class) {
            return;
        } else if (returnType == long.class) {
            method.visitInsn(LCONST_0);
        } else if (returnType == float.class) {
            method.visitInsn(FCONST_0);
        } else if (returnType == double.class) {
            method.visitInsn(DCONST_0);
        } else if (returnType.isPrimitive()) {
            method.visitInsn(ICONST_0);
        } else {
            method.visitInsn(ACONST_NULL);
        }
    }

    private static void box(MethodVisitor method, Class<?> type) {
        if (!type.isPrimitive()) {
            return;
        }
        if (type == boolean.class) {
            method.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Boolean.class), "valueOf", "(Z)Ljava/lang/Boolean;", false);
        } else if (type == byte.class) {
            method.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Byte.class), "valueOf", "(B)Ljava/lang/Byte;", false);
        } else if (type == char.class) {
            method.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Character.class), "valueOf", "(C)Ljava/lang/Character;", false);
        } else if (type == short.class) {
            method.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Short.class), "valueOf", "(S)Ljava/lang/Short;", false);
        } else if (type == int.class) {
            method.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Integer.class), "valueOf", "(I)Ljava/lang/Integer;", false);
        } else if (type == long.class) {
            method.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Long.class), "valueOf", "(J)Ljava/lang/Long;", false);
        } else if (type == float.class) {
            method.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Float.class), "valueOf", "(F)Ljava/lang/Float;", false);
        } else if (type == double.class) {
            method.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Double.class), "valueOf", "(D)Ljava/lang/Double;", false);
        }
    }

    private static String runtimeMethodName(Class<?> returnType) {
        if (returnType == void.class) {
            return "invokeVoid";
        } else if (returnType == boolean.class) {
            return "invokeBoolean";
        } else if (returnType == byte.class) {
            return "invokeByte";
        } else if (returnType == char.class) {
            return "invokeChar";
        } else if (returnType == short.class) {
            return "invokeShort";
        } else if (returnType == int.class) {
            return "invokeInt";
        } else if (returnType == long.class) {
            return "invokeLong";
        } else if (returnType == float.class) {
            return "invokeFloat";
        } else if (returnType == double.class) {
            return "invokeDouble";
        }
        return "invokeObject";
    }

    private static String runtimeDescriptor(Class<?> returnType) {
        return "(" + ENTITY_DESCRIPTOR + Type.getDescriptor(String.class) + Type.getDescriptor(String.class) + OBJECT_ARRAY_DESCRIPTOR + ")" + (returnType.isPrimitive() ? Type.getDescriptor(returnType) : OBJECT_DESCRIPTOR);
    }

    private static int overrideAccess(DynamicOverrideMethodCatalog.MethodSpec methodSpec) {
        return methodSpec.access();
    }

    private static int loadOpcode(Class<?> type) {
        if (type == long.class) {
            return LLOAD;
        }
        if (type == float.class) {
            return FLOAD;
        }
        if (type == double.class) {
            return DLOAD;
        }
        if (type.isPrimitive()) {
            return ILOAD;
        }
        return ALOAD;
    }

    private static int localSize(Class<?> type) {
        return type == long.class || type == double.class ? 2 : 1;
    }

    private static void pushInt(MethodVisitor method, int value) {
        if (value >= -1 && value <= 5) {
            method.visitInsn(ICONST_0 + value);
        } else if (value <= Byte.MAX_VALUE) {
            method.visitIntInsn(BIPUSH, value);
        } else if (value <= Short.MAX_VALUE) {
            method.visitIntInsn(SIPUSH, value);
        } else {
            method.visitLdcInsn(value);
        }
    }

    private static String checkcastName(Class<?> type) {
        return type.isArray() ? Type.getDescriptor(type) : Type.getInternalName(type);
    }

    private static List<DynamicOverrideMethodCatalog.MethodSpec> resolvedMethods(CustomEntityBuilder builder) {
        List<DynamicOverrideMethodCatalog.MethodSpec> methods = new ArrayList<>();
        for (String methodKey : builder.dynamicOverrideKeys()) {
            DynamicOverrideMethodCatalog.MethodSpec methodSpec = DynamicOverrideMethodCatalog.resolve(builder.getEntityClass(), methodKey);
            if (methodSpec == null) {
                throw new IllegalArgumentException("Dynamic override '" + methodKey + "' cannot be registered because " + DynamicOverrideMethodCatalog.rejectionReason(builder.getEntityClass(), methodKey) + ".");
            }
            methods.add(methodSpec);
        }
        methods.sort(Comparator.comparing(DynamicOverrideMethodCatalog.MethodSpec::key));
        return methods;
    }

    private static List<DynamicOverrideMethodCatalog.MethodSpec> defaultAbstractMethods(CustomEntityBuilder builder, List<DynamicOverrideMethodCatalog.MethodSpec> overrideMethods) {
        if (!Modifier.isAbstract(builder.getEntityClass().getModifiers())) {
            return List.of();
        }
        List<DynamicOverrideMethodCatalog.MethodSpec> methods = new ArrayList<>();
        List<String> overrideKeys = overrideMethods.stream().map(DynamicOverrideMethodCatalog.MethodSpec::key).toList();
        for (DynamicOverrideMethodCatalog.MethodSpec methodSpec : DynamicOverrideMethodCatalog.abstractMethods(builder.getEntityClass())) {
            if (!overrideKeys.contains(methodSpec.key())) {
                methods.add(methodSpec);
            }
        }
        methods.sort(Comparator.comparing(DynamicOverrideMethodCatalog.MethodSpec::key));
        return methods;
    }

    private static void warnDefaultAbstractMethods(CustomEntityBuilder builder, List<DynamicOverrideMethodCatalog.MethodSpec> methods) {
        if (methods.isEmpty()) {
            return;
        }
        StringBuilder message = new StringBuilder("[EntityJS]: Custom entity ")
                .append(builder.id)
                .append(" uses abstract base class ")
                .append(builder.getEntityClass().getName())
                .append(". The following abstract methods were not overridden and will use default return values:");
        for (DynamicOverrideMethodCatalog.MethodSpec methodSpec : methods) {
            message.append("\n - ")
                    .append(methodSpec.key())
                    .append(" -> ")
                    .append(defaultValueLabel(methodSpec.returnType()));
        }
        EntityJSHelperClass.logWarningMessageOnce(message.toString());
    }

    private static String defaultValueLabel(Class<?> returnType) {
        if (returnType == void.class) {
            return "no-op";
        }
        if (returnType == boolean.class) {
            return "false";
        }
        if (returnType == char.class) {
            return "\\0";
        }
        if (returnType == long.class) {
            return "0L";
        }
        if (returnType == float.class) {
            return "0F";
        }
        if (returnType == double.class) {
            return "0D";
        }
        if (returnType.isPrimitive()) {
            return "0";
        }
        return "null";
    }

    private static String constructorKey(CustomEntityBuilder builder) {
        List<String> methodKeys = new ArrayList<>(builder.dynamicOverrideKeys());
        methodKeys.sort(String::compareTo);
        return builder.getEntityClass().getName() + "|" + Modifier.isFinal(builder.getEntityClass().getModifiers()) + "|" + String.join("|", methodKeys);
    }
}
