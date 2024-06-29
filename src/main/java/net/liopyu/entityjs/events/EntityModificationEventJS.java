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

    private EntityModificationEventJS(EntityType<?> entityType) {
        this.builder = determineModificationType(entityType);
    }

    public static EntityModificationEventJS getOrCreate(EntityType<?> entityType) {
        return eventMap.computeIfAbsent(entityType, EntityModificationEventJS::new);
    }

    public Object getBuilder() {
        return builder;
    }

    public void modify(EntityType<?> entityType, Consumer<? extends ModifyEntityBuilder> consumer) {
        Object builder = getOrCreate(entityType).getBuilder();
        EntityJSHelperClass.logWarningMessageOnce(builder.getClass().toString());
        if (builder instanceof ModifyTamableAnimalBuilder && consumer instanceof Consumer<?>) {
            ((Consumer<ModifyTamableAnimalBuilder>) consumer).accept((ModifyTamableAnimalBuilder) builder);
        } else if (builder instanceof ModifyAnimalBuilder && consumer instanceof Consumer<?>) {
            ((Consumer<ModifyAnimalBuilder>) consumer).accept((ModifyAnimalBuilder) builder);
        } else if (builder instanceof ModifyAgeableMobBuilder && consumer instanceof Consumer<?>) {
            ((Consumer<ModifyAgeableMobBuilder>) consumer).accept((ModifyAgeableMobBuilder) builder);
        } else if (builder instanceof ModifyPathfinderMobBuilder && consumer instanceof Consumer<?>) {
            ((Consumer<ModifyPathfinderMobBuilder>) consumer).accept((ModifyPathfinderMobBuilder) builder);
        } else if (builder instanceof ModifyMobBuilder && consumer instanceof Consumer<?>) {
            ((Consumer<ModifyMobBuilder>) consumer).accept((ModifyMobBuilder) builder);
        } else if (builder instanceof ModifyLivingEntityBuilder && consumer instanceof Consumer<?>) {
            ((Consumer<ModifyLivingEntityBuilder>) consumer).accept((ModifyLivingEntityBuilder) builder);
        } else if (builder instanceof ModifyEntityBuilder && consumer instanceof Consumer<?>) {
            ((Consumer<ModifyEntityBuilder>) consumer).accept((ModifyEntityBuilder) builder);
        } else {
            throw new IllegalArgumentException("Unsupported builder type or consumer type.");
        }
    }

    public Object determineModificationType(EntityType<?> entity) {
        if (TamableAnimal.class.isAssignableFrom(entity.getBaseClass())) {
            return new ModifyTamableAnimalBuilder(entity);
        } else if (Animal.class.isAssignableFrom(entity.getBaseClass())) {
            return new ModifyAnimalBuilder(entity);
        } else if (AgeableMob.class.isAssignableFrom(entity.getBaseClass())) {
            return new ModifyAgeableMobBuilder(entity);
        } else if (PathfinderMob.class.isAssignableFrom(entity.getBaseClass())) {
            return new ModifyPathfinderMobBuilder(entity);
        } else if (Mob.class.isAssignableFrom(entity.getBaseClass())) {
            return new ModifyMobBuilder(entity);
        } else if (LivingEntity.class.isAssignableFrom(entity.getBaseClass())) {
            return new ModifyLivingEntityBuilder(entity);
        } else {
            return new ModifyEntityBuilder(entity);
        }
    }
}
