package net.liopyu.entityjs.util;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

// Move usages of Registry#<registry>#get() to RegistryInfo#<registry>#getValue() in 1.20+
public class Wrappers {

    public static Attribute attribute(Object unknown) {
        if (unknown instanceof ResourceLocation || unknown instanceof CharSequence) {
            return Registry.ATTRIBUTE.get(new ResourceLocation(unknown.toString()));
        } else if (unknown instanceof Attribute attribute) {
            return attribute;
        }

        return null;
    }

    public static SoundEvent soundEvent(Object unknown) {
        if (unknown instanceof ResourceLocation || unknown instanceof CharSequence) {
            return Registry.SOUND_EVENT.get(new ResourceLocation(unknown.toString()));
        } else if (unknown instanceof SoundEvent event) {
            return event;
        }

        return null;
    }

    // Hopefully it works...
    // Probably won't work for non-vanilla types in the current implementation
    public static MemoryModuleType<?> memoryModuleType(Object unknown) {
        if (unknown instanceof ResourceLocation || unknown instanceof CharSequence) {
            return Registry.MEMORY_MODULE_TYPE.get(new ResourceLocation(unknown.toString()));
        } else if (unknown instanceof MemoryModuleType<?> memory) {
            return memory;
        }

        return null;
    }

    public static SensorType<?> sensorType(Object unknown) {
        if (unknown instanceof ResourceLocation || unknown instanceof CharSequence) {
            return Registry.SENSOR_TYPE.get(new ResourceLocation(unknown.toString()));
        } else if (unknown instanceof SensorType<?> sensor) {
            return sensor;
        }

        return null;
    }

    public static Activity activity(Object unknown) {
        if (unknown instanceof ResourceLocation || unknown instanceof CharSequence) {
            return Registry.ACTIVITY.get(new ResourceLocation(unknown.toString()));
        } else if (unknown instanceof Activity activity)  {
            return activity;
        }

        return null;
    }

    public static EntityType<?> entityType(Object unknown) {
        if (unknown instanceof ResourceLocation || unknown instanceof CharSequence) {
            return Registry.ENTITY_TYPE.get(new ResourceLocation(unknown.toString()));
        } else if (unknown instanceof EntityType<?> type) {
            return type;
        } else if (unknown instanceof Entity entity) {
            return entity.getType();
        }

        return null;
    }
}
