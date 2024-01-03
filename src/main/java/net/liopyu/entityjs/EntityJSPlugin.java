package net.liopyu.entityjs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.rhino.util.wrap.TypeWrappers;
import net.liopyu.entityjs.builders.BaseEntityJSBuilder;
import net.liopyu.entityjs.builders.MobEntityJSBuilder;
import net.liopyu.entityjs.util.Wrappers;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;

public class EntityJSPlugin extends KubeJSPlugin {

    @Override
    public void init() {
        RegistryInfo.ENTITY_TYPE.addType("entityjs:living", BaseEntityJSBuilder.class, BaseEntityJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("entityjs:mob", MobEntityJSBuilder.class, MobEntityJSBuilder::new);
    }

    @Override
    public void registerTypeWrappers(ScriptType type, TypeWrappers typeWrappers) {
        typeWrappers.registerSimple(Attribute.class, Wrappers::attribute);
        typeWrappers.registerSimple(SoundEvent.class, Wrappers::soundEvent);
        typeWrappers.registerSimple(MemoryModuleType.class, Wrappers::memoryModuleType);
        typeWrappers.registerSimple(SensorType.class, Wrappers::sensorType);
    }
}
