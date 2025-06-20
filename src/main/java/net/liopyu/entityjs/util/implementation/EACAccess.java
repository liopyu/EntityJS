package net.liopyu.entityjs.util.implementation;

import dev.latvian.mods.rhino.util.RemapPrefixForJS;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

import java.util.Map;

@RemapPrefixForJS("entityJs$")
public interface EACAccess {
    Map<EntityType<? extends LivingEntity>, AttributeSupplier> entityJs$getMap();
}