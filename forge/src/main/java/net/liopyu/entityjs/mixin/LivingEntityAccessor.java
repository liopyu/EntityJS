package net.liopyu.entityjs.mixin;

import dev.latvian.mods.rhino.util.RemapPrefixForJS;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
@RemapPrefixForJS("entityJs$")
@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Invoker("brainProvider")
    Brain.Provider<?> entityJs$brainProvider();
}
