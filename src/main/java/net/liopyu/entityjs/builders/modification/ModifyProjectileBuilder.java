package net.liopyu.entityjs.builders.modification;

import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.builders.nonliving.entityjs.ProjectileEntityBuilder;
import net.liopyu.entityjs.util.ContextUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class ModifyProjectileBuilder extends ModifyEntityBuilder {
    public transient Consumer<ContextUtils.ProjectileEntityHitContext> onHitEntity;
    public transient Consumer<ContextUtils.ProjectileBlockHitContext> onHitBlock;
    public transient Function<Entity, Object> canHitEntity;

    public ModifyProjectileBuilder(EntityType<?> entityType) {
        super(entityType);
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
    public ModifyProjectileBuilder onHitEntity(Consumer<ContextUtils.ProjectileEntityHitContext> consumer) {
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
    public ModifyProjectileBuilder onHitBlock(Consumer<ContextUtils.ProjectileBlockHitContext> consumer) {
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
    public ModifyProjectileBuilder canHitEntity(Function<Entity, Object> function) {
        canHitEntity = function;
        return this;
    }
}
