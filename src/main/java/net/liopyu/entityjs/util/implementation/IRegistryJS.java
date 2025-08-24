package net.liopyu.entityjs.util.implementation;

import dev.latvian.mods.rhino.util.RemapPrefixForJS;
import net.liopyu.entityjs.builders.misc.CustomEntityBuilder;
import net.liopyu.entityjs.builders.modification.ModifyEntityBuilder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Consumer;

@RemapPrefixForJS("entityJs$")
public interface IRegistryJS {
    CustomEntityBuilder entityJs$createCustom(String id, Class<? extends LivingEntity> entityClass, Consumer<ModifyEntityBuilder> consumer);
}
