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

public class Wrappers {

    // TODO: REMOVE THIS
    public static SoundEvent soundEvent(Object unknown) {
        if (unknown instanceof ResourceLocation || unknown instanceof CharSequence) {
            return Registry.SOUND_EVENT.get(new ResourceLocation(unknown.toString()));
        } else if (unknown instanceof SoundEvent event) {
            return event;
        }

        return null;
    }
}
