package net.liopyu.entityjs.builders.misc;

import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.liopyu.entityjs.item.SpawnEggItemBuilder;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.common.util.overrides.CallbackInvoker;
import net.liopyu.entityjs.util.overrides.dynamic.DynamicOverrideEntityFactory;
import net.liopyu.entityjs.common.util.overrides.catalog.DynamicOverrideMethodCatalog;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;


public class CustomEntityBuilder extends CustomEntityJSBuilder {

    private final Class<? extends Entity> entityClass;
    public transient SpawnEggItemBuilder eggItem;
    public transient boolean noEggItem = false;
    public transient Function<Object, Object> entityModelFactory;
    public transient Class<?> entityRendererClass;
    public transient String entityRendererClassName;
    public transient Function<Object, Object> entityRendererFactory;
    public transient EntityType<?> entityRendererType;
    private transient final Map<String, Function<ContextUtils.DynamicOverrideContext<Entity>, Object>> dynamicOverrides = new LinkedHashMap<>();

    public CustomEntityBuilder(ResourceLocation i, Class<? extends Entity> entityClass) {
        super(i);
        this.entityClass = EntityReflection.createEntityClass(entityClass);
        if (Mob.class.isAssignableFrom(entityClass)) {
            this.eggItem = new SpawnEggItemBuilder(id, this)
                    .backgroundColor(0)
                    .highlightColor(0);
        }
    }

    @Info(value = "Indicates that no egg item should be created for this entity type")
    public CustomEntityBuilder noEggItem() {
        this.noEggItem = true;
        return this;
    }

    @Info(value = "Creates a spawn egg item for this entity type")
    public CustomEntityBuilder eggItem(Consumer<SpawnEggItemBuilder> eggItem) {
        this.eggItem = new SpawnEggItemBuilder(id, this);
        eggItem.accept(this.eggItem);
        this.noEggItem = false;
        return this;
    }

    @Info(value = """
            Uses a factory to create this entity's EntityModel.

            Example:
            ```javascript
            entityBuilder.setModel(context => {
                return new MyModel(context.bakeLayer(MyModel.LAYER_LOCATION))
            })
            ```
            """, params = {
            @Param(name = "entityModelFactory", value = "Function that returns the EntityModel to use for rendering.")
    })
    public CustomEntityBuilder setModel(Function<Object, Object> entityModelFactory) {
        this.entityModelFactory = CallbackInvoker.wrapFunction(entityModelFactory);
        this.entityRendererClass = null;
        this.entityRendererClassName = null;
        this.entityRendererFactory = null;
        this.entityRendererType = null;
        return this;
    }

    @Info(value = """
            Uses an EntityRenderer class for this entity.

            Example:
            ```javascript
            let EvokerFangsRenderer = Java.loadClass("net.minecraft.client.renderer.entity.EvokerFangsRenderer")
            entityBuilder.setRendererClass(EvokerFangsRenderer)
            ```
            """, params = {
            @Param(name = "entityRendererClass", value = "The EntityRenderer class to instantiate for rendering.")
    })
    public CustomEntityBuilder setRendererClass(Class<?> entityRendererClass) {
        if (!EntityReflection.validateRendererClassCompatibility(id, entityClass, entityRendererClass)) {
            return this;
        }
        this.entityRendererClass = entityRendererClass;
        this.entityRendererClassName = null;
        this.entityRendererFactory = null;
        this.entityModelFactory = null;
        this.entityRendererType = null;
        return this;
    }

