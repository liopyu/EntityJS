package net.liopyu.entityjs.builders.nonliving.entityjs;

import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.typings.Generics;
import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.builders.nonliving.BaseEntityBuilder;
import net.liopyu.entityjs.builders.nonliving.EntityTypeBuilder;
import net.liopyu.entityjs.builders.nonliving.NonAnimatableEntityTypeBuilder;
import net.liopyu.entityjs.entities.nonliving.entityjs.BaseEntityJS;
import net.liopyu.entityjs.entities.nonliving.entityjs.ProjectileAnimatableJS;
import net.liopyu.entityjs.entities.nonliving.entityjs.ProjectileEntityJS;
import net.liopyu.entityjs.item.ProjectileItemBuilder;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class ProjectileAnimatableJSBuilder extends BaseEntityBuilder<ProjectileAnimatableJS> {
    public transient Consumer<ContextUtils.ProjectileEntityHitContext> onHitEntity;
    public transient Consumer<ContextUtils.ProjectileBlockHitContext> onHitBlock;
    public transient Function<Entity, Object> canHitEntity;
    public transient Consumer<ContextUtils.CollidingProjectileEntityContext> onEntityCollision;
    public transient ProjectileItemBuilder item;
    public transient boolean noItem;

    public ProjectileAnimatableJSBuilder(ResourceLocation i) {
        super(i);
        this.item = (ProjectileItemBuilder) new ProjectileItemBuilder(id, this)
                .canThrow(true)
                .texture(i.getNamespace() + ":item/" + i.getPath());
    }


    @Info(value = "Indicates that no projectile item should be created for this entity type")
    public ProjectileAnimatableJSBuilder noItem() {
        this.noItem = true;
        return this;
    }

    @Info(value = "Creates the arrow item for this entity type")
    @Generics(value = BaseEntityBuilder.class)
    public ProjectileAnimatableJSBuilder item(Consumer<ProjectileItemBuilder> item) {
        this.item = new ProjectileItemBuilder(id, this);
        item.accept(this.item);
        return this;
    }

    @Override
    public void createAdditionalObjects() {
        if (!noItem) {
            RegistryInfo.ITEM.addBuilder(item);
        }
    }

    @Override
    public EntityType.EntityFactory<ProjectileAnimatableJS> factory() {
        return (type, level) -> new ProjectileAnimatableJS(this, type, level);
    }

    @Override
    public AttributeSupplier.Builder getAttributeBuilder() {
        //final AttributeSupplier.Builder builder = BaseEntityJS.createLivingAttributes();
        return null; //BaseEntityJS.createLivingAttributes();
    }

    @Override
    public EntityType<ProjectileAnimatableJS> createObject() {
        return new EntityTypeBuilder<>(this).get();
    }

    //Projectile Overrides

    @Info(value = """
            Sets a callback function to be executed when the projectile
            collides with an entity.
                        
            Example usage:
            ```javascript
            arrowEntityBuilder.onEntityCollision(context => {
                const { entity, target } = context
                console.log(entity)
            });
            ```
            """)
    public ProjectileAnimatableJSBuilder onEntityCollision(Consumer<ContextUtils.CollidingProjectileEntityContext> consumer) {
        onEntityCollision = consumer;
        return this;
    }

    @Info(value = """
            Sets a callback function to be executed when the projectile hits an entity.
            The provided Consumer accepts a {@link ContextUtils.ProjectileEntityHitContext} parameter,
            representing the context of the projectile's interaction with a specific entity.
                        
            Example usage:
            ```javascript
            projectileBuilder.onHitEntity(context -> {
                // Custom logic to handle the projectile hitting an entity.
                // Access information about the entity and projectile using the provided context.
            });
            ```
            """)
    public ProjectileAnimatableJSBuilder onHitEntity(Consumer<ContextUtils.ProjectileEntityHitContext> consumer) {
        onHitEntity = consumer;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when the projectile hits a block.
            The provided Consumer accepts a {@link ContextUtils.ProjectileBlockHitContext} parameter,
            representing the context of the projectile's interaction with a specific block.
                        
            Example usage:
            ```javascript
            projectileBuilder.onHitBlock(context -> {
                // Custom logic to handle the projectile hitting a block.
                // Access information about the block and projectile using the provided context.
            });
            ```
            """)
    public ProjectileAnimatableJSBuilder onHitBlock(Consumer<ContextUtils.ProjectileBlockHitContext> consumer) {
        onHitBlock = consumer;
        return this;
    }

    @Info(value = """
            Sets a function to determine if the projectile entity can hit a specific entity.
                        
            @param canHitEntity The predicate to check if the arrow can hit the entity.
                        
            Example usage:
            ```javascript
            projectileEntityBuilder.canHitEntity(entity -> {
                // Custom logic to determine if the projectile can hit the specified entity
                // Return true if the arrow can hit, false otherwise.
            });
            ```
            """)
    public ProjectileAnimatableJSBuilder canHitEntity(Function<Entity, Object> function) {
        canHitEntity = function;
        return this;
    }
}
