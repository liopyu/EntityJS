package net.liopyu.entityjs.events;

import dev.latvian.mods.kubejs.event.EventJS;
import net.liopyu.entityjs.builders.living.modification.*;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Animal;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class EntityModificationEventJS extends EventJS {
    public static final Map<EntityType<?>, EntityModificationEventJS> eventMap = new HashMap<>();
    private final Object builder;
    private final Entity entity;

    private EntityModificationEventJS(EntityType<?> entityType, Entity entity) {
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

    public Object getBuilder() {
        return builder;
    }

    public void modify(EntityType<?> entityType, Consumer<? extends ModifyEntityBuilder> consumer) {
        Object builder = getOrCreate(entityType, entity).getBuilder();
        //EntityJSHelperClass.logWarningMessageOnce(builder.getClass().toString());
        /*if (builder instanceof ModifyTamableAnimalBuilder) {
            ((Consumer<ModifyTamableAnimalBuilder>) consumer).accept((ModifyTamableAnimalBuilder) builder);
        } else if (builder instanceof ModifyAnimalBuilder) {
            ((Consumer<ModifyAnimalBuilder>) consumer).accept((ModifyAnimalBuilder) builder);
        } else if (builder instanceof ModifyAgeableMobBuilder) {
            ((Consumer<ModifyAgeableMobBuilder>) consumer).accept((ModifyAgeableMobBuilder) builder);
        } else */
        if (builder instanceof ModifyPathfinderMobBuilder) {
            ((Consumer<ModifyPathfinderMobBuilder>) consumer).accept((ModifyPathfinderMobBuilder) builder);
        } else if (builder instanceof ModifyMobBuilder) {
            ((Consumer<ModifyMobBuilder>) consumer).accept((ModifyMobBuilder) builder);
        } else if (builder instanceof ModifyLivingEntityBuilder) {
            ((Consumer<ModifyLivingEntityBuilder>) consumer).accept((ModifyLivingEntityBuilder) builder);
        } else if (builder instanceof ModifyEntityBuilder) {
            ((Consumer<ModifyEntityBuilder>) consumer).accept((ModifyEntityBuilder) builder);
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
