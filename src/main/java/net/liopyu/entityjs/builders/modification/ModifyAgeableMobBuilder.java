package net.liopyu.entityjs.builders.modification;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public class ModifyAgeableMobBuilder extends ModifyPathfinderMobBuilder {
    public ModifyAgeableMobBuilder(EntityType<?> entityType, Entity entity) {
        super(entityType, entity);
    }
}
