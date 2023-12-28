package net.liopyu.entityjs.util;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;

public class Wrappers {

    // Move usages of Registry#Attribute#get() to RegistryInfo#ATTRIBUTE#getValue() in 1.20+
    public static Attribute attribute(Object unknown) {
        if (unknown instanceof ResourceLocation rs) {
            return Registry.ATTRIBUTE.get(rs);
        } else if (unknown instanceof CharSequence cs) {
            return Registry.ATTRIBUTE.get(new ResourceLocation(cs.toString()));
        } else if (unknown instanceof Attribute attribute) {
            return attribute;
        }

        return null;
    }
}
