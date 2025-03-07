package net.liopyu.entityjs.mixin;

import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryEventJS;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import net.liopyu.entityjs.events.EntityRegistryEvent;
import net.liopyu.entityjs.util.EventHandlers;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(RegistryEventJS.class)
public class RegistryEventJSMixin<T> {
    @Final
    @Shadow
    private RegistryInfo<T> registry;
    @Final
    @Shadow
    public List<BuilderBase<? extends T>> created;

    @Inject(method = "<init>", at = @At("RETURN"), remap = true)
    private void entityjs$onEntityInit(RegistryInfo<T> r, CallbackInfo ci) {
        if (EventHandlers.registry.hasListeners()) {
            EventHandlers.registry.post(new EntityRegistryEvent<>(registry, created));
        }
    }
}
