package net.liopyu.entityjs.util.implementation;

import dev.latvian.mods.rhino.util.RemapPrefixForJS;
import net.liopyu.entityjs.builders.misc.CustomEntityBuilder;
import net.minecraft.world.entity.Entity;

import java.util.function.Consumer;

@RemapPrefixForJS("entityJs$")
public interface IRegistryJS {
    CustomEntityBuilder entityJs$createCustom(String id, Class<? extends Entity> entityClass);
}
