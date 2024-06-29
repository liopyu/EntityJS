package net.liopyu.entityjs.builders.living.modification;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

public class ModifyAgeableMobBuilder extends ModifyPathfinderMobBuilder {
    public ModifyAgeableMobBuilder(EntityType<?> entity) {
        super(entity);
    }
}