    @Info(value = """
            Uses an EntityRenderer class name for this entity.

            Example:
            ```javascript
            entityBuilder.setRendererClass("net.minecraft.client.renderer.entity.CreeperRenderer")
            ```
            """, params = {
            @Param(name = "entityRendererClassName", value = "The fully qualified EntityRenderer class name to instantiate for rendering.")
    })
    public CustomEntityBuilder setRendererClass(String entityRendererClassName) {
        String resolvedClassName = EntityReflection.normalizeClassName(entityRendererClassName);
        if (resolvedClassName == null || resolvedClassName.isBlank()) {
            EntityReflection.resolveClassName(id, "renderer", resolvedClassName);
            return this;
        }
        Class<?> entityRendererClass = EntityReflection.resolveClassName(id, "renderer", resolvedClassName);
        if (entityRendererClass == null && EntityReflection.isClientEnvironment()) {
            return this;
        }
        if (entityRendererClass != null && !EntityReflection.validateRendererClassCompatibility(id, entityClass, entityRendererClass)) {
            return this;
        }
        this.entityRendererClass = entityRendererClass;
        this.entityRendererClassName = resolvedClassName;
        this.entityRendererFactory = null;
        this.entityModelFactory = null;
        this.entityRendererType = null;
        return this;
    }

    @Info(value = """
            Uses a factory to create this entity's EntityRenderer.

            Example:
            ```javascript
            entityBuilder.setRenderer(context => {
                return new MyRenderer(context.rendererContext)
            })
            ```
            """, params = {
            @Param(name = "entityRendererFactory", value = "Function that returns the EntityRenderer to use for rendering.")
    })
    public CustomEntityBuilder setRenderer(Function<Object, Object> entityRendererFactory) {
        this.entityRendererFactory = CallbackInvoker.wrapFunction(entityRendererFactory);
        this.entityRendererClass = null;
        this.entityRendererClassName = null;
        this.entityModelFactory = null;
        this.entityRendererType = null;
        return this;
    }

    @Info(value = """
            Reuses the renderer registered for another entity type.

            Example:
            ```javascript
            entityBuilder.setRendererFromType(EntityType.CREEPER)
            ```
            """, params = {
            @Param(name = "entityType", value = "The entity type whose registered renderer should be reused.")
    })
    public CustomEntityBuilder setRendererFromType(EntityType<?> entityType) {
        EntityType<?> rendererType = Objects.requireNonNull(entityType, "entityType");
        if (!EntityReflection.validateRendererTypeCompatibility(id, entityClass, rendererType)) {
            return this;
        }
        this.entityRendererType = rendererType;
        this.entityRendererClass = null;
        this.entityRendererClassName = null;
        this.entityRendererFactory = null;
        this.entityModelFactory = null;
        return this;
    }

    @Info(value = """
            Overrides a non-final instance method on the entity superclass.

            Example:
            ```javascript
            entityBuilder.override("isPushable()", context => false)
            ```
            """, params = {
            @Param(name = "methodKey", value = "Method key such as tick() or isPushable()."),
            @Param(name = "callback", value = "Function that receives the dynamic override context.")
    })
    public CustomEntityBuilder override(String methodKey, Function<ContextUtils.DynamicOverrideContext<Entity>, Object> callback) {
        DynamicOverrideMethodCatalog.validateOverrideKey(entityClass, methodKey);
        var methodSpec = DynamicOverrideMethodCatalog.resolve(entityClass, methodKey);
        if (methodSpec != null && methodSpec.parameterNames().length > 0) {
            ConsoleJS.STARTUP.info("[EntityJS]: Dynamic override '" + id + "#" + methodKey + "' argument names: " + Arrays.toString(methodSpec.parameterNames()));
        }
        String runtimeKey = methodSpec == null ? methodKey : methodSpec.key();
        dynamicOverrides.put(runtimeKey, CallbackInvoker.wrapFunction(Objects.requireNonNull(callback, "callback")));
        return this;
    }

    @HideFromJS
    public Function<ContextUtils.DynamicOverrideContext<Entity>, Object> getDynamicOverride(String methodKey) {
        return dynamicOverrides.get(methodKey);
    }

    @HideFromJS
    public List<String> dynamicOverrideKeys() {
        return List.copyOf(dynamicOverrides.keySet());
    }

    @HideFromJS
    public Class<? extends Entity> getEntityClass() {
        return entityClass;
    }

