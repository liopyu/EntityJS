package net.liopyu.entityjs.builders.misc;

import net.liopyu.entityjs.entities.living.entityjs.IAnimatableJSCustom;
import net.minecraft.world.entity.LivingEntity;

import java.util.concurrent.ConcurrentHashMap;

public class EntityReflection {
    private static final ConcurrentHashMap<Class<? extends LivingEntity>, Class<? extends LivingEntity>> subclassCache = new ConcurrentHashMap<>();

    /**
     * Creates and stores a subclass of the provided base entity class that implements IAnimatableJSCustom.
     * If the entity already implements IAnimatableJSCustom, it returns the original class.
     *
     * @param baseClass The entity class to extend
     * @return The stored class implementing IAnimatableJSCustom
     */
    public static Class<? extends LivingEntity> createEntityClass(Class<? extends LivingEntity> baseClass) {
        if (IAnimatableJSCustom.class.isAssignableFrom(baseClass)) {
            return baseClass;
        }

        return subclassCache.computeIfAbsent(baseClass, EntityReflection::getSubclassInstance);
    }

    /**
     * Uses Reflection to create a subclass at runtime.
     *
     * @param baseClass The base entity class
     * @return A dynamically grab the subclass implementing IAnimatableJSCustom
     */
    public static Class<? extends LivingEntity> getSubclassInstance(Class<? extends LivingEntity> baseClass) {
        return baseClass;
    }
}