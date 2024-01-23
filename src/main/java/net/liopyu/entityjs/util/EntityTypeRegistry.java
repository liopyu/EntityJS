package net.liopyu.entityjs.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.AgeableMob;

import java.util.HashMap;
import java.util.Map;

public class EntityTypeRegistry {
    private static final Map<ResourceLocation, EntityType<? extends AgeableMob>> entityTypes = new HashMap<>();

    public static void registerEntityType(ResourceLocation location, EntityType<? extends AgeableMob> entityType) {
        entityTypes.put(location, entityType);
    }

    public static EntityType<? extends AgeableMob> getEntityType(ResourceLocation location) {
        return entityTypes.get(location);
    }
}
