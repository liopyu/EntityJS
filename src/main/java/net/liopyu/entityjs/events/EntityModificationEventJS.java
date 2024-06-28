package net.liopyu.entityjs.events;

import dev.latvian.mods.kubejs.event.EventJS;
import net.liopyu.entityjs.builders.living.modification.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Animal;

public class EntityModificationEventJS extends EventJS {
    public final Entity entity;
    private final ModifyEntityBuilder event;

    public EntityModificationEventJS(Entity entity) {
        this.entity = entity;
        event = determineModificationType(entity);
    }

    public static ModifyEntityBuilder determineModificationType(Entity entity) {
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

    public enum EntityModificationType {
        ENTITY,
        LIVING_ENTITY,
        MOB,
        PATHFINDER_MOB,
        ANIMAL,
        AGEABLEMOB,
        TAMABLE
    }
}
