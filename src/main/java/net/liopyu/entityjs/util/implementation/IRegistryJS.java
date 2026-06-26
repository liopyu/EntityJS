package net.liopyu.entityjs.util.implementation;

import dev.latvian.mods.kubejs.util.KubeResourceLocation;
import dev.latvian.mods.rhino.util.RemapPrefixForJS;
import net.liopyu.entityjs.builders.misc.CustomEntityBuilder;
import net.liopyu.entityjs.builders.modification.ModifyEntityBuilder;
import net.minecraft.world.entity.Entity;

import java.util.function.Consumer;

@RemapPrefixForJS("entityJs$")
public interface IRegistryJS {
    CustomEntityBuilder entityJs$createCustom(KubeResourceLocation id, Class<? extends Entity> entityClass, Consumer<ModifyEntityBuilder> consumer);

    CustomEntityBuilder entityJs$createCustom(KubeResourceLocation id, String entityClassName, Consumer<ModifyEntityBuilder> consumer);
}
