package net.liopyu.entityjs.events;

import com.mojang.datafixers.util.Pair;
import com.probejs.docs.DocCompiler;
import com.probejs.features.plugin.DocGenerationEventJS;
import com.probejs.jdoc.document.DocumentClass;
import com.probejs.jdoc.document.DocumentMethod;
import com.probejs.jdoc.property.PropertyParam;
import com.probejs.jdoc.property.PropertyType;
import dev.latvian.mods.kubejs.registry.RegistryEventJS;
import net.liopyu.entityjs.builders.living.BaseLivingEntityBuilder;
import net.liopyu.entityjs.builders.misc.CustomEntityBuilder;
import net.liopyu.entityjs.builders.misc.CustomEntityJSBuilder;
import net.liopyu.entityjs.builders.modification.ModifyEntityBuilder;
import net.liopyu.entityjs.builders.modification.ModifyLivingEntityBuilder;
import net.liopyu.entityjs.builders.modification.ModifyMobBuilder;
import net.liopyu.entityjs.builders.modification.ModifyPathfinderMobBuilder;
import net.liopyu.entityjs.builders.modification.ModifyProjectileBuilder;
import net.liopyu.entityjs.builders.nonliving.BaseEntityBuilder;
import net.liopyu.entityjs.builders.nonliving.BaseNonAnimatableEntityBuilder;
import net.liopyu.entityjs.typings.EntityJSTypingNames;
import net.liopyu.entityjs.typings.RuntimeClassNameCatalog;
import net.liopyu.entityjs.util.EntityJSUtils;
import net.liopyu.entityjs.util.implementation.IRegistryJS;
import net.liopyu.entityjs.util.overrides.dynamic.DynamicOverrideMethodCatalog;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.projectile.Projectile;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;

public final class EntityJSBuiltinDocs {
    private static final String OVERRIDE_METHOD_KEY = "Special.EntityJSOverrideMethodKey";
    private static final String OVERRIDE_METHOD_KEY_FOR_ENTITY = "Special.EntityJSOverrideMethodKeyForEntity";
    private static final String OVERRIDE_METHOD_KEY_FOR_ENTITY_CLASS = "Special.EntityJSOverrideMethodKeyForEntityClass";
    private static final String OVERRIDE_METHOD_KEY_FOR_ENTITY_CLASS_NAME = "Special.EntityJSOverrideMethodKeyForEntityClassName";
    private static final String ENTITY_FOR_ENTITY_CLASS = "Special.EntityJSEntityForEntityClass";
    private static final String ENTITY_FOR_ENTITY_CLASS_NAME = "Special.EntityJSEntityForEntityClassName";
    private static final String ENTITY_CLASS_NAME = "Special.EntityJSEntityClassName";
    private static final String DYNAMIC_OVERRIDE_CONTEXT = "Special.EntityJSDynamicOverrideContext";
    private static final String DYNAMIC_OVERRIDE_CONTEXT_FOR = "Special.EntityJSDynamicOverrideContextFor";
    private static final String DYNAMIC_OVERRIDE_ARGS_BY_METHOD_KEY = "Special.EntityJSDynamicOverrideArgsByMethodKey";
    private static final String CUSTOM_ENTITY_BUILDER_WITH_OVERRIDES = "Special.EntityJSCustomEntityBuilderWithOverrides";
    private static final String MODIFY_BUILDER_FOR_ENTITY = "Special.EntityJSModifyBuilderForEntity";
    private static final String MODIFY_BUILDER_FOR_ENTITY_CLASS = "Special.EntityJSModifyBuilderForEntityClass";
    private static final String MODIFY_BUILDER_FOR_ENTITY_CLASS_NAME = "Special.EntityJSModifyBuilderForEntityClassName";
    private static final String RENDERER_CLASS_NAME = EntityJSTypingNames.SPECIAL_RENDERER_CLASS_NAME;
    private static final Set<DocGenerationEventJS> REGISTERED_EVENTS = Collections.newSetFromMap(new WeakHashMap<>());

    private EntityJSBuiltinDocs() {
    }