    @HideFromJS
    public boolean hasDynamicOverrides() {
        return !dynamicOverrides.isEmpty();
    }

    @HideFromJS
    public Entity createBaseEntity(EntityType<?> type, Level world) {
        if (entityClass == null) {
            throw new IllegalStateException("Entity class not set! Call .set(Class<T>) before using this builder.");
        }
        try {
            return entityClass.getDeclaredConstructor(EntityType.class, Level.class).newInstance(type, world);
        } catch (Exception e) {
            throw new RuntimeException("Failed to dynamically instantiate entity: " + id, e);
        }
    }

    @HideFromJS
    public boolean isLivingEntityClass() {
        return LivingEntity.class.isAssignableFrom(entityClass);
    }

    @HideFromJS
    public boolean usesEntityModelRenderer() {
        return entityModelFactory != null;
    }

    @HideFromJS
    public boolean usesEntityTypeRenderer() {
        return entityRendererType != null;
    }

    @HideFromJS
    public EntityType<?> getEntityRendererType() {
        return entityRendererType;
    }

    @HideFromJS
    public boolean usesCustomRenderer() {
        return entityRendererClass != null || entityRendererClassName != null || entityRendererFactory != null;
    }

    @HideFromJS
    public Object createEntityRenderer(Object rendererContext) {
        if (entityRendererFactory != null) {
            return entityRendererFactory.apply(new ContextUtils.EntityRendererFactoryContext(rendererContext, this));
        }
        Class<?> rendererClass = resolveRendererClass();
        if (rendererClass == null) {
            return null;
        }
        try {
            var constructor = EntityReflection.findRendererConstructor(rendererClass, rendererContext.getClass());
            if (constructor == null) {
                throw new NoSuchMethodException("No compatible EntityRendererProvider.Context constructor found.");
            }
            return constructor.newInstance(rendererContext);
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate custom EntityRenderer for entity: " + id, e);
        }
    }

    @HideFromJS
    public Object createEntityModel(Object rendererContext) {
        if (entityModelFactory != null) {
            return entityModelFactory.apply(new ContextUtils.EntityModelFactoryContext(rendererContext, this));
        }
        return null;
    }

    private Class<?> resolveRendererClass() {
        if (entityRendererClass != null || entityRendererClassName == null) {
            return entityRendererClass;
        }
        Class<?> resolvedClass = EntityReflection.resolveClassName(id, "renderer", entityRendererClassName, true);
        if (resolvedClass != null && EntityReflection.validateRendererClassCompatibility(id, entityClass, resolvedClass)) {
            entityRendererClass = resolvedClass;
        }
        return entityRendererClass;
    }

    @HideFromJS
    @Override
    public void createAdditionalObjects() {
        if (noEggItem || !Mob.class.isAssignableFrom(entityClass)) return;
        RegistryInfo.ITEM.addBuilder(eggItem);
    }

    @Override
    public AttributeSupplier.Builder getAttributeBuilder() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE)
                .add(Attributes.FOLLOW_RANGE)
                .add(Attributes.ARMOR_TOUGHNESS)
                .add(Attributes.SPAWN_REINFORCEMENTS_CHANCE)
                .add(Attributes.ATTACK_KNOCKBACK)
                .add(Attributes.FLYING_SPEED)
                .add(Attributes.JUMP_STRENGTH)
                .add(Attributes.LUCK)
                .add(Attributes.ATTACK_SPEED)
                .add(Attributes.KNOCKBACK_RESISTANCE)
                .add(Attributes.ARMOR);
    }


    @Override
    public EntityType.EntityFactory<? extends Entity> factory() {
        return (type, world) -> {
            if (hasDynamicOverrides() || Modifier.isAbstract(entityClass.getModifiers())) {
                return DynamicOverrideEntityFactory.create(this, type, world);
            }
            return createBaseEntity(type, world);
        };
    }
}
