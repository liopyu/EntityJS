package net.liopyu.entityjs.probe;

import dev.latvian.mods.kubejs.registry.RegistryKubeEvent;
import dev.latvian.mods.kubejs.util.KubeResourceLocation;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.java.ClassRegistry;
import moe.wolfgirl.probejs.java.members.ClassInfo;
import moe.wolfgirl.probejs.typescript.ClassPath;
import moe.wolfgirl.probejs.typescript.Documents;
import moe.wolfgirl.probejs.typescript.base.DocumentRegistrar;
import moe.wolfgirl.probejs.typescript.document.Members;
import moe.wolfgirl.probejs.typescript.document.TypeDecl;
import moe.wolfgirl.probejs.typescript.document.Types;
import moe.wolfgirl.probejs.typescript.document.base.Code;
import moe.wolfgirl.probejs.typescript.document.base.KindAware;
import moe.wolfgirl.probejs.typescript.document.base.Type;
import moe.wolfgirl.probejs.typescript.document.members.MethodDecl;
import moe.wolfgirl.probejs.typescript.document.members.ParamDecl;
import moe.wolfgirl.probejs.typescript.document.types.special.NamespacedType;
import net.liopyu.entityjs.builders.misc.CustomEntityBuilder;
import net.liopyu.entityjs.builders.misc.CustomEntityJSBuilder;
import net.liopyu.entityjs.builders.misc.EntityReflection;
import net.liopyu.entityjs.builders.living.BaseLivingEntityBuilder;
import net.liopyu.entityjs.builders.modification.ModifyEntityBuilder;
import net.liopyu.entityjs.builders.modification.ModifyLivingEntityBuilder;
import net.liopyu.entityjs.builders.modification.ModifyMobBuilder;
import net.liopyu.entityjs.builders.modification.ModifyPathfinderMobBuilder;
import net.liopyu.entityjs.builders.modification.ModifyProjectileBuilder;
import net.liopyu.entityjs.builders.nonliving.BaseEntityBuilder;
import net.liopyu.entityjs.builders.nonliving.BaseNonAnimatableEntityBuilder;
import net.liopyu.entityjs.events.EntityModificationEventJS;
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
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class EntityJSProbeJSPlugin extends ProbeJSPlugin {
    private static final ClassPath ENTITYJS_TYPES = ClassPath.special("types.EntityJS");
    private static final NamespacedType OVERRIDE_METHOD_KEY = Types.namespaced(ENTITYJS_TYPES, "OverrideMethodKey");
    private static final NamespacedType OVERRIDE_METHOD_KEY_FOR_ENTITY = Types.namespaced(ENTITYJS_TYPES, "OverrideMethodKeyForEntity");
    private static final NamespacedType OVERRIDE_METHOD_KEY_FOR_ENTITY_CLASS = Types.namespaced(ENTITYJS_TYPES, "OverrideMethodKeyForEntityClass");
    private static final NamespacedType OVERRIDE_METHOD_KEY_FOR_ENTITY_CLASS_NAME = Types.namespaced(ENTITYJS_TYPES, "OverrideMethodKeyForEntityClassName");
    private static final NamespacedType OVERRIDE_METHOD_KEY_BY_ENTITY_CLASS_NAME = Types.namespaced(ENTITYJS_TYPES, "OverrideMethodKeyByEntityClassName");
    private static final NamespacedType ENTITY_FOR_ENTITY_CLASS = Types.namespaced(ENTITYJS_TYPES, "EntityForEntityClass");
    private static final NamespacedType ENTITY_FOR_ENTITY_CLASS_NAME = Types.namespaced(ENTITYJS_TYPES, "EntityForEntityClassName");
    private static final NamespacedType ENTITY_CLASS_NAME = Types.namespaced(ENTITYJS_TYPES, "EntityClassName");
    private static final NamespacedType RENDERER_CLASS_NAME = Types.namespaced(ENTITYJS_TYPES, EntityJSTypingNames.RENDERER_CLASS_NAME);
    private static final NamespacedType DYNAMIC_OVERRIDE_CONTEXT = Types.namespaced(ENTITYJS_TYPES, "DynamicOverrideContext");
    private static final NamespacedType DYNAMIC_OVERRIDE_CONTEXT_FOR = Types.namespaced(ENTITYJS_TYPES, "DynamicOverrideContextFor");
    private static final NamespacedType DYNAMIC_OVERRIDE_ARGS_BY_METHOD_KEY = Types.namespaced(ENTITYJS_TYPES, "DynamicOverrideArgsByMethodKey");
    private static final NamespacedType CUSTOM_ENTITY_BUILDER_WITH_OVERRIDES = Types.namespaced(ENTITYJS_TYPES, "CustomEntityBuilderWithOverrides");
    private static final NamespacedType MODIFY_BUILDER_FOR_ENTITY = Types.namespaced(ENTITYJS_TYPES, "ModifyBuilderForEntity");
    private static final NamespacedType MODIFY_BUILDER_FOR_ENTITY_CLASS = Types.namespaced(ENTITYJS_TYPES, "ModifyBuilderForEntityClass");
    private static final NamespacedType MODIFY_BUILDER_FOR_ENTITY_CLASS_NAME = Types.namespaced(ENTITYJS_TYPES, "ModifyBuilderForEntityClassName");
    private static volatile ProbeIndex probeIndex;
    private static volatile int probeIndexClassRegistrySize = -1;

    @Override
    public void addSpecialDocuments(DocumentRegistrar registrar) {
        ProbeIndex index = index();
        var namespace = Members.clazz(ENTITYJS_TYPES).kind(KindAware.Kind.NAMESPACE);
        namespace.member(typeDecl(OVERRIDE_METHOD_KEY, literalUnion(index.overrideMethodKeys)));
        for (Map.Entry<Class<? extends Entity>, NamespacedType> entry : index.overrideMethodKeyTypes.entrySet()) {
            namespace.member(typeDecl(entry.getValue(), overrideMethodKeyType(entry.getKey(), index)));
        }
        namespace.member(new TypeDecl(
                OVERRIDE_METHOD_KEY_FOR_ENTITY.asClassPath(),
                List.of(Types.variable("T", Types.clazz(Entity.class))),
                overrideMethodKeyForEntity(),
                false
        ));
        namespace.member(new TypeDecl(
                OVERRIDE_METHOD_KEY_FOR_ENTITY_CLASS.asClassPath(),
                List.of(Types.variable("T")),
                overrideMethodKeyForEntityClass(),
                false
        ));
        namespace.member(new TypeDecl(
                OVERRIDE_METHOD_KEY_FOR_ENTITY_CLASS_NAME.asClassPath(),
                List.of(Types.variable("T", ENTITY_CLASS_NAME)),
                overrideMethodKeyForEntityClassName(),
                false
        ));
        namespace.member(new TypeDecl(
                OVERRIDE_METHOD_KEY_BY_ENTITY_CLASS_NAME.asClassPath(),
                overrideMethodKeyByEntityClassName(),
                false
        ));
        namespace.member(new TypeDecl(
                ENTITY_FOR_ENTITY_CLASS.asClassPath(),
                List.of(Types.variable("T")),
                entityForEntityClass(),
                false
        ));
        namespace.member(new TypeDecl(
                ENTITY_FOR_ENTITY_CLASS_NAME.asClassPath(),
                List.of(Types.variable("T", ENTITY_CLASS_NAME)),
                entityForEntityClassName(),
                false
        ));
        namespace.member(typeDecl(ENTITY_CLASS_NAME, literalUnion(index.entityClassNames)));
        namespace.member(typeDecl(RENDERER_CLASS_NAME, literalUnion(index.rendererClassNames)));
        namespace.member(new TypeDecl(
                DYNAMIC_OVERRIDE_CONTEXT.asClassPath(),
                List.of(Types.variable("E", Types.clazz(Entity.class))),
                dynamicOverrideContext(),
                false
        ));
        namespace.member(typeDecl(DYNAMIC_OVERRIDE_ARGS_BY_METHOD_KEY, dynamicOverrideArgsByMethodKey(index)));
        namespace.member(new TypeDecl(
                DYNAMIC_OVERRIDE_CONTEXT_FOR.asClassPath(),
                List.of(Types.variable("K", Types.STRING), Types.variable("E", Types.clazz(Entity.class))),
                dynamicOverrideContextFor(),
                false
        ));
        namespace.member(new TypeDecl(
                CUSTOM_ENTITY_BUILDER_WITH_OVERRIDES.asClassPath(),
                List.of(Types.variable("K", Types.STRING), Types.variable("E", Types.clazz(Entity.class))),
                customEntityBuilderWithOverrides(),
                false
        ));
        namespace.member(new TypeDecl(
                MODIFY_BUILDER_FOR_ENTITY.asClassPath(),
                List.of(Types.variable("T", Types.clazz(Entity.class))),
                modifyBuilderForEntity(),
                false
        ));
        namespace.member(new TypeDecl(
                MODIFY_BUILDER_FOR_ENTITY_CLASS.asClassPath(),
                List.of(Types.variable("T")),
                modifyBuilderForEntityClass(),
                false
        ));
        namespace.member(new TypeDecl(
                MODIFY_BUILDER_FOR_ENTITY_CLASS_NAME.asClassPath(),
                List.of(Types.variable("T", ENTITY_CLASS_NAME)),
                modifyBuilderForEntityClassName(),
                false
        ));
        for (ModifyBuilderGroup group : index.modifyBuilderGroups.values()) {
            namespace.member(typeDecl(group.entityType, literalUnion(group.entityTypeIds)));
            namespace.member(typeDecl(group.entityClassName, literalUnion(group.entityClassNames)));
        }
        registrar.addDocument(ENTITYJS_TYPES, namespace.build());
    }

    @Override
    public void transformClass(Documents.ClassDocument classDocument) {
        Class<?> type = classDocument.classInfo().clazz();
        if (type == CustomEntityBuilder.class) {
            patchCustomEntityBuilder(classDocument);
        } else if (type == EntityModificationEventJS.class) {
            patchEntityModificationEvent(classDocument);
        } else if (type == RegistryKubeEvent.class || type == IRegistryJS.class) {
            patchRegistryCreateCustom(classDocument);
        }
    }

    private static void patchCustomEntityBuilder(Documents.ClassDocument classDocument) {
        removeMethodsNamed(classDocument, "setRendererClass");
        List<Code> overloads = new ArrayList<>();
        addClientClassNameOverloads(overloads, "setRendererClass", "entityRendererClassName", "entityRendererClass", RENDERER_CLASS_NAME);
        classDocument.document().members.addAll(0, overloads);
        for (Code member : classDocument.document().members) {
            if (member instanceof MethodDecl method && method.name.equals("override") && method.params.size() >= 2) {
                method.params.get(0).typeInfo = OVERRIDE_METHOD_KEY;
            }
        }
    }

    private static void patchEntityModificationEvent(Documents.ClassDocument classDocument) {
        List<Code> overloads = new ArrayList<>();
        for (ModifyBuilderGroup group : index().modifyBuilderGroups.values()) {
            overloads.add(method(
                    "modify",
                    Types.VOID,
                    param("entityType", literalUnion(group.entityTypeIds)),
                    param("modifyBuilder", builderCallback(group.builderClass))
            ));
        }
        overloads.add(method(
                "modify",
                List.of(Types.variable("T", Types.clazz(Entity.class))),
                Types.VOID,
                param("entityType", input(Types.parameterized(Types.clazz(EntityType.class), Types.variable("T")))),
                param("modifyBuilder", builderCallback(Types.parameterized(MODIFY_BUILDER_FOR_ENTITY, Types.variable("T"))))
        ));
        classDocument.document().members.addAll(0, overloads);
    }

    private static void patchRegistryCreateCustom(Documents.ClassDocument classDocument) {
        List<Code> overloads = new ArrayList<>();
        overloads.add(method(
                "createCustom",
                List.of(Types.variable("T", ENTITY_CLASS_NAME)),
                customEntityBuilderWithOverrides(
                        Types.parameterized(OVERRIDE_METHOD_KEY_FOR_ENTITY_CLASS_NAME, Types.variable("T")),
                        Types.parameterized(ENTITY_FOR_ENTITY_CLASS_NAME, Types.variable("T"))
                ),
                param("id", input(Types.clazz(KubeResourceLocation.class))),
                param("entityClass", input(Types.variable("T"))),
                param("modifyBuilder", builderCallback(Types.parameterized(MODIFY_BUILDER_FOR_ENTITY_CLASS_NAME, Types.variable("T"))))
        ));
        overloads.add(method(
                "createCustom",
                List.of(Types.variable("T")),
                customEntityBuilderWithOverrides(
                        Types.parameterized(OVERRIDE_METHOD_KEY_FOR_ENTITY_CLASS, Types.variable("T")),
                        Types.parameterized(ENTITY_FOR_ENTITY_CLASS, Types.variable("T"))
                ),
                param("id", input(Types.clazz(KubeResourceLocation.class))),
                param("entityClass", input(new ImportedRawType(
                        Set.of(classPath(Class.class)),
                        symbols -> "T extends " + symbol(symbols, Class.class) + "<any> ? T : never"
                ))),
                param("modifyBuilder", builderCallback(Types.parameterized(MODIFY_BUILDER_FOR_ENTITY_CLASS, Types.variable("T"))))
        ));
        overloads.add(method(
                "createCustom",
                List.of(Types.variable("T")),
                customEntityBuilderWithOverrides(
                        Types.parameterized(OVERRIDE_METHOD_KEY_FOR_ENTITY_CLASS, Types.variable("T")),
                        Types.parameterized(ENTITY_FOR_ENTITY_CLASS, Types.variable("T"))
                ),
                param("id", input(Types.clazz(KubeResourceLocation.class))),
                param("entityClass", input(new ImportedRawType(
                        Set.of(classPath(Class.class)),
                        symbols -> "T extends " + symbol(symbols, Class.class) + "<any> ? never : T"
                ))),
                param("modifyBuilder", builderCallback(Types.parameterized(MODIFY_BUILDER_FOR_ENTITY_CLASS, Types.variable("T"))))
        ));
        classDocument.document().members.addAll(0, overloads);
    }

    private static TypeDecl typeDecl(NamespacedType type, Type value) {
        return new TypeDecl(type.asClassPath(), value, false);
    }

    private static MethodDecl method(String name, Type returnType, ParamDecl... params) {
        return new MethodDecl(name, List.of(), new ArrayList<>(List.of(params)), returnType, false);
    }

    private static MethodDecl method(String name, List<moe.wolfgirl.probejs.typescript.document.types.VariableType> typeParams, Type returnType, ParamDecl... params) {
        return new MethodDecl(name, typeParams, new ArrayList<>(List.of(params)), returnType, false);
    }

    private static ParamDecl param(String name, Type type) {
        return new ParamDecl(name, type);
    }

    private static void addClientClassNameOverloads(List<Code> overloads, String methodName, String classNameParam, String classParam, Type classNameType) {
        overloads.add(method(
                methodName,
                Types.clazz(CustomEntityBuilder.class),
                param(classNameParam, input(Types.union(List.of(classNameType))))
        ));
        overloads.add(method(
                methodName,
                Types.clazz(CustomEntityBuilder.class),
                param(classNameParam, input(stringFallback()))
        ));
        overloads.add(method(
                methodName,
                Types.clazz(CustomEntityBuilder.class),
                param(classParam, input(javaClassAny()))
        ));
    }

    private static void removeMethodsNamed(Documents.ClassDocument classDocument, String methodName) {
        classDocument.document().members.removeIf(member -> member instanceof MethodDecl method
                && (method.name.equals(methodName) || method.name.startsWith(methodName + "(")));
    }

    private static Type builderCallback(Class<? extends ModifyEntityBuilder> builderClass) {
        return builderCallback(Types.clazz(builderClass).asMaybeGeneric());
    }

    private static Type builderCallback(Type builderType) {
        return Types.lambda(builder -> builder
                .param("builder", builderType)
                .returns(Types.VOID)
        );
    }

    private static Type literalUnion(Collection<String> values) {
        if (values.isEmpty()) {
            return Types.NEVER;
        }
        return Types.union(values.stream()
                .distinct()
                .sorted()
                .map(Types::literal)
                .map(Type.class::cast)
                .toList());
    }

    private static Type overrideMethodKeyType(Class<? extends Entity> entityClass, ProbeIndex index) {
        Set<String> localKeys = index.overrideKeysByClass.getOrDefault(entityClass, Set.of());
        Class<? extends Entity> parentClass = index.overrideParentByClass.get(entityClass);
        if (parentClass == null) {
            return literalUnion(localKeys);
        }
        if (localKeys.isEmpty()) {
            return index.overrideMethodKeyTypes.get(parentClass);
        }

        List<Type> types = new ArrayList<>(localKeys.stream()
                .distinct()
                .sorted()
                .map(Types::literal)
                .map(Type.class::cast)
                .toList());
        types.add(index.overrideMethodKeyTypes.get(parentClass));
        return Types.union(types);
    }

    private static Type input(Type type) {
        Types.markAsInput(type);
        return type;
    }

    private static Type stringFallback() {
        return new ImportedRawType(Set.of(), symbols -> "(string & {})");
    }

    private static Type javaClassAny() {
        return new ImportedRawType(
                Set.of(classPath(Class.class)),
                symbols -> symbol(symbols, Class.class) + "<any>"
        );
    }

    private static Type customEntityBuilderWithOverrides(Type overrideKeyType, Type entityType) {
        return Types.parameterized(CUSTOM_ENTITY_BUILDER_WITH_OVERRIDES, overrideKeyType, entityType);
    }

    private static Type customEntityBuilderWithOverrides() {
        return new ImportedRawType(
                Set.of(
                        classPath(CustomEntityBuilder.class),
                        classPath(CustomEntityJSBuilder.class),
                        classPath(Class.class)
                ),
                symbols -> "{ [P in keyof " + symbol(symbols, CustomEntityBuilder.class) + "]: P extends \"override\""
                        + " ? (methodKey: K, callback: (context: DynamicOverrideContextFor<K, E>) => any) => CustomEntityBuilderWithOverrides<K, E>"
                        + " : P extends \"setRendererClass\""
                        + " ? " + fluentClassNameOverloads("entityRendererClassName", "entityRendererClass", EntityJSTypingNames.RENDERER_CLASS_NAME, symbol(symbols, Class.class) + "<any>")
                        + " : " + symbol(symbols, CustomEntityBuilder.class) + "[P] extends (...args: infer A) => infer R"
                        + " ? R extends " + symbol(symbols, CustomEntityBuilder.class) + " | " + symbol(symbols, CustomEntityJSBuilder.class)
                        + " ? (...args: A) => CustomEntityBuilderWithOverrides<K, E>"
                        + " : " + symbol(symbols, CustomEntityBuilder.class) + "[P]"
                        + " : " + symbol(symbols, CustomEntityBuilder.class) + "[P] }"
        );
    }

    private static Type dynamicOverrideContext() {
        return new ImportedRawType(
                Set.of(),
                symbols -> "{ readonly entity: E; readonly method: string; readonly args: { readonly [key: string]: any }; get(name: string): any; superCall(...args: any[]): any; readonly [key: string]: any }"
        );
    }

    private static String fluentClassNameOverloads(String classNameParam, String classParam, String classNameType, String classType) {
        return "{ (" + classNameParam + ": " + classNameType + "): CustomEntityBuilderWithOverrides<K, E>;"
                + " (" + classNameParam + ": string & {}): CustomEntityBuilderWithOverrides<K, E>;"
                + " (" + classParam + ": " + classType + "): CustomEntityBuilderWithOverrides<K, E> }";
    }

    private static Type dynamicOverrideContextFor() {
        return new ImportedRawType(
                Set.of(),
                symbols -> "DynamicOverrideContext<E> & (K extends keyof DynamicOverrideArgsByMethodKey ? DynamicOverrideArgsByMethodKey[K] : {})"
        );
    }

    private static Type dynamicOverrideArgsByMethodKey(ProbeIndex index) {
        if (index.overrideMethodsByKey.isEmpty()) {
            return new ImportedRawType(Set.of(), symbols -> "{}");
        }
        return new ImportedRawType(Set.of(), symbols -> {
            StringBuilder builder = new StringBuilder("{ ");
            index.overrideMethodsByKey.values().stream()
                    .sorted((left, right) -> left.key().compareTo(right.key()))
                    .forEach(method -> builder.append("readonly \"")
                            .append(method.key())
                            .append("\": ")
                            .append(dynamicOverrideArgs(method))
                            .append("; "));
            builder.append("}");
            return builder.toString();
        });
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
        builder.append("readonly \"")
                .append(name)
                .append("\": ")
                .append(type)
                .append("; ");
    }

    private static Type overrideMethodKeyForEntity() {
        ProbeIndex index = index();
        Set<ClassPath> imports = new LinkedHashSet<>();
        for (Class<? extends Entity> entityClass : index.sortedEntityClasses) {
            imports.add(classPath(entityClass));
        }
        return new ImportedRawType(imports, symbols -> {
            StringBuilder builder = new StringBuilder();
            for (Class<? extends Entity> entityClass : index.sortedEntityClasses) {
                builder.append("T extends ")
                        .append(symbol(symbols, entityClass))
                        .append(" ? ")
                        .append(overrideMethodKeyTypeName(entityClass))
                        .append(" : ");
            }
            builder.append("OverrideMethodKey");
            return builder.toString();
        });
    }

    private static Type overrideMethodKeyForEntityClass() {
        ProbeIndex index = index();
        Set<ClassPath> imports = new LinkedHashSet<>();
        imports.add(classPath(Class.class));
        imports.add(classPath(Entity.class));
        for (Class<? extends Entity> entityClass : index.sortedEntityClasses) {
            imports.add(classPath(entityClass));
        }
        return new ImportedRawType(imports, symbols -> {
            StringBuilder builder = new StringBuilder();
            builder.append("[T] extends [never] ? OverrideMethodKey : T extends ")
                    .append(symbol(symbols, Class.class))
                    .append("<infer E> ? E extends ")
                    .append(symbol(symbols, Entity.class))
                    .append(" ? OverrideMethodKeyForEntity<E> : OverrideMethodKey")
                    .append(" : T extends { prototype: infer E } ? E extends ")
                    .append(symbol(symbols, Entity.class))
                    .append(" ? OverrideMethodKeyForEntity<E> : OverrideMethodKey")
                    .append(" : T extends abstract new (...args: any) => infer E ? E extends ")
                    .append(symbol(symbols, Entity.class))
                    .append(" ? OverrideMethodKeyForEntity<E> : OverrideMethodKey")
                    .append(" : ");
            for (Class<? extends Entity> entityClass : index.sortedEntityClasses) {
                builder.append("T extends typeof ")
                        .append(symbol(symbols, entityClass))
                        .append(" ? ")
                        .append(overrideMethodKeyTypeName(entityClass))
                        .append(" : ");
            }
            builder.append("T extends ")
                    .append(symbol(symbols, Entity.class))
                    .append(" ? OverrideMethodKeyForEntity<T> : OverrideMethodKey");
            return builder.toString();
        });
    }

    private static Type overrideMethodKeyForEntityClassName() {
        return new ImportedRawType(
                Set.of(),
                symbols -> "T extends keyof OverrideMethodKeyByEntityClassName ? OverrideMethodKeyByEntityClassName[T] : OverrideMethodKey"
        );
    }

    private static Type overrideMethodKeyByEntityClassName() {
        ProbeIndex index = index();
        return new ImportedRawType(Set.of(), symbols -> {
            StringBuilder builder = new StringBuilder("{ ");
            for (Class<? extends Entity> entityClass : index.sortedEntityClasses) {
                builder.append("\"")
                        .append(entityClass.getName())
                        .append("\": ")
                        .append(overrideMethodKeyTypeName(entityClass))
                        .append("; ");
            }
            builder.append("}");
            return builder.toString();
        });
    }

    private static Type entityForEntityClass() {
        ProbeIndex index = index();
        Set<ClassPath> imports = new LinkedHashSet<>();
        imports.add(classPath(Class.class));
        imports.add(classPath(Entity.class));
        for (Class<? extends Entity> entityClass : index.sortedEntityClasses) {
            imports.add(classPath(entityClass));
        }
        return new ImportedRawType(imports, symbols -> {
            StringBuilder builder = new StringBuilder();
            builder.append("[T] extends [never] ? ")
                    .append(symbol(symbols, Entity.class))
                    .append(" : T extends ")
                    .append(symbol(symbols, Class.class))
                    .append("<infer E> ? E extends ")
                    .append(symbol(symbols, Entity.class))
                    .append(" ? E : ")
                    .append(symbol(symbols, Entity.class))
                    .append(" : T extends { prototype: infer E } ? E extends ")
                    .append(symbol(symbols, Entity.class))
                    .append(" ? E : ")
                    .append(symbol(symbols, Entity.class))
                    .append(" : T extends abstract new (...args: any) => infer E ? E extends ")
                    .append(symbol(symbols, Entity.class))
                    .append(" ? E : ")
                    .append(symbol(symbols, Entity.class))
                    .append(" : ");
            for (Class<? extends Entity> entityClass : index.sortedEntityClasses) {
                builder.append("T extends typeof ")
                        .append(symbol(symbols, entityClass))
                        .append(" ? ")
                        .append(symbol(symbols, entityClass))
                        .append(" : ");
            }
            builder.append("T extends ")
                    .append(symbol(symbols, Entity.class))
                    .append(" ? T : ")
                    .append(symbol(symbols, Entity.class));
            return builder.toString();
        });
    }

    private static Type entityForEntityClassName() {
        ProbeIndex index = index();
        Set<ClassPath> imports = new LinkedHashSet<>();
        imports.add(classPath(Entity.class));
        for (Class<? extends Entity> entityClass : index.sortedEntityClasses) {
            imports.add(classPath(entityClass));
        }
        return new ImportedRawType(imports, symbols -> {
            StringBuilder builder = new StringBuilder();
            for (Class<? extends Entity> entityClass : index.sortedEntityClasses) {
                builder.append("T extends \"")
                        .append(entityClass.getName())
                        .append("\" ? ")
                        .append(symbol(symbols, entityClass))
                        .append(" : ");
            }
            builder.append(symbol(symbols, Entity.class));
            return builder.toString();
        });
    }

    private static Type modifyBuilderForEntity() {
        return new ImportedRawType(
                Set.of(
                        classPath(Projectile.class),
                        classPath(PathfinderMob.class),
                        classPath(Mob.class),
                        classPath(LivingEntity.class),
                        classPath(ModifyProjectileBuilder.class),
                        classPath(ModifyPathfinderMobBuilder.class),
                        classPath(ModifyMobBuilder.class),
                        classPath(ModifyLivingEntityBuilder.class),
                        classPath(ModifyEntityBuilder.class)
                ),
                symbols -> "T extends " + symbol(symbols, Projectile.class) + " ? " + symbol(symbols, ModifyProjectileBuilder.class)
                        + " : T extends " + symbol(symbols, PathfinderMob.class) + " ? " + symbol(symbols, ModifyPathfinderMobBuilder.class)
                        + " : T extends " + symbol(symbols, Mob.class) + " ? " + symbol(symbols, ModifyMobBuilder.class)
                        + " : T extends " + symbol(symbols, LivingEntity.class) + " ? " + symbol(symbols, ModifyLivingEntityBuilder.class)
                        + " : " + symbol(symbols, ModifyEntityBuilder.class)
        );
    }

    private static Type modifyBuilderForEntityClass() {
        ProbeIndex index = index();
        Set<ClassPath> imports = new LinkedHashSet<>();
        imports.add(classPath(Class.class));
        imports.add(classPath(Entity.class));
        imports.add(classPath(ModifyEntityBuilder.class));
        for (Class<? extends Entity> entityClass : index.sortedEntityClasses) {
            imports.add(classPath(entityClass));
            imports.add(classPath(modifyBuilderClass(entityClass)));
        }
        return new ImportedRawType(
                imports,
                symbols -> {
                    StringBuilder builder = new StringBuilder();
                    builder.append("[T] extends [never] ? ")
                            .append(symbol(symbols, ModifyEntityBuilder.class))
                            .append(" : T extends ")
                            .append(symbol(symbols, Class.class))
                            .append("<infer E> ? E extends ")
                            .append(symbol(symbols, Entity.class))
                            .append(" ? ModifyBuilderForEntity<E> : ")
                            .append(symbol(symbols, ModifyEntityBuilder.class))
                            .append(" : T extends { prototype: infer E } ? E extends ")
                            .append(symbol(symbols, Entity.class))
                            .append(" ? ModifyBuilderForEntity<E> : ")
                            .append(symbol(symbols, ModifyEntityBuilder.class))
                            .append(" : T extends abstract new (...args: any) => infer E ? E extends ")
                            .append(symbol(symbols, Entity.class))
                            .append(" ? ModifyBuilderForEntity<E> : ")
                            .append(symbol(symbols, ModifyEntityBuilder.class))
                            .append(" : ");
                    for (Class<? extends Entity> entityClass : index.sortedEntityClasses) {
                        builder.append("T extends typeof ")
                                .append(symbol(symbols, entityClass))
                                .append(" ? ")
                                .append(symbol(symbols, modifyBuilderClass(entityClass)))
                                .append(" : ");
                    }
                    builder.append("T extends ")
                            .append(symbol(symbols, Entity.class))
                            .append(" ? ModifyBuilderForEntity<T> : ")
                            .append(symbol(symbols, ModifyEntityBuilder.class));
                    return builder.toString();
                }
        );
    }

    private static Type modifyBuilderForEntityClassName() {
        return new ImportedRawType(
                Set.of(
                        classPath(ModifyProjectileBuilder.class),
                        classPath(ModifyPathfinderMobBuilder.class),
                        classPath(ModifyMobBuilder.class),
                        classPath(ModifyLivingEntityBuilder.class),
                        classPath(ModifyEntityBuilder.class)
                ),
                symbols -> "T extends ModifyProjectileEntityClassName ? " + symbol(symbols, ModifyProjectileBuilder.class)
                        + " : T extends ModifyPathfinderMobEntityClassName ? " + symbol(symbols, ModifyPathfinderMobBuilder.class)
                        + " : T extends ModifyMobEntityClassName ? " + symbol(symbols, ModifyMobBuilder.class)
                        + " : T extends ModifyLivingEntityClassName ? " + symbol(symbols, ModifyLivingEntityBuilder.class)
                        + " : " + symbol(symbols, ModifyEntityBuilder.class)
        );
    }

    private static String overrideMethodKeyTypeName(Class<?> entityClass) {
        return "OverrideMethodKey_" + entityClass.getName().replaceAll("[^A-Za-z0-9_]", "_");
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

    private static ProbeIndex index() {
        ProbeIndex current = probeIndex;
        int classRegistrySize = ClassRegistry.INSTANCE.getAllClasses().size();
        if (current == null || probeIndexClassRegistrySize != classRegistrySize) {
            current = buildProbeIndex();
            probeIndex = current;
            probeIndexClassRegistrySize = classRegistrySize;
        }
        return current;
    }

    private static ProbeIndex buildProbeIndex() {
        Set<Class<? extends Entity>> entityClasses = discoverEntityClasses();
        RuntimeClassNameCatalog runtimeClassNameCatalog = RuntimeClassNameCatalog.get();
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
        Map<Class<? extends Entity>, NamespacedType> overrideMethodKeyTypes = new LinkedHashMap<>();
        Map<Class<? extends Entity>, Class<? extends Entity>> overrideParentByClass = new LinkedHashMap<>();
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
            overrideMethodKeyTypes.put(entityClass, Types.namespaced(ENTITYJS_TYPES, overrideMethodKeyTypeName(entityClass)));
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

        return new ProbeIndex(
                entityClasses,
                sortedEntityClasses,
                entityClassNames,
                runtimeClassNameCatalog.rendererClassNames(),
                overrideMethodKeys,
                overrideMethodsByKey,
                overrideKeysByClass,
                overrideMethodKeyTypes,
                overrideParentByClass,
                modifyBuilderGroups(entityClasses)
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
        for (ClassInfo classInfo : ClassRegistry.INSTANCE.getAllClasses().values()) {
            Class<?> type = classInfo.clazz();
            if (type == null
                    || type.isAnonymousClass()
                    || type.isSynthetic()
                    || type.getName().contains("$EntityJSDynamicOverride$")
                    || EntityReflection.isMixinClass(type)
                    || !Entity.class.isAssignableFrom(type)) {
                continue;
            }
            classes.add(type.asSubclass(Entity.class));
        }
        return classes;
    }

    private static Map<Class<? extends ModifyEntityBuilder>, ModifyBuilderGroup> modifyBuilderGroups(Set<Class<? extends Entity>> entityClasses) {
        Map<Class<? extends ModifyEntityBuilder>, ModifyBuilderGroup> groups = new LinkedHashMap<>();
        groups.put(ModifyEntityBuilder.class, new ModifyBuilderGroup("ModifyBaseEntityType", ModifyEntityBuilder.class));
        groups.put(ModifyLivingEntityBuilder.class, new ModifyBuilderGroup("ModifyLivingEntityType", ModifyLivingEntityBuilder.class));
        groups.put(ModifyMobBuilder.class, new ModifyBuilderGroup("ModifyMobEntityType", ModifyMobBuilder.class));
        groups.put(ModifyPathfinderMobBuilder.class, new ModifyBuilderGroup("ModifyPathfinderMobEntityType", ModifyPathfinderMobBuilder.class));
        groups.put(ModifyProjectileBuilder.class, new ModifyBuilderGroup("ModifyProjectileEntityType", ModifyProjectileBuilder.class));

        for (Map.Entry<ResourceKey<EntityType<?>>, EntityType<?>> entry : BuiltInRegistries.ENTITY_TYPE.entrySet()) {
            Class<? extends Entity> entityClass = entityClass(entry.getValue());
            ModifyBuilderGroup group = groups.get(modifyBuilderClass(entityClass));
            group.entityTypeIds.add(entry.getKey().location().toString());
            group.entityClasses.add(entityClass);
        }
        for (Class<? extends Entity> entityClass : entityClasses) {
            ModifyBuilderGroup group = groups.get(modifyBuilderClass(entityClass));
            group.entityClassNames.add(entityClass.getName());
            group.entityClasses.add(entityClass);
        }
        return groups;
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
        return entityType.getBaseClass();
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

    private static ClassPath classPath(Class<?> type) {
        return new ClassPath(type);
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
        return "any";
    }

    private static String symbol(Map<ClassPath, String> symbols, Class<?> type) {
        ClassPath classPath = classPath(type);
        return symbols.getOrDefault(classPath, classPath.getClassName());
    }

    private static final class ImportedRawType extends Type {
        private final Set<ClassPath> imports;
        private final Function<Map<ClassPath, String>, String> formatter;
        private Map<ClassPath, String> resolvedSymbols = Map.of();

        private ImportedRawType(Set<ClassPath> imports, Function<Map<ClassPath, String>, String> formatter) {
            this.imports = imports;
            this.formatter = formatter;
        }

        @Override
        public Set<ClassPath> getImports() {
            return imports;
        }

        @Override
        public void setResolvedSymbols(Map<ClassPath, String> resolvedSymbols) {
            this.resolvedSymbols = resolvedSymbols;
        }

        @Override
        public List<String> format(int indent) {
            return List.of(formatter.apply(resolvedSymbols));
        }

        @Override
        public Collection<Code> getContainedTypes() {
            return List.of();
        }
    }

    private record ProbeIndex(
            Set<Class<? extends Entity>> entityClasses,
            List<Class<? extends Entity>> sortedEntityClasses,
            Set<String> entityClassNames,
            Set<String> rendererClassNames,
            Set<String> overrideMethodKeys,
            Map<String, DynamicOverrideMethodCatalog.MethodSpec> overrideMethodsByKey,
            Map<Class<? extends Entity>, Set<String>> overrideKeysByClass,
            Map<Class<? extends Entity>, NamespacedType> overrideMethodKeyTypes,
            Map<Class<? extends Entity>, Class<? extends Entity>> overrideParentByClass,
            Map<Class<? extends ModifyEntityBuilder>, ModifyBuilderGroup> modifyBuilderGroups
    ) {
    }

    private static final class ModifyBuilderGroup {
        private final NamespacedType entityType;
        private final NamespacedType entityClassName;
        private final Class<? extends ModifyEntityBuilder> builderClass;
        private final Set<String> entityTypeIds = new LinkedHashSet<>();
        private final Set<String> entityClassNames = new LinkedHashSet<>();
        private final Set<Class<? extends Entity>> entityClasses = new LinkedHashSet<>();

        private ModifyBuilderGroup(String entityTypeAlias, Class<? extends ModifyEntityBuilder> builderClass) {
            this.entityType = Types.namespaced(ENTITYJS_TYPES, entityTypeAlias);
            this.entityClassName = Types.namespaced(ENTITYJS_TYPES, entityTypeAlias.replace("EntityType", "EntityClassName"));
            this.builderClass = builderClass;
        }
    }
}