    public static void register(DocGenerationEventJS event) {
        if (!REGISTERED_EVENTS.add(event)) {
            return;
        }
        captureEntityJSClasses();
        ProbeIndex index = buildProbeIndex();
        event.specialType("EntityJSOverrideMethodKey", rawUnion(index.overrideMethodKeys()));
        for (Map.Entry<Class<? extends Entity>, String> entry : index.overrideMethodKeyTypes.entrySet()) {
            event.specialType(entry.getValue(), List.of(overrideMethodKeyType(entry.getKey(), index)));
        }
        event.specialType("EntityJSOverrideMethodKeyForEntity<T>", List.of(overrideMethodKeyForEntity(index)));
        event.specialType("EntityJSOverrideMethodKeyForEntityClass<T>", List.of(overrideMethodKeyForEntityClass(index)));
        event.specialType("EntityJSOverrideMethodKeyForEntityClassName<T extends " + ENTITY_CLASS_NAME + ">", List.of("T extends keyof Special.EntityJSOverrideMethodKeyByEntityClassName ? Special.EntityJSOverrideMethodKeyByEntityClassName[T] : " + OVERRIDE_METHOD_KEY));
        event.specialType("EntityJSOverrideMethodKeyByEntityClassName", List.of(overrideMethodKeyByEntityClassName(index)));
        event.specialType("EntityJSEntityForEntityClass<T>", List.of(entityForEntityClass(index)));
        event.specialType("EntityJSEntityForEntityClassName<T extends " + ENTITY_CLASS_NAME + ">", List.of(entityForEntityClassName(index)));
        event.specialType("EntityJSEntityClassName", rawUnion(index.entityClassNames()));
        event.specialType(EntityJSTypingNames.RENDERER_CLASS_NAME, rawUnion(index.rendererClassNames()));
        event.specialType("EntityJSDynamicOverrideContext<E extends Internal.Entity = Internal.Entity>", List.of(dynamicOverrideContext()));
        event.specialType("EntityJSDynamicOverrideArgsByMethodKey", List.of(dynamicOverrideArgsByMethodKey(index)));
        event.specialType("EntityJSDynamicOverrideContextFor<K extends string, E extends Internal.Entity = Internal.Entity>", List.of(dynamicOverrideContextFor()));
        event.specialType("EntityJSCustomEntityBuilderWithOverrides<K extends string, E extends Internal.Entity = Internal.Entity>", List.of(customEntityBuilderWithOverrides()));
        event.specialType("EntityJSModifyBuilderForEntity<T>", List.of(modifyBuilderForEntity()));
        event.specialType("EntityJSModifyBuilderForEntityClass<T>", List.of(modifyBuilderForEntityClass(index)));
        event.specialType("EntityJSModifyBuilderForEntityClassName<T extends " + ENTITY_CLASS_NAME + ">", List.of(modifyBuilderForEntityClassName()));
        for (ModifyBuilderGroup group : index.modifyBuilderGroups().values()) {
            event.specialType(group.entityTypeAlias, rawUnion(group.entityTypeIds));
            event.specialType(group.entityClassNameAlias, rawUnion(group.entityClassNames));
        }

        event.transformDocument(CustomEntityBuilder.class, EntityJSBuiltinDocs::patchCustomEntityBuilder);
        event.transformDocument(EntityModificationEventJS.class, EntityJSBuiltinDocs::patchEntityModificationEvent);
        event.transformDocument(RegistryEventJS.class, EntityJSBuiltinDocs::patchRegistryCreateCustom);
        event.transformDocument(IRegistryJS.class, EntityJSBuiltinDocs::patchRegistryCreateCustom);
    }

    private static void captureEntityJSClasses() {
        DocCompiler.CapturedClasses.capturedJavaClasses.addAll(List.of(
                CustomEntityBuilder.class,
                CustomEntityJSBuilder.class,
                EntityModificationEventJS.class,
                ModifyEntityBuilder.class,
                ModifyLivingEntityBuilder.class,
                ModifyMobBuilder.class,
                ModifyPathfinderMobBuilder.class,
                ModifyProjectileBuilder.class,
                BaseLivingEntityBuilder.class,
                BaseEntityBuilder.class,
                BaseNonAnimatableEntityBuilder.class
        ));
    }

    private static void patchCustomEntityBuilder(DocumentClass documentClass) {
        removeMethodsNamed(documentClass, "setRendererClass");
        addClientClassNameOverloads(documentClass, "setRendererClass", "entityRendererClassName", "entityRendererClass", RENDERER_CLASS_NAME);
        for (DocumentMethod method : documentClass.methods) {
            if ("override".equals(method.name) && !method.params.isEmpty()) {
                replaceParam(method, 0, "methodKey", nativeType(OVERRIDE_METHOD_KEY));
            }
        }
    }

    private static void patchEntityModificationEvent(DocumentClass documentClass) {
        removeMethodsNamed(documentClass, "modify");
        for (ModifyBuilderGroup group : buildProbeIndex().modifyBuilderGroups().values()) {
            addMethod(documentClass, method(
                    "modify",
                    voidType(),
                    param("entityType", nativeType("Special." + group.entityTypeAlias)),
                    param("modifyBuilder", builderCallback(clazz(group.builderClass)))
            ));
        }
        addMethod(documentClass, method(
                "modify",
                List.of(nativeType("T extends Internal.Entity")),
                voidType(),
                param("entityType", nativeType("Internal.EntityType<T>")),
                param("modifyBuilder", builderCallback(nativeType(MODIFY_BUILDER_FOR_ENTITY + "<T>")))
        ));
    }

