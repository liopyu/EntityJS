package net.liopyu.entityjs.events;

import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.liopyu.entityjs.builders.modification.ModifyEntityBuilder;
import net.liopyu.entityjs.builders.modification.ModifyLivingEntityBuilder;
import net.liopyu.entityjs.builders.modification.ModifyMobBuilder;
import net.liopyu.entityjs.builders.modification.ModifyPathfinderMobBuilder;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class EntityModificationEventJS extends EventJS {
    public static final Map<EntityType<?>, EntityModificationEventJS> eventMap = new HashMap<>();
    private final Object builder;
    private final Entity entity;

    public EntityModificationEventJS(EntityType<?> entityType, Entity entity) {
        this.entity = entity;
        this.builder = determineModificationType(entityType, entity);
    }


    public static EntityModificationEventJS getOrCreate(EntityType<?> entityType, Entity entity) {
        if (!eventMap.containsKey(entityType)) {
            var event = new EntityModificationEventJS(entityType, entity);
            eventMap.put(entityType, event);
            return event;
        }
        return eventMap.get(entityType);
    }

    @HideFromJS
    public Object getBuilder() {
        return builder;
    }

    @Info(value = """
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
        /*if (builder instanceof ModifyTamableAnimalBuilder) {
            ((Consumer<ModifyTamableAnimalBuilder>) modifyBuilder).accept((ModifyTamableAnimalBuilder) builder);
        } else if (builder instanceof ModifyAnimalBuilder) {
            ((Consumer<ModifyAnimalBuilder>) modifyBuilder).accept((ModifyAnimalBuilder) builder);
        } else if (builder instanceof ModifyAgeableMobBuilder) {
            ((Consumer<ModifyAgeableMobBuilder>) modifyBuilder).accept((ModifyAgeableMobBuilder) builder);
        } else */
        if (builder instanceof ModifyPathfinderMobBuilder) {
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

    public ModifyEntityBuilder determineModificationType(EntityType<?> type, Entity entity) {
        /*if (entity instanceof TamableAnimal) {
            return new ModifyTamableAnimalBuilder(type);
        } else if (entity instanceof Animal) {
            return new ModifyAnimalBuilder(type);
        } else if (entity instanceof AgeableMob) {
            return new ModifyAgeableMobBuilder(type);
        } else */
        if (entity instanceof PathfinderMob) {
            return new ModifyPathfinderMobBuilder(type);
        } else if (entity instanceof Mob) {
            return new ModifyMobBuilder(type);
        } else if (entity instanceof LivingEntity) {
            return new ModifyLivingEntityBuilder(type);
        } else {
            return new ModifyEntityBuilder(type);
        }
    }
}