package net.liopyu.entityjs.events;

import dev.latvian.mods.kubejs.KubeJS;
import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.util.UtilsJS;
import net.liopyu.entityjs.builders.misc.CustomEntityBuilder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;
import java.util.function.Consumer;

public class EntityRegistryEvent<T> extends EventJS {

    private final RegistryInfo<T> registry;

    private final List<BuilderBase<? extends T>> created;


    public EntityRegistryEvent(RegistryInfo<T> registry, List<BuilderBase<? extends T>> created) {
        this.registry = registry;
        this.created = created;
    }

}