    private static void patchRegistryCreateCustom(DocumentClass documentClass) {
        removeMethodsNamed(documentClass, "createCustom");
        for (ModifyBuilderGroup group : buildProbeIndex().modifyBuilderGroups().values()) {
            addMethod(documentClass, method(
                    "createCustom",
                    List.of(nativeType("T extends Special." + group.entityClassNameAlias)),
                    nativeType(CUSTOM_ENTITY_BUILDER_WITH_OVERRIDES + "<" + OVERRIDE_METHOD_KEY_FOR_ENTITY_CLASS_NAME + "<T>, " + ENTITY_FOR_ENTITY_CLASS_NAME + "<T>>"),
                    param("id", nativeType("string")),
                    param("entityClass", nativeType("T")),
                    param("modifyBuilder", builderCallback(clazz(group.builderClass)))
            ));
        }
        addMethod(documentClass, method(
                "createCustom",
                List.of(nativeType("T extends " + ENTITY_CLASS_NAME)),
                nativeType(CUSTOM_ENTITY_BUILDER_WITH_OVERRIDES + "<" + OVERRIDE_METHOD_KEY_FOR_ENTITY_CLASS_NAME + "<T>, " + ENTITY_FOR_ENTITY_CLASS_NAME + "<T>>"),
                param("id", nativeType("string")),
                param("entityClass", nativeType("T")),
                param("modifyBuilder", builderCallback(nativeType(MODIFY_BUILDER_FOR_ENTITY_CLASS_NAME + "<T>")))
        ));
        addMethod(documentClass, method(
                "createCustom",
                List.of(nativeType("T")),
                nativeType(CUSTOM_ENTITY_BUILDER_WITH_OVERRIDES + "<" + OVERRIDE_METHOD_KEY_FOR_ENTITY_CLASS + "<T>, " + ENTITY_FOR_ENTITY_CLASS + "<T>>"),
                param("id", nativeType("string")),
                param("entityClass", nativeType("T extends Internal.Class<any> ? T : never")),
                param("modifyBuilder", builderCallback(nativeType(MODIFY_BUILDER_FOR_ENTITY_CLASS + "<T>")))
        ));
        addMethod(documentClass, method(
                "createCustom",
                List.of(nativeType("T")),
                nativeType(CUSTOM_ENTITY_BUILDER_WITH_OVERRIDES + "<" + OVERRIDE_METHOD_KEY_FOR_ENTITY_CLASS + "<T>, " + ENTITY_FOR_ENTITY_CLASS + "<T>>"),
                param("id", nativeType("string")),
                param("entityClass", nativeType("T extends string | Internal.Class<any> ? never : T")),
                param("modifyBuilder", builderCallback(nativeType(MODIFY_BUILDER_FOR_ENTITY_CLASS + "<T>")))
        ));
    }

    private static DocumentMethod method(String name, PropertyType<?> returnType, PropertyParam... params) {
        DocumentMethod method = new DocumentMethod();
        method.name = name;
        method.returns = returnType;
        method.params.addAll(List.of(params));
        return method;
    }

    private static DocumentMethod method(String name, List<PropertyType<?>> typeParams, PropertyType<?> returnType, PropertyParam... params) {
        DocumentMethod method = method(name, returnType, params);
        method.variables.addAll(typeParams);
        return method;
    }

    private static PropertyParam param(String name, PropertyType<?> type) {
        return new PropertyParam(name, type, false);
    }

    private static void replaceParam(DocumentMethod method, int index, String name, PropertyType<?> type) {
        boolean varArg = method.params.size() > index && method.params.get(index).isVarArg();
        method.params.set(index, new PropertyParam(name, type, varArg));
    }

    private static void addMethod(DocumentClass documentClass, DocumentMethod method) {
        documentClass.methods.remove(method);
        documentClass.methods.add(method);
    }

    private static void addClientClassNameOverloads(DocumentClass documentClass, String methodName, String classNameParam, String classParam, String classNameType) {
        addMethod(documentClass, method(
                methodName,
                clazz(CustomEntityBuilder.class),
                param(classNameParam, nativeType(classNameType))
        ));
        addMethod(documentClass, method(
                methodName,
                clazz(CustomEntityBuilder.class),
                param(classNameParam, nativeType("string & {}"))
        ));
        addMethod(documentClass, method(
                methodName,
                clazz(CustomEntityBuilder.class),
                param(classParam, nativeType("Internal.Class<any>"))
        ));
    }

    private static void removeMethodsNamed(DocumentClass documentClass, String name) {
        documentClass.methods.removeIf(method -> name.equals(method.name) || method.name.startsWith(name + "("));
    }

