package net.liopyu.entityjs.mixin;

import net.liopyu.entityjs.util.implementation.EACAccess;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Map;

@Mixin(value = EntityAttributeCreationEvent.class, remap = true)
public class EntityAttributeCreationEventMixin implements EACAccess {
    @Final
    @Shadow
    private Map<EntityType<? extends LivingEntity>, AttributeSupplier> map;

    @Unique
    public Map<EntityType<? extends LivingEntity>, AttributeSupplier> entityJs$getMap() {
        return map;
    }
}