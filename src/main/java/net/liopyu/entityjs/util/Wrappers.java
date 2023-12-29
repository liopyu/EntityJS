package net.liopyu.entityjs.util;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.attributes.Attribute;

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
}
