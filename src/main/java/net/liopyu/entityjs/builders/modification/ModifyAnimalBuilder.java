package net.liopyu.entityjs.builders.modification;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

public class ModifyAnimalBuilder extends ModifyAgeableMobBuilder {
    public ModifyAnimalBuilder(EntityType<?> entity) {
        super(entity);
    }
}
