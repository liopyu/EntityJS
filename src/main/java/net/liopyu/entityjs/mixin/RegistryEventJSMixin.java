package net.liopyu.entityjs.mixin;

import dev.latvian.mods.kubejs.KubeJS;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryEventJS;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.util.UtilsJS;
import net.liopyu.entityjs.builders.misc.CustomEntityBuilder;
import net.liopyu.entityjs.events.EntityRegistryEvent;
import net.liopyu.entityjs.util.EventHandlers;
import net.liopyu.entityjs.util.implementation.IRegistryJS;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Consumer;

@Mixin(RegistryEventJS.class)
public class RegistryEventJSMixin<T> implements IRegistryJS {
    @Final
    @Shadow
    private RegistryInfo<T> registry;
    @Final
    @Shadow
    public List<BuilderBase<? extends T>> created;

    public CustomEntityBuilder entityJs$createCustom(String id, Class<? extends Entity> entityClass) {
        if (!Entity.class.isAssignableFrom(entityClass)) {
            throw new IllegalArgumentException("Tried to create entity from a class that does not extend Entity. Id: " + id);
        }
        var rl = UtilsJS.getMCID(ScriptType.STARTUP.manager.get().context, KubeJS.appendModId(id));
        CustomEntityBuilder b = null;
        if (LivingEntity.class.isAssignableFrom(entityClass)) {
            b = new CustomEntityBuilder(rl, (Class<? extends LivingEntity>) entityClass);
            //consumer.accept(b);
        }
        if (b == null) {
            throw new IllegalArgumentException("CustomEntityBuilder is null for entity id: " + id);
        }
        registry.addBuilder((BuilderBase<? extends T>) b);
        created.add((BuilderBase<? extends T>) b);
        return b;
    }
}
