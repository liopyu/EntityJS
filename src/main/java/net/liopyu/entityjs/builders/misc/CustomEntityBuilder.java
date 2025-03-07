package net.liopyu.entityjs.builders.misc;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;


public class CustomEntityBuilder extends CustomEntityJSBuilder {

    private final Class<?> entityClass;

    public CustomEntityBuilder(ResourceLocation i, Class<? extends LivingEntity> entityClass) {
        super(i);
        this.entityClass = EntityReflection.createEntityClass(entityClass);
    }

    @Override
    public AttributeSupplier.Builder getAttributeBuilder() {
        return Mob.createMobAttributes()
                //HEALTH
                .add(Attributes.MAX_HEALTH)
                //SPEED
                .add(Attributes.MOVEMENT_SPEED)
                //ATTACK
                .add(Attributes.ATTACK_DAMAGE)
                //FOLLOW RANGE
                .add(Attributes.FOLLOW_RANGE)
                //ARMOR
                .add(Attributes.ARMOR);
    }


    @Override
    public EntityType.EntityFactory<? extends LivingEntity> factory() {
        return (type, world) -> {
            if (entityClass == null) {
                throw new IllegalStateException("Entity class not set! Call .set(Class<T>) before using this builder.");
            }
            try {
                return (LivingEntity) entityClass.getDeclaredConstructor(EntityType.class, Level.class).newInstance(type, world);
            } catch (Exception e) {
                throw new RuntimeException("Failed to dynamically instantiate entity: " + id, e);
            }
        };
    }
}
