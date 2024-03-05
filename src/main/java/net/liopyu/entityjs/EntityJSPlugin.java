package net.liopyu.entityjs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import net.liopyu.entityjs.builders.*;
import net.liopyu.entityjs.util.EventHandlers;
import net.minecraft.world.InteractionResult;

public class EntityJSPlugin extends KubeJSPlugin {

    @Override
    public void init() {
        RegistryInfo.ENTITY_TYPE.addType("entityjs:living", BaseLivingEntityJSBuilder.class, BaseLivingEntityJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("entityjs:animal", AnimalEntityJSBuilder.class, AnimalEntityJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("entityjs:mob", MobEntityJSBuilder.class, MobEntityJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("entityjs:tamable", TameableMobJSBuilder.class, TameableMobJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("entityjs:arrow", ArrowEntityJSBuilder.class, ArrowEntityJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("entityjs:projectile", ProjectileEntityJSBuilder.class, ProjectileEntityJSBuilder::new);
    }

    @Override
    public void registerBindings(BindingsEvent event) {
        event.add("InteractionResult", InteractionResult.class);
    }

    @Override
    public void registerEvents() {
        EventHandlers.EntityJSEvents.register();
    }
}
