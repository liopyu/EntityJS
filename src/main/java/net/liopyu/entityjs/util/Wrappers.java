package net.liopyu.entityjs.util;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

// Move usages of Registry#<registry>#get() to RegistryInfo#<registry>#getValue() in 1.20+
public class Wrappers {

    // TODO: Investigate if these are handled by kube since they're registry objects
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
        } else if (unknown instanceof Activity activity) {
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

    public static Item getItemFromObject(Object item) {
        if (item instanceof Item stack) {
            return stack;
        } else if (item instanceof ResourceLocation || item instanceof CharSequence) {
            return ForgeRegistries.ITEMS.getValue(new ResourceLocation(item.toString()));
        }
        return null;
    }

    public static ItemStack getItemStackFromObject(Object item) {
        if (item instanceof ItemStack stack) {
            return stack;
        } else if (item instanceof ResourceLocation || item instanceof CharSequence) {
            return Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(new ResourceLocation(item.toString()))).getDefaultInstance();
        }
        return null;
    }
/*public static Goal.Flag flag(Object unknown) {
    if (unknown instanceof ResourceLocation || unknown instanceof CharSequence) {
        return Goal.Flag.
    } else if (unknown instanceof Goal.Flag flag) {
        return flag;
    }
}*/

}