    private static PropertyType<?> builderCallback(PropertyType<?> builderType) {
        return new PropertyType.JSLambda(List.of(Pair.of("builder", builderType)), voidType());
    }

    private static PropertyType<?> clazz(Class<?> type) {
        return new PropertyType.Clazz(type);
    }

    private static PropertyType<?> nativeType(String type) {
        return new PropertyType.Native(type);
    }

    private static PropertyType<?> voidType() {
        return nativeType("void");
    }

    private static PropertyType<?> parameterized(PropertyType<?> base, PropertyType<?>... params) {
        return new PropertyType.Parameterized(base, List.of(params));
    }

    private static List<Object> rawUnion(Collection<String> values) {
        if (values.isEmpty()) {
            return List.of("never");
        }
        return values.stream()
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .map(EntityJSBuiltinDocs::quote)
                .map(Object.class::cast)
                .toList();
    }

    private static String overrideMethodKeyType(Class<? extends Entity> entityClass, ProbeIndex index) {
        Set<String> localKeys = index.overrideKeysByClass.getOrDefault(entityClass, Set.of());
        Class<? extends Entity> parentClass = index.overrideParentByClass.get(entityClass);
        List<String> types = new ArrayList<>(localKeys.stream()
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .map(EntityJSBuiltinDocs::quote)
                .toList());
        if (parentClass != null) {
            types.add("Special." + index.overrideMethodKeyTypes.get(parentClass));
        }
        if (types.isEmpty()) {
            return "never";
        }
        return String.join(" | ", types);
    }

    private static String overrideMethodKeyForEntity(ProbeIndex index) {
        StringBuilder builder = new StringBuilder();
        for (Class<? extends Entity> entityClass : index.sortedEntityClasses) {
            builder.append("T extends ")
                    .append(internalType(entityClass))
                    .append(" ? Special.")
                    .append(overrideMethodKeyTypeName(entityClass))
                    .append(" : ");
        }
        builder.append(OVERRIDE_METHOD_KEY);
        return builder.toString();
    }

    private static String overrideMethodKeyForEntityClass(ProbeIndex index) {
        StringBuilder builder = new StringBuilder();
        builder.append("T extends Internal.Class<infer E> ? E extends Internal.Entity ? ")
                .append(OVERRIDE_METHOD_KEY_FOR_ENTITY)
                .append("<E> : ")
                .append(OVERRIDE_METHOD_KEY)
                .append(" : T extends { prototype: infer E } ? E extends Internal.Entity ? ")
                .append(OVERRIDE_METHOD_KEY_FOR_ENTITY)
                .append("<E> : ")
                .append(OVERRIDE_METHOD_KEY)
                .append(" : T extends abstract new (...args: any) => infer E ? E extends Internal.Entity ? ")
                .append(OVERRIDE_METHOD_KEY_FOR_ENTITY)
                .append("<E> : ")
                .append(OVERRIDE_METHOD_KEY)
                .append(" : ");
        for (Class<? extends Entity> entityClass : index.sortedEntityClasses) {
            builder.append("T extends typeof ")
                    .append(internalType(entityClass))
                    .append(" ? Special.")
                    .append(overrideMethodKeyTypeName(entityClass))
                    .append(" : ");
        }
        builder.append("T extends Internal.Entity ? ")
                .append(OVERRIDE_METHOD_KEY_FOR_ENTITY)
                .append("<T> : ")
                .append(OVERRIDE_METHOD_KEY);
        return builder.toString();
    }

    private static String overrideMethodKeyByEntityClassName(ProbeIndex index) {
        StringBuilder builder = new StringBuilder("{ ");
        for (Class<? extends Entity> entityClass : index.sortedEntityClasses) {
            builder.append(quote(entityClass.getName()))
                    .append(": Special.")
                    .append(overrideMethodKeyTypeName(entityClass))
                    .append("; ");
        }
        builder.append("}");
        return builder.toString();
    }

    private static String entityForEntityClass(ProbeIndex index) {
        StringBuilder builder = new StringBuilder();
        builder.append("T extends Internal.Class<infer E> ? E extends Internal.Entity ? E : Internal.Entity")
                .append(" : T extends { prototype: infer E } ? E extends Internal.Entity ? E : Internal.Entity")
                .append(" : T extends abstract new (...args: any) => infer E ? E extends Internal.Entity ? E : Internal.Entity")
                .append(" : ");
        for (Class<? extends Entity> entityClass : index.sortedEntityClasses) {
            builder.append("T extends typeof ")
                    .append(internalType(entityClass))
                    .append(" ? ")
                    .append(internalType(entityClass))
                    .append(" : ");
        }
        builder.append("T extends Internal.Entity ? T : Internal.Entity");
        return builder.toString();
    }

