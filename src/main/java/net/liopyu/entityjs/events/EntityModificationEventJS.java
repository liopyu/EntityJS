package net.liopyu.entityjs.events;

import dev.latvian.mods.kubejs.event.EventJS;
import net.liopyu.entityjs.builders.living.modification.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Animal;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class EntityModificationEventJS extends EventJS {
    private final ModifyEntityBuilder event;
    public static final Map<EntityType<?>, EntityModificationEventJS> events = new HashMap<>();

    public EntityModificationEventJS(Entity entity) {
        event = determineModificationType(entity);
    }

    public static EntityModificationEventJS create(Entity entity) {
        return new EntityModificationEventJS(entity);
    }

    public ModifyEntityBuilder getEvent() {
        return event;
    }

    public EntityModificationEventJS modify(EntityType<?> entityType, Consumer<ModifyEntityBuilder> consumer) {
        events.put(entityType, consumer);
    }

    public ModifyEntityBuilder determineModificationType(Entity entity) {
        if (entity instanceof TamableAnimal) {
            return new ModifyTamableAnimalBuilder(entity);
        } else if (entity instanceof Animal) {
            return new ModifyAnimalBuilder(entity);
        } else if (entity instanceof AgeableMob) {
            return new ModifyAgeableMobBuilder(entity);
        } else if (entity instanceof PathfinderMob) {
            return new ModifyPathfinderMobBuilder(entity);
        } else if (entity instanceof Mob) {
            return new ModifyMobBuilder(entity);
        } else if (entity instanceof LivingEntity) {
            return new ModifyLivingEntityBuilder(entity);
        } else {
            return new ModifyEntityBuilder(entity);
        }
    }
}
