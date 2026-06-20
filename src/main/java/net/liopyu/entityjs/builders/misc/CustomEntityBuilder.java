package net.liopyu.entityjs.builders.misc;

import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.typings.Generics;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.liopyu.entityjs.item.SpawnEggItemBuilder;
import net.liopyu.entityjs.util.ContextUtils;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;
import java.util.function.Function;


public class CustomEntityBuilder extends CustomEntityJSBuilder {

    private final Class<? extends Entity> entityClass;
    public transient SpawnEggItemBuilder eggItem;
    public transient boolean noEggItem = false;
    public transient Class<? extends EntityModel> entityModelClass;
    public transient Function<ContextUtils.EntityModelFactoryContext, EntityModel<? extends Entity>> entityModelFactory;
    public transient Class<? extends EntityRenderer> entityRendererClass;
    public transient Function<ContextUtils.EntityRendererFactoryContext, EntityRenderer<? extends Entity>> entityRendererFactory;

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
    @Generics(value = {Mob.class, SpawnEggItemBuilder.class})
    public CustomEntityBuilder eggItem(Consumer<SpawnEggItemBuilder> eggItem) {
        this.eggItem = new SpawnEggItemBuilder(id, this);
        eggItem.accept(this.eggItem);
        return this;
    }

    @Info(value = """
            Sets an exact EntityModel class for this custom entity renderer.
            This works for living and non-living custom entities, and uses the normal EntityModel render path instead of GeckoLib.
            The class must provide a no-argument constructor.

            Example usage:
            ```javascript
            let MyModel = Java.loadClass("com.example.client.MyEntityModel")
            entityBuilder.setModelClass(MyModel);
            ```
            """, params = {
            @Param(name = "entityModelClass", value = "The EntityModel class to instantiate for rendering.")
    })
    public CustomEntityBuilder setModelClass(Class<? extends EntityModel> entityModelClass) {
        this.entityModelClass = entityModelClass;
        this.entityModelFactory = null;
        this.entityRendererClass = null;
        this.entityRendererFactory = null;
        return this;
    }

    @Info(value = """
            Sets an EntityModel factory for this custom entity renderer.
            This works for living and non-living custom entities, and uses the normal EntityModel render path instead of GeckoLib.
            Use this for EntityModel classes that need constructor arguments or baked model layers.

            Example usage:
            ```javascript
            entityBuilder.setModel(context => {
                return new MyModel(context.bakeLayer(MyModel.LAYER_LOCATION));
            });
            ```
            """, params = {
            @Param(name = "entityModelFactory", value = "Function that returns the EntityModel to use for rendering.")
    })
    public CustomEntityBuilder setModel(Function<ContextUtils.EntityModelFactoryContext, EntityModel<? extends Entity>> entityModelFactory) {
        this.entityModelFactory = entityModelFactory;
        this.entityModelClass = null;
        this.entityRendererClass = null;
        this.entityRendererFactory = null;
        return this;
    }

    @Info(value = """
            Sets an exact EntityRenderer class for this custom entity.
            Use this when the renderer owns entity-specific animation or render behavior that a generic EntityModel cannot reproduce.
            The class must provide a constructor that accepts EntityRendererProvider.Context.

            Example usage:
            ```javascript
            let EvokerFangsRenderer = Java.loadClass("net.minecraft.client.renderer.entity.EvokerFangsRenderer")
            entityBuilder.setRendererClass(EvokerFangsRenderer);
            ```
            """, params = {
            @Param(name = "entityRendererClass", value = "The EntityRenderer class to instantiate for rendering.")
    })
    public CustomEntityBuilder setRendererClass(Class<? extends EntityRenderer> entityRendererClass) {
        this.entityRendererClass = entityRendererClass;
        this.entityRendererFactory = null;
        this.entityModelClass = null;
        this.entityModelFactory = null;
        return this;
    }

    @Info(value = """
            Sets an EntityRenderer factory for this custom entity.
            Use this for renderers that need constructor arguments or custom setup.

            Example usage:
            ```javascript
            entityBuilder.setRenderer(context => {
                return new MyRenderer(context.rendererContext);
            });
            ```
            """, params = {
            @Param(name = "entityRendererFactory", value = "Function that returns the EntityRenderer to use for rendering.")
    })
    public CustomEntityBuilder setRenderer(Function<ContextUtils.EntityRendererFactoryContext, EntityRenderer<? extends Entity>> entityRendererFactory) {
        this.entityRendererFactory = entityRendererFactory;
        this.entityRendererClass = null;
        this.entityModelClass = null;
        this.entityModelFactory = null;
        return this;
    }

    @HideFromJS
    public boolean isLivingEntityClass() {
        return LivingEntity.class.isAssignableFrom(entityClass);
    }

    @HideFromJS
    public boolean usesEntityModelRenderer() {
        return entityModelClass != null || entityModelFactory != null;
    }

    @HideFromJS
    public boolean usesCustomRenderer() {
        return entityRendererClass != null || entityRendererFactory != null;
    }

    @HideFromJS
    public EntityRenderer<? extends Entity> createEntityRenderer(EntityRendererProvider.Context rendererContext) {
        if (entityRendererFactory != null) {
            return entityRendererFactory.apply(new ContextUtils.EntityRendererFactoryContext(rendererContext, this));
        }
        if (entityRendererClass == null) {
            return null;
        }
        try {
            return entityRendererClass.getDeclaredConstructor(EntityRendererProvider.Context.class).newInstance(rendererContext);
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate custom EntityRenderer for entity: " + id, e);
        }
    }

    @HideFromJS
    public EntityModel<Entity> createEntityModel(EntityRendererProvider.Context rendererContext) {
        if (entityModelFactory != null) {
            return (EntityModel<Entity>) entityModelFactory.apply(new ContextUtils.EntityModelFactoryContext(rendererContext, this));
        }
        if (entityModelClass == null) {
            return null;
        }
        try {
            return (EntityModel<Entity>) entityModelClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate custom EntityModel for entity: " + id, e);
        }
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
                .add(Attributes.MOVEMENT_SPEED)
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
            if (entityClass == null) {
                throw new IllegalStateException("Entity class not set! Call .set(Class<T>) before using this builder.");
            }
            try {
                return entityClass.getDeclaredConstructor(EntityType.class, Level.class).newInstance(type, world);
            } catch (Exception e) {
                throw new RuntimeException("Failed to dynamically instantiate entity: " + id, e);
            }
        };
    }
}