    private static String entityForEntityClassName(ProbeIndex index) {
        StringBuilder builder = new StringBuilder();
        for (Class<? extends Entity> entityClass : index.sortedEntityClasses) {
            builder.append("T extends ")
                    .append(quote(entityClass.getName()))
                    .append(" ? ")
                    .append(internalType(entityClass))
                    .append(" : ");
        }
        builder.append("Internal.Entity");
        return builder.toString();
    }

    private static String customEntityBuilderWithOverrides() {
        return "{ [P in keyof Internal.CustomEntityBuilder]: P extends \"override\""
                + " ? (methodKey: K, callback: (context: " + DYNAMIC_OVERRIDE_CONTEXT_FOR + "<K, E>) => any) => " + CUSTOM_ENTITY_BUILDER_WITH_OVERRIDES + "<K, E>"
                + " : P extends \"setRendererClass\""
                + " ? " + fluentClassNameOverloads("entityRendererClassName", "entityRendererClass", RENDERER_CLASS_NAME)
                + " : Internal.CustomEntityBuilder[P] extends (...args: infer A) => infer R"
                + " ? R extends Internal.CustomEntityBuilder | Internal.CustomEntityJSBuilder"
                + " ? (...args: A) => " + CUSTOM_ENTITY_BUILDER_WITH_OVERRIDES + "<K, E>"
                + " : Internal.CustomEntityBuilder[P]"
                + " : Internal.CustomEntityBuilder[P] }";
    }

    private static String fluentClassNameOverloads(String classNameParam, String classParam, String classNameType) {
        String returnType = CUSTOM_ENTITY_BUILDER_WITH_OVERRIDES + "<K, E>";
        return "{ (" + classNameParam + ": " + classNameType + "): " + returnType + ";"
                + " (" + classNameParam + ": string & {}): " + returnType + ";"
                + " (" + classParam + ": Internal.Class<any>): " + returnType + " }";
    }

    private static String dynamicOverrideContext() {
        return "{ readonly entity: E; readonly method: string; readonly args: { readonly [key: string]: any }; get(name: string): any; superCall(...args: any[]): any; readonly [key: string]: any }";
    }

    private static String dynamicOverrideContextFor() {
        return DYNAMIC_OVERRIDE_CONTEXT + "<E> & (K extends keyof " + DYNAMIC_OVERRIDE_ARGS_BY_METHOD_KEY + " ? " + DYNAMIC_OVERRIDE_ARGS_BY_METHOD_KEY + "[K] : {})";
    }

    private static String dynamicOverrideArgsByMethodKey(ProbeIndex index) {
        if (index.overrideMethodsByKey().isEmpty()) {
            return "{}";
        }
        StringBuilder builder = new StringBuilder("{ ");
        index.overrideMethodsByKey().values().stream()
                .sorted((left, right) -> left.key().compareTo(right.key()))
                .forEach(method -> builder.append("readonly ")
                        .append(quote(method.key()))
                        .append(": ")
                        .append(dynamicOverrideArgs(method))
                        .append("; "));
        builder.append("}");
        return builder.toString();
    }

    private static String dynamicOverrideArgs(DynamicOverrideMethodCatalog.MethodSpec method) {
        StringBuilder builder = new StringBuilder("{ ");
        StringBuilder argsBuilder = new StringBuilder("{ ");
        String[] names = method.parameterNames();
        Class<?>[] types = method.parameterTypes();
        for (int i = 0; i < names.length; i++) {
            String type = typeScriptType(types[i]);
            appendReadonlyProperty(builder, names[i], type);
            appendReadonlyProperty(builder, "arg" + i, type);
            appendReadonlyProperty(argsBuilder, names[i], type);
            appendReadonlyProperty(argsBuilder, "arg" + i, type);
        }
        argsBuilder.append("readonly [key: string]: any }");
        builder.append("readonly args: ").append(argsBuilder).append(" }");
        return builder.toString();
    }

    private static void appendReadonlyProperty(StringBuilder builder, String name, String type) {
        builder.append("readonly ")
                .append(quote(name))
                .append(": ")
                .append(type)
                .append("; ");
    }

    private static String modifyBuilderForEntity() {
        return "T extends Internal.Projectile ? Internal.ModifyProjectileBuilder"
                + " : T extends Internal.PathfinderMob ? Internal.ModifyPathfinderMobBuilder"
                + " : T extends Internal.Mob ? Internal.ModifyMobBuilder"
                + " : T extends Internal.LivingEntity ? Internal.ModifyLivingEntityBuilder"
                + " : Internal.ModifyEntityBuilder";
    }

