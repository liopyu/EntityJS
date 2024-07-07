package net.liopyu.entityjs.events;

import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import net.liopyu.entityjs.builders.modification.ModifyEntityBuilder;
import net.liopyu.entityjs.builders.modification.ModifyLivingEntityBuilder;
import net.liopyu.entityjs.builders.modification.ModifyMobBuilder;
import net.liopyu.entityjs.builders.modification.ModifyPathfinderMobBuilder;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.minecraft.world.entity.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class EntityModificationEventJS extends EventJS {
    public enum EntityModificationType {
        PATHFINDERMOB,
        MOB,
        LIVING_ENTITY,
        ENTITY
    }

    public static final Map<EntityType<?>, EntityModificationEventJS> eventMap = new HashMap<>();
    public static final Map<EntityType<?>, EntityModificationType> entityMap = new HashMap<>();
    private final Object builder;
    private final Entity entity;

    private EntityModificationEventJS(EntityType<?> entityType, Entity entity) {
        if (!eventMap.containsKey(entityType)) {
            eventMap.put(entityType, this);
        }
        this.entity = entity;
        this.builder = determineModificationType(entityType, entity);
    }

    public Entity getEntity() {
        return entity;
    }

    public static EntityModificationEventJS getOrCreate(EntityType<?> entityType, Entity entity) {
        if (!eventMap.containsKey(entityType)) {
            ConsoleJS.STARTUP.info("Creating Builder for: " + "EntityType: " + entityType + " Entity: " + entity);
            var event = new EntityModificationEventJS(entityType, entity);
            eventMap.put(entityType, event);
            return event;
        }
        ConsoleJS.STARTUP.info("Modifying: " + "EntityType: " + entityType + " Entity: " + entity);
        return eventMap.get(entityType);
    }

    public Object getBuilder() {
        return builder;
    }

    public ModifyEntityBuilder modify(EntityType<?> entityType) {
        Object builder = getOrCreate(entityType, entity).getBuilder();
        ConsoleJS.STARTUP.info(builder.getClass() + " :" + entityType + " -> " + ((ModifyEntityBuilder) builder).getEntity());
        /*if (builder instanceof ModifyTamableAnimalBuilder) {
            ((Consumer<ModifyTamableAnimalBuilder>) consumer).accept((ModifyTamableAnimalBuilder) builder);
        } else if (builder instanceof ModifyAnimalBuilder) {
            ((Consumer<ModifyAnimalBuilder>) consumer).accept((ModifyAnimalBuilder) builder);
        } else if (builder instanceof ModifyAgeableMobBuilder) {
            ((Consumer<ModifyAgeableMobBuilder>) consumer).accept((ModifyAgeableMobBuilder) builder);
        } else */
        if (builder instanceof ModifyPathfinderMobBuilder) {
            return (ModifyPathfinderMobBuilder) builder;
        } else if (builder instanceof ModifyMobBuilder) {
            return (ModifyMobBuilder) builder;
        } else if (builder instanceof ModifyLivingEntityBuilder) {
            return (ModifyLivingEntityBuilder) builder;
        } else if (builder instanceof ModifyEntityBuilder) {
            return (ModifyEntityBuilder) builder;
        } else {
            throw new IllegalArgumentException("Unsupported builder type or consumer type.");
        }
    }

    public ModifyEntityBuilder determineModificationType(EntityType<?> type, Entity entity) {
        ConsoleJS.STARTUP.info("[EntityJS]: " + entity + " is living entity? " + (entity instanceof LivingEntity));
        /*if (entity instanceof TamableAnimal) {
            return new ModifyTamableAnimalBuilder(type);
        } else if (entity instanceof Animal) {
            return new ModifyAnimalBuilder(type);
        } else if (entity instanceof AgeableMob) {
            return new ModifyAgeableMobBuilder(type);
        } else */
        if (entity instanceof PathfinderMob) {
            return new ModifyPathfinderMobBuilder(type, entity);
        } else if (entity instanceof Mob) {
            return new ModifyMobBuilder(type, entity);
        } else if (entity instanceof LivingEntity) {
            return new ModifyLivingEntityBuilder(type, entity);
        } else {
            return new ModifyEntityBuilder(type, entity);
        }
    }
}
