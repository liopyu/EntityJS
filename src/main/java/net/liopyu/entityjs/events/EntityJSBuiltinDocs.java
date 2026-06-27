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
import net.liopyu.entityjs.builders.modification.ModifyEntityBuilder;
import net.liopyu.entityjs.builders.modification.ModifyLivingEntityBuilder;
import net.liopyu.entityjs.builders.modification.ModifyMobBuilder;
import net.liopyu.entityjs.builders.modification.ModifyPathfinderMobBuilder;
import net.liopyu.entityjs.builders.modification.ModifyProjectileBuilder;
import net.liopyu.entityjs.builders.nonliving.BaseEntityBuilder;
import net.liopyu.entityjs.builders.nonliving.BaseNonAnimatableEntityBuilder;
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
    private static final String ENTITY_CLASS_NAME = "Special.EntityJSEntityClassName";
    private static final Set<DocGenerationEventJS> REGISTERED_EVENTS = Collections.newSetFromMap(new WeakHashMap<>());

    private EntityJSBuiltinDocs() {
    }

    public static void register(DocGenerationEventJS event) {
        if (!REGISTERED_EVENTS.add(event)) {
            return;
        }
        captureEntityJSClasses();
        ProbeIndex index = buildProbeIndex();
        event.specialType("EntityJSOverrideMethodKey", quotedUnion(index.overrideMethodKeys()));
        event.specialType("EntityJSEntityClassName", quotedUnion(index.entityClassNames()));
        for (ModifyBuilderGroup group : index.modifyBuilderGroups().values()) {
            event.specialType(group.entityTypeAlias, quotedUnion(group.entityTypeIds));
        }

        event.transformDocument(CustomEntityBuilder.class, EntityJSBuiltinDocs::patchCustomEntityBuilder);
        event.transformDocument(EntityModificationEventJS.class, EntityJSBuiltinDocs::patchEntityModificationEvent);
        event.transformDocument(RegistryEventJS.class, EntityJSBuiltinDocs::patchRegistryCreateCustom);
        event.transformDocument(IRegistryJS.class, EntityJSBuiltinDocs::patchRegistryCreateCustom);
    }

    private static void captureEntityJSClasses() {
        DocCompiler.CapturedClasses.capturedJavaClasses.addAll(List.of(
                CustomEntityBuilder.class,
                EntityModificationEventJS.class,
                ModifyEntityBuilder.class,
                ModifyLivingEntityBuilder.class,
                ModifyMobBuilder.class,
                ModifyPathfinderMobBuilder.class,
                ModifyProjectileBuilder.class
        ));
    }

    private static void patchCustomEntityBuilder(DocumentClass documentClass) {
        for (DocumentMethod method : documentClass.methods) {
            if ("override".equals(method.name) && !method.params.isEmpty()) {
                replaceParam(method, 0, "methodKey", nativeType(OVERRIDE_METHOD_KEY));
            }
        }
    }

    private static void patchEntityModificationEvent(DocumentClass documentClass) {
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
                voidType(),
                param("entityType", parameterized(clazz(EntityType.class), clazz(Entity.class))),
                param("modifyBuilder", builderCallback(clazz(ModifyEntityBuilder.class)))
        ));
    }

    private static void patchRegistryCreateCustom(DocumentClass documentClass) {
        addMethod(documentClass, method(
                "createCustom",
                clazz(CustomEntityBuilder.class),
                param("id", nativeType("string")),
                param("entityClass", nativeType(ENTITY_CLASS_NAME)),
                param("modifyBuilder", builderCallback(clazz(ModifyEntityBuilder.class)))
        ));
        addMethod(documentClass, method(
                "createCustom",
                clazz(CustomEntityBuilder.class),
                param("id", nativeType("string")),
                param("entityClass", parameterized(clazz(Class.class), clazz(Entity.class))),
                param("modifyBuilder", builderCallback(clazz(ModifyEntityBuilder.class)))
        ));
    }

    private static DocumentMethod method(String name, PropertyType<?> returnType, PropertyParam... params) {
        DocumentMethod method = new DocumentMethod();
        method.name = name;
        method.returns = returnType;
        method.params.addAll(List.of(params));
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

    private static List<Object> quotedUnion(Collection<String> values) {
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

    private static String quote(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private static ProbeIndex buildProbeIndex() {
        Set<Class<? extends Entity>> entityClasses = discoverEntityClasses();
        Set<String> entityClassNames = new LinkedHashSet<>();
        Set<String> overrideMethodKeys = new LinkedHashSet<>();
        Map<Class<? extends ModifyEntityBuilder>, ModifyBuilderGroup> modifyBuilderGroups = createModifyGroups();

        for (Class<? extends Entity> entityClass : entityClasses) {
            entityClassNames.add(entityClass.getName());
            overrideMethodKeys.addAll(DynamicOverrideMethodCatalog.overrideKeys(entityClass));
        }
        for (Map.Entry<ResourceKey<EntityType<?>>, EntityType<?>> entry : BuiltInRegistries.ENTITY_TYPE.entrySet()) {
            Class<? extends Entity> entityClass = entityClass(entry.getValue());
            ModifyBuilderGroup group = modifyBuilderGroups.get(modifyBuilderClass(entityClass));
            if (group != null) {
                group.entityTypeIds.add(entry.getKey().location().toString());
            }
        }
        return new ProbeIndex(entityClassNames, overrideMethodKeys, modifyBuilderGroups);
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
            Set<String> entityClassNames,
            Set<String> overrideMethodKeys,
            Map<Class<? extends ModifyEntityBuilder>, ModifyBuilderGroup> modifyBuilderGroups
    ) {
    }

    private static final class ModifyBuilderGroup {
        private final String entityTypeAlias;
        private final Class<? extends ModifyEntityBuilder> builderClass;
        private final Set<String> entityTypeIds = new LinkedHashSet<>();

        private ModifyBuilderGroup(String entityTypeAlias, Class<? extends ModifyEntityBuilder> builderClass) {
            this.entityTypeAlias = entityTypeAlias;
            this.builderClass = builderClass;
        }
    }
}
