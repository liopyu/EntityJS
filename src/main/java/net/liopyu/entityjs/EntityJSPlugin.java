package net.liopyu.entityjs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import net.liopyu.entityjs.builders.living.entityjs.AnimalEntityJSBuilder;
import net.liopyu.entityjs.builders.living.entityjs.BaseLivingEntityJSBuilder;
import net.liopyu.entityjs.builders.living.entityjs.MobEntityJSBuilder;
import net.liopyu.entityjs.builders.living.entityjs.TameableMobJSBuilder;
import net.liopyu.entityjs.builders.living.vanilla.*;
import net.liopyu.entityjs.builders.nonliving.entityjs.ArrowEntityJSBuilder;
import net.liopyu.entityjs.builders.nonliving.entityjs.BaseEntityJSBuilder;
import net.liopyu.entityjs.builders.nonliving.vanilla.BoatJSBuilder;
import net.liopyu.entityjs.builders.nonliving.entityjs.ProjectileEntityJSBuilder;
import net.liopyu.entityjs.util.EntityJSUtils;
import net.liopyu.entityjs.util.EventHandlers;

import java.io.IOException;

public class EntityJSPlugin extends KubeJSPlugin {

    @Override
    public void init() {
        ;
        RegistryInfo.ENTITY_TYPE.addType("entityjs:nonliving", BaseEntityJSBuilder.class, BaseEntityJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("entityjs:living", BaseLivingEntityJSBuilder.class, BaseLivingEntityJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("entityjs:mob", MobEntityJSBuilder.class, MobEntityJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("entityjs:animal", AnimalEntityJSBuilder.class, AnimalEntityJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("entityjs:tamable", TameableMobJSBuilder.class, TameableMobJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("entityjs:arrow", ArrowEntityJSBuilder.class, ArrowEntityJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("entityjs:projectile", ProjectileEntityJSBuilder.class, ProjectileEntityJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("minecraft:boat", BoatJSBuilder.class, BoatJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("minecraft:creeper", CreeperJSBuilder.class, CreeperJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("minecraft:zombie", ZombieJSBuilder.class, ZombieJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("minecraft:allay", AllayJSBuilder.class, AllayJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("minecraft:axolotl", AxolotlJSBuilder.class, AxolotlJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("minecraft:bat", BatJSBuilder.class, BatJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("minecraft:bee", BeeJSBuilder.class, BeeJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("minecraft:blaze", BlazeJSBuilder.class, BlazeJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("minecraft:camel", CamelJSBuilder.class, CamelJSBuilder::new);
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
