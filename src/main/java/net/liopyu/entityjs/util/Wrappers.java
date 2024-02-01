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

    public static Item[] getItemFromObject(Object item) {
        if (item instanceof Item stack) {
            // If the object is already an instance of Item, return it in an array.
            return new Item[]{stack};
        } else if (item instanceof ResourceLocation || item instanceof CharSequence) {
            // If the object is a ResourceLocation or CharSequence, convert it to a ResourceLocation.
            ResourceLocation resourceLocation = new ResourceLocation(item.toString());

            // Retrieve the Item from the ForgeRegistries.ITEMS using the ResourceLocation and return it in an array.
            Item resolvedItem = ForgeRegistries.ITEMS.getValue(resourceLocation);
            return resolvedItem != null ? new Item[]{resolvedItem} : new Item[]{};
        } else if (item instanceof Object[]) {
            // If the object is an array, cast it to Object[] and return it directly.
            Object[] array = (Object[]) item;
            Item[] result = new Item[array.length];

            for (int i = 0; i < array.length; i++) {
                if (array[i] instanceof Item) {
                    result[i] = (Item) array[i];
                } else if (array[i] instanceof ResourceLocation || array[i] instanceof CharSequence) {
                    ResourceLocation resourceLocation = new ResourceLocation(array[i].toString());
                    result[i] = ForgeRegistries.ITEMS.getValue(resourceLocation);
                }
            }

            return result;
        }

        // If none of the conditions are met, return an empty array.
        return new Item[]{};
    }


    public static ItemStack getItemStackFromObject(Object item) {
        if (item instanceof ItemStack stack) {
            return stack;
        } else if (item instanceof ResourceLocation || item instanceof CharSequence) {
            return Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(new ResourceLocation(item.toString()))).getDefaultInstance();
        }
        return null;
    }


}