    private static String modifyBuilderForEntityClass(ProbeIndex index) {
        StringBuilder builder = new StringBuilder();
        builder.append("T extends Internal.Class<infer E> ? E extends Internal.Entity ? ")
                .append(MODIFY_BUILDER_FOR_ENTITY)
                .append("<E> : Internal.ModifyEntityBuilder")
                .append(" : T extends { prototype: infer E } ? E extends Internal.Entity ? ")
                .append(MODIFY_BUILDER_FOR_ENTITY)
                .append("<E> : Internal.ModifyEntityBuilder")
                .append(" : T extends abstract new (...args: any) => infer E ? E extends Internal.Entity ? ")
                .append(MODIFY_BUILDER_FOR_ENTITY)
                .append("<E> : Internal.ModifyEntityBuilder")
                .append(" : ");
        for (Class<? extends Entity> entityClass : index.sortedEntityClasses) {
            builder.append("T extends typeof ")
                    .append(internalType(entityClass))
                    .append(" ? ")
                    .append(internalType(modifyBuilderClass(entityClass)))
                    .append(" : ");
        }
        builder.append("T extends Internal.Entity ? ")
                .append(MODIFY_BUILDER_FOR_ENTITY)
                .append("<T> : Internal.ModifyEntityBuilder");
        return builder.toString();
    }

    private static String modifyBuilderForEntityClassName() {
        return "T extends Special.EntityJSModifyProjectileEntityClassName ? Internal.ModifyProjectileBuilder"
                + " : T extends Special.EntityJSModifyPathfinderMobEntityClassName ? Internal.ModifyPathfinderMobBuilder"
                + " : T extends Special.EntityJSModifyMobEntityClassName ? Internal.ModifyMobBuilder"
                + " : T extends Special.EntityJSModifyLivingEntityClassName ? Internal.ModifyLivingEntityBuilder"
                + " : Internal.ModifyEntityBuilder";
    }

