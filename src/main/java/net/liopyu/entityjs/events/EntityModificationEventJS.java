package net.liopyu.entityjs.events;

import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.liopyu.entityjs.builders.modification.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.Projectile;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class EntityModificationEventJS implements KubeEvent {
    public static final Map<EntityType<?>, EntityModificationEventJS> eventMap = new HashMap<>();
    private final Object builder;
    private Entity entity = null;
    private final Class<? extends Entity> entityClass;

    /*public EntityModificationEventJS(EntityType<?> entityType, Entity entity) {
        this.entity = entity;
        this.builder = determineModificationType(entityType, entity);
    }*/

    public EntityModificationEventJS(EntityType<?> entityType, Class<? extends Entity> entity) {
        this.entityClass = entity;
        this.builder = determineModificationType(entityType, entityClass);
    }

    public static EntityModificationEventJS getOrCreate(EntityType<?> entityType, Class<? extends Entity> entity) {
        if (!eventMap.containsKey(entityType)) {
            var event = new EntityModificationEventJS(entityType, entity);
            eventMap.put(entityType, event);
            return event;
        }
        return eventMap.get(entityType);
    }

    /*public static EntityModificationEventJS getOrCreate(EntityType<?> entityType, Entity entity) {
        if (!eventMap.containsKey(entityType)) {
            var event = new EntityModificationEventJS(entityType, entity);
            eventMap.put(entityType, event);
            return event;
        }
        return eventMap.get(entityType);
    }*/

    @HideFromJS
    public Object getBuilder() {
        return builder;
    }


    /* @Info(value = """
             Entity type modification event. Allows modification of methods for any existing entity.\s
             \s
             This event determines the entity's type and uses the appropriate builder for modification.\s
             \s
             Builders:\s
                 - ModifyPathfinderMobBuilder: For entities extending {@link PathfinderMob}\s
                 - ModifyMobBuilder: For entities extending {@link Mob}\s
                 - ModifyLivingEntityBuilder: For entities extending {@link LivingEntity}\s
                 - ModifyEntityBuilder: For entities extending {@link Entity}\s
             \s
             Example usage:\s
             ```javascript
             EntityJSEvents.modifyEntity(event => {
                 event.modify("minecraft:zombie", builder => {
                     builder.onRemovedFromWorld(entity => {
                         // Execute code when the zombie is removed from the world.
                     })
                 })
             })
             ```
             """, params = {
             @Param(name = "entityType", value = "The entity type to modify"),
             @Param(name = "modifyBuilder", value = "A consumer to modify the entity type."),
     })
     public void modify(EntityType<?> entityType, Consumer<? extends ModifyEntityBuilder> modifyBuilder) {
         var entity = this.entity;
         boolean entityTypeMatch = entityType == entity.getType();
         if (!entityTypeMatch) return;
         Object builder = getOrCreate(entityType, entity).getBuilder();
         if (builder instanceof ModifyProjectileBuilder) {
             ((Consumer<ModifyProjectileBuilder>) modifyBuilder).accept((ModifyProjectileBuilder) builder);
         } else if (builder instanceof ModifyPathfinderMobBuilder) {
             ((Consumer<ModifyPathfinderMobBuilder>) modifyBuilder).accept((ModifyPathfinderMobBuilder) builder);
         } else if (builder instanceof ModifyMobBuilder) {
             ((Consumer<ModifyMobBuilder>) modifyBuilder).accept((ModifyMobBuilder) builder);
         } else if (builder instanceof ModifyLivingEntityBuilder) {
             ((Consumer<ModifyLivingEntityBuilder>) modifyBuilder).accept((ModifyLivingEntityBuilder) builder);
         } else if (builder instanceof ModifyEntityBuilder) {
             ((Consumer<ModifyEntityBuilder>) modifyBuilder).accept((ModifyEntityBuilder) builder);
         } else {
             throw new IllegalArgumentException("Unsupported builder type or consumer type.");
         }
     }*/
    @Info(value = """
            Entity type modification event. Allows modification of methods for any existing entity.
            
            This event determines the entity's type and uses the appropriate builder for modification.
            
            Builders:
                - ModifyPathfinderMobBuilder: For entities extending {@link PathfinderMob}
                - ModifyMobBuilder: For entities extending {@link Mob}
                - ModifyLivingEntityBuilder: For entities extending {@link LivingEntity}
                - ModifyEntityBuilder: For entities extending {@link Entity}
            
            Example usage:
            ```javascript
            EntityJSEvents.modifyEntity(event => {
                event.modify("minecraft:zombie", builder => {
                    builder.onRemovedFromWorld(entity => {
                        // Execute code when the zombie is removed from the world.
                    })
                })
            })
            ```
            """, params = {
            @Param(name = "entityType", value = "The entity type to modify"),
            @Param(name = "modifyBuilder", value = "A consumer to modify the entity type."),
    })
    public void modify(EntityType<?> entityType, Consumer<? extends ModifyEntityBuilder> modifyBuilder) {
        Object builder = this.getBuilder();

        // Determine whether to compare against instance entity or just class
        if (entity != null) {
            if (entity.getType() != entityType) return;
        } else if (entityClass != null) {
            Class<?> expectedClass = entityType.getBaseClass(); // If NeoForge exposes this
            if (expectedClass != null && !expectedClass.equals(entityClass)) return;
        } else {
            return;
        }

        if (builder instanceof ModifyProjectileBuilder) {
            ((Consumer<ModifyProjectileBuilder>) modifyBuilder).accept((ModifyProjectileBuilder) builder);
        } else if (builder instanceof ModifyPathfinderMobBuilder) {
            ((Consumer<ModifyPathfinderMobBuilder>) modifyBuilder).accept((ModifyPathfinderMobBuilder) builder);
        } else if (builder instanceof ModifyMobBuilder) {
            ((Consumer<ModifyMobBuilder>) modifyBuilder).accept((ModifyMobBuilder) builder);
        } else if (builder instanceof ModifyLivingEntityBuilder) {
            ((Consumer<ModifyLivingEntityBuilder>) modifyBuilder).accept((ModifyLivingEntityBuilder) builder);
        } else if (builder instanceof ModifyEntityBuilder) {
            ((Consumer<ModifyEntityBuilder>) modifyBuilder).accept((ModifyEntityBuilder) builder);
        } else {
            throw new IllegalArgumentException("Unsupported builder type or consumer type.");
        }
    }

    public ModifyEntityBuilder determineModificationType(EntityType<?> type, Class<? extends Entity> entityClass) {
        if (PathfinderMob.class.isAssignableFrom(entityClass)) {
            return new ModifyPathfinderMobBuilder(type);
        } else if (Mob.class.isAssignableFrom(entityClass)) {
            return new ModifyMobBuilder(type);
        } else if (LivingEntity.class.isAssignableFrom(entityClass)) {
            return new ModifyLivingEntityBuilder(type);
        } else if (Projectile.class.isAssignableFrom(entityClass)) {
            return new ModifyProjectileBuilder(type);
        } else {
            return new ModifyEntityBuilder(type);
        }
    }

}