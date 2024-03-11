package net.liopyu.entityjs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import net.liopyu.entityjs.builders.living.AnimalEntityJSBuilder;
import net.liopyu.entityjs.builders.living.BaseLivingEntityJSBuilder;
import net.liopyu.entityjs.builders.living.MobEntityJSBuilder;
import net.liopyu.entityjs.builders.living.TameableMobJSBuilder;
import net.liopyu.entityjs.builders.nonliving.ArrowEntityJSBuilder;
import net.liopyu.entityjs.builders.nonliving.BaseEntityJSBuilder;
import net.liopyu.entityjs.builders.nonliving.ProjectileEntityJSBuilder;
import net.liopyu.entityjs.util.EntityJSUtils;
import net.liopyu.entityjs.util.EventHandlers;

public class EntityJSPlugin extends KubeJSPlugin {

    @Override
    public void init() {
        RegistryInfo.ENTITY_TYPE.addType("entityjs:living", BaseLivingEntityJSBuilder.class, BaseLivingEntityJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("entityjs:animal", AnimalEntityJSBuilder.class, AnimalEntityJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("entityjs:tamable", TameableMobJSBuilder.class, TameableMobJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("entityjs:mob", MobEntityJSBuilder.class, MobEntityJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("entityjs:arrow", ArrowEntityJSBuilder.class, ArrowEntityJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("entityjs:projectile", ProjectileEntityJSBuilder.class, ProjectileEntityJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("entityjs:nonliving", BaseEntityJSBuilder.class, BaseEntityJSBuilder::new);
    }

    @Override
    public void registerBindings(BindingsEvent event) {
        event.add("EntityJSUtils", EntityJSUtils.class);
    }

    @Override
    public void registerEvents() {
        EventHandlers.EntityJSEvents.register();
    }
}