    private static String quote(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private static String overrideMethodKeyTypeName(Class<?> entityClass) {
        return "EntityJSOverrideMethodKey_" + entityClass.getName().replaceAll("[^A-Za-z0-9_]", "_");
    }

    private static String internalType(Class<?> entityClass) {
        return "Internal." + entityClass.getSimpleName();
    }

    private static String typeScriptType(Class<?> type) {
        if (type == void.class || type == Void.class) {
            return "void";
        }
        if (type == boolean.class || type == Boolean.class) {
            return "boolean";
        }
        if (type == byte.class || type == short.class || type == int.class || type == long.class
                || type == float.class || type == double.class || Number.class.isAssignableFrom(type)) {
            return "number";
        }
        if (type == char.class || type == Character.class || type == String.class) {
            return "string";
        }
        if (type.isArray()) {
            return "any";
        }
        return internalType(type);
    }

    private static int inheritanceDepth(Class<?> type) {
        int depth = 0;
        Class<?> current = type;
        while (current != null && current != Object.class) {
            depth++;
            current = current.getSuperclass();
        }
        return depth;
    }

    private static ProbeIndex buildProbeIndex() {
        Set<Class<? extends Entity>> entityClasses = discoverEntityClasses();
        List<Class<? extends Entity>> sortedEntityClasses = entityClasses.stream()
                .sorted((left, right) -> {
                    int depth = Integer.compare(inheritanceDepth(right), inheritanceDepth(left));
                    if (depth != 0) {
                        return depth;
                    }
                    return left.getName().compareTo(right.getName());
                })
                .toList();
        Set<String> entityClassNames = new LinkedHashSet<>();
        Set<String> overrideMethodKeys = new LinkedHashSet<>();
        Map<Class<? extends Entity>, Set<String>> overrideKeysByClass = new LinkedHashMap<>();
        Map<Class<? extends Entity>, Set<String>> resolvedOverrideKeysByClass = new LinkedHashMap<>();
        Map<String, DynamicOverrideMethodCatalog.MethodSpec> overrideMethodsByKey = new LinkedHashMap<>();
        Map<Class<? extends Entity>, String> overrideMethodKeyTypes = new LinkedHashMap<>();
        Map<Class<? extends Entity>, Class<? extends Entity>> overrideParentByClass = new LinkedHashMap<>();
        Map<Class<? extends ModifyEntityBuilder>, ModifyBuilderGroup> modifyBuilderGroups = createModifyGroups();
        RuntimeClassNameCatalog runtimeClassNameCatalog = RuntimeClassNameCatalog.get();

        for (Class<? extends Entity> entityClass : sortedEntityClasses) {
            entityClassNames.add(entityClass.getName());
            Set<String> resolvedKeys = new LinkedHashSet<>(DynamicOverrideMethodCatalog.overrideKeys(entityClass));
            resolvedOverrideKeysByClass.put(entityClass, resolvedKeys);
            overrideMethodKeys.addAll(resolvedKeys);
            for (String key : resolvedKeys) {
                DynamicOverrideMethodCatalog.MethodSpec method = DynamicOverrideMethodCatalog.resolve(entityClass, key);
                if (method != null) {
                    overrideMethodsByKey.putIfAbsent(method.key(), method);
                }
            }
            overrideMethodKeyTypes.put(entityClass, overrideMethodKeyTypeName(entityClass));
        }
        for (Class<? extends Entity> entityClass : sortedEntityClasses) {
            Class<? extends Entity> parentClass = nearestIndexedSuperclass(entityClass, entityClasses);
            Set<String> localKeys = new LinkedHashSet<>(resolvedOverrideKeysByClass.getOrDefault(entityClass, Set.of()));
            if (parentClass != null) {
                localKeys.removeAll(resolvedOverrideKeysByClass.getOrDefault(parentClass, Set.of()));
                overrideParentByClass.put(entityClass, parentClass);
            }
            overrideKeysByClass.put(entityClass, localKeys);
        }
        for (Map.Entry<ResourceKey<EntityType<?>>, EntityType<?>> entry : BuiltInRegistries.ENTITY_TYPE.entrySet()) {
            Class<? extends Entity> entityClass = entityClass(entry.getValue());
            ModifyBuilderGroup group = modifyBuilderGroups.get(modifyBuilderClass(entityClass));
            if (group != null) {
                group.entityTypeIds.add(entry.getKey().location().toString());
                group.entityClasses.add(entityClass);
            }
        }
        for (Class<? extends Entity> entityClass : entityClasses) {
            ModifyBuilderGroup group = modifyBuilderGroups.get(modifyBuilderClass(entityClass));
            if (group != null) {
                group.entityClassNames.add(entityClass.getName());
                group.entityClasses.add(entityClass);
            }
        }
        return new ProbeIndex(
                entityClasses,
                sortedEntityClasses,
                entityClassNames,
                overrideMethodKeys,
                overrideMethodsByKey,
                overrideKeysByClass,
                overrideMethodKeyTypes,
                overrideParentByClass,
                modifyBuilderGroups,
                runtimeClassNameCatalog.rendererClassNames()
        );
    }

    private static Class<? extends Entity> nearestIndexedSuperclass(Class<? extends Entity> entityClass, Set<Class<? extends Entity>> entityClasses) {
        Class<?> current = entityClass.getSuperclass();
        while (current != null && Entity.class.isAssignableFrom(current)) {
            if (entityClasses.contains(current)) {
                return current.asSubclass(Entity.class);
            }
            current = current.getSuperclass();
        }
        return null;
    }

    private static Set<Class<? extends Entity>> discoverEntityClasses() {
        Set<Class<? extends Entity>> classes = new LinkedHashSet<>();
        classes.add(Entity.class);
        classes.add(LivingEntity.class);
        classes.add(Mob.class);
        classes.add(PathfinderMob.class);
        classes.add(Projectile.class);
        for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
            classes.add(entityClass(entityType));
        }
        return classes;
    }

    private static Class<? extends Entity> entityClass(EntityType<?> entityType) {
        Class<? extends Entity> reflectedClass = vanillaEntityTypeClasses().get(entityType);
        if (reflectedClass != null) {
            return reflectedClass;
        }
        Object builder = EntityJSUtils.getEntityBuilder(entityType);
        if (builder instanceof CustomEntityBuilder customBuilder) {
            return customBuilder.getEntityClass();
        }
        Class<? extends Entity> builderEntityClass = builderEntityClass(builder);
        if (builderEntityClass != null) {
            return builderEntityClass;
        }
        if (builder instanceof BaseNonAnimatableEntityBuilder<?>) {
            return Projectile.class;
        }
        if (builder instanceof BaseEntityBuilder<?>) {
            return Entity.class;
        }
        if (builder instanceof BaseLivingEntityBuilder<?>) {
            return LivingEntity.class;
        }
        return Entity.class;
    }

    private static Class<? extends Entity> builderEntityClass(Object builder) {
        if (builder == null) {
            return null;
        }
        Class<?> type = builder.getClass();
        while (type != null && type != Object.class) {
            java.lang.reflect.Type genericSuperclass = type.getGenericSuperclass();
            if (genericSuperclass instanceof ParameterizedType parameterizedType) {
                Class<? extends Entity> entityClass = entityClassArgument(parameterizedType);
                if (entityClass != null) {
                    return entityClass;
                }
            }
            type = type.getSuperclass();
        }
        return null;
    }

