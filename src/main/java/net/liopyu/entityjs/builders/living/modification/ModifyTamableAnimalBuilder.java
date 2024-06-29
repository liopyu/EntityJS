package net.liopyu.entityjs.builders.living.modification;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

public class ModifyTamableAnimalBuilder extends ModifyAnimalBuilder {
    public ModifyTamableAnimalBuilder(EntityType<?> entity) {
        super(entity);
    }
}