    private static Class<? extends Entity> entityClassArgument(ParameterizedType parameterizedType) {
        if (!(parameterizedType.getRawType() instanceof Class<?> rawType)) {
            return null;
        }
        if (!BaseLivingEntityBuilder.class.isAssignableFrom(rawType)
                && !BaseEntityBuilder.class.isAssignableFrom(rawType)
                && !BaseNonAnimatableEntityBuilder.class.isAssignableFrom(rawType)) {
            return null;
        }
        java.lang.reflect.Type entityArgument = parameterizedType.getActualTypeArguments()[0];
        if (entityArgument instanceof Class<?> entityClass && Entity.class.isAssignableFrom(entityClass)) {
            return entityClass.asSubclass(Entity.class);
        }
        return null;
    }

    private static Map<EntityType<?>, Class<? extends Entity>> vanillaEntityTypeClasses() {
        Map<EntityType<?>, Class<? extends Entity>> classes = new LinkedHashMap<>();
        for (Field field : EntityType.class.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) || !EntityType.class.isAssignableFrom(field.getType())) {
                continue;
            }
            if (!(field.getGenericType() instanceof ParameterizedType parameterizedType)) {
                continue;
            }
            java.lang.reflect.Type entityArgument = parameterizedType.getActualTypeArguments()[0];
            if (!(entityArgument instanceof Class<?> entityClass) || !Entity.class.isAssignableFrom(entityClass)) {
                continue;
            }
            try {
                Object value = field.get(null);
                if (value instanceof EntityType<?> entityType) {
                    classes.put(entityType, entityClass.asSubclass(Entity.class));
                }
            } catch (IllegalAccessException ignored) {
            }
        }
        return classes;
    }

    private static Class<? extends ModifyEntityBuilder> modifyBuilderClass(Class<? extends Entity> entityClass) {
        if (Projectile.class.isAssignableFrom(entityClass)) {
            return ModifyProjectileBuilder.class;
        }
        if (PathfinderMob.class.isAssignableFrom(entityClass)) {
            return ModifyPathfinderMobBuilder.class;
        }
        if (Mob.class.isAssignableFrom(entityClass)) {
            return ModifyMobBuilder.class;
        }
        if (LivingEntity.class.isAssignableFrom(entityClass)) {
            return ModifyLivingEntityBuilder.class;
        }
        return ModifyEntityBuilder.class;
    }

    private static Map<Class<? extends ModifyEntityBuilder>, ModifyBuilderGroup> createModifyGroups() {
        Map<Class<? extends ModifyEntityBuilder>, ModifyBuilderGroup> groups = new LinkedHashMap<>();
        groups.put(ModifyEntityBuilder.class, new ModifyBuilderGroup("EntityJSModifyBaseEntityType", ModifyEntityBuilder.class));
        groups.put(ModifyLivingEntityBuilder.class, new ModifyBuilderGroup("EntityJSModifyLivingEntityType", ModifyLivingEntityBuilder.class));
        groups.put(ModifyMobBuilder.class, new ModifyBuilderGroup("EntityJSModifyMobEntityType", ModifyMobBuilder.class));
        groups.put(ModifyPathfinderMobBuilder.class, new ModifyBuilderGroup("EntityJSModifyPathfinderMobEntityType", ModifyPathfinderMobBuilder.class));
        groups.put(ModifyProjectileBuilder.class, new ModifyBuilderGroup("EntityJSModifyProjectileEntityType", ModifyProjectileBuilder.class));
        return groups;
    }

    private record ProbeIndex(
            Set<Class<? extends Entity>> entityClasses,
            List<Class<? extends Entity>> sortedEntityClasses,
            Set<String> entityClassNames,
            Set<String> overrideMethodKeys,
            Map<String, DynamicOverrideMethodCatalog.MethodSpec> overrideMethodsByKey,
            Map<Class<? extends Entity>, Set<String>> overrideKeysByClass,
            Map<Class<? extends Entity>, String> overrideMethodKeyTypes,
            Map<Class<? extends Entity>, Class<? extends Entity>> overrideParentByClass,
            Map<Class<? extends ModifyEntityBuilder>, ModifyBuilderGroup> modifyBuilderGroups,
            Set<String> rendererClassNames
    ) {
    }

    private static final class ModifyBuilderGroup {
        private final String entityTypeAlias;
        private final String entityClassNameAlias;
        private final Class<? extends ModifyEntityBuilder> builderClass;
        private final Set<String> entityTypeIds = new LinkedHashSet<>();
        private final Set<String> entityClassNames = new LinkedHashSet<>();
        private final Set<Class<? extends Entity>> entityClasses = new LinkedHashSet<>();

        private ModifyBuilderGroup(String entityTypeAlias, Class<? extends ModifyEntityBuilder> builderClass) {
            this.entityTypeAlias = entityTypeAlias;
            this.entityClassNameAlias = entityTypeAlias.replace("EntityType", "EntityClassName");
            this.builderClass = builderClass;
        }
    }
}
