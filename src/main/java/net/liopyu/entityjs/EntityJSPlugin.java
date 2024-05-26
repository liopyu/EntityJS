package net.liopyu.entityjs;

import com.mojang.logging.LogUtils;
import dev.architectury.registry.level.entity.EntityAttributeRegistry;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.liopyu.entityjs.builders.living.BaseLivingEntityBuilder;
import net.liopyu.entityjs.builders.living.entityjs.*;
import net.liopyu.entityjs.builders.living.vanilla.*;
import net.liopyu.entityjs.builders.nonliving.entityjs.ArrowEntityJSBuilder;
import net.liopyu.entityjs.builders.nonliving.entityjs.BaseEntityJSBuilder;
import net.liopyu.entityjs.builders.nonliving.entityjs.ProjectileEntityJSBuilder;
import net.liopyu.entityjs.builders.nonliving.vanilla.BoatJSBuilder;
import net.liopyu.entityjs.builders.nonliving.vanilla.EyeOfEnderJSBuilder;
import net.liopyu.entityjs.util.EntityJSUtils;
import net.liopyu.entityjs.util.EventHandlers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public class EntityJSPlugin extends KubeJSPlugin {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static int customEntities = 0;
    public static boolean modifiedAttributes = false;

    @Override
    public void init() {
        RegistryInfo.ENTITY_TYPE.addType("entityjs:nonliving", BaseEntityJSBuilder.class, BaseEntityJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("entityjs:living", BaseLivingEntityJSBuilder.class, BaseLivingEntityJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("entityjs:mob", MobEntityJSBuilder.class, MobEntityJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("entityjs:animal", AnimalEntityJSBuilder.class, AnimalEntityJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("entityjs:watercreature", WaterEntityJSBuilder.class, WaterEntityJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("entityjs:tamable", TameableMobJSBuilder.class, TameableMobJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("entityjs:arrow", ArrowEntityJSBuilder.class, ArrowEntityJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("entityjs:projectile", ProjectileEntityJSBuilder.class, ProjectileEntityJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("minecraft:zombie", ZombieJSBuilder.class, ZombieJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("minecraft:allay", AllayJSBuilder.class, AllayJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("minecraft:axolotl", AxolotlJSBuilder.class, AxolotlJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("minecraft:bat", BatJSBuilder.class, BatJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("minecraft:bee", BeeJSBuilder.class, BeeJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("minecraft:blaze", BlazeJSBuilder.class, BlazeJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("minecraft:boat", BoatJSBuilder.class, BoatJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("minecraft:camel", CamelJSBuilder.class, CamelJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("minecraft:cat", CatJSBuilder.class, CatJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("minecraft:chicken", ChickenJSBuilder.class, ChickenJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("minecraft:cow", CowJSBuilder.class, CowJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("minecraft:creeper", CreeperJSBuilder.class, CreeperJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("minecraft:dolphin", DolphinJSBuilder.class, DolphinJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("minecraft:donkey", DonkeyJSBuilder.class, DonkeyJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("minecraft:enderman", EnderManJSBuilder.class, EnderManJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("minecraft:evoker", EvokerJSBuilder.class, EvokerJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("minecraft:ghast", GhastJSBuilder.class, GhastJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("minecraft:goat", GoatJSBuilder.class, GoatJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("minecraft:guardian", GuardianJSBuilder.class, GuardianJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("minecraft:horse", HorseJSBuilder.class, HorseJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("minecraft:illusioner", IllusionerJSBuilder.class, IllusionerJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("minecraft:iron_golem", IronGolemJSBuilder.class, IronGolemJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("minecraft:panda", PandaJSBuilder.class, PandaJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("minecraft:parrot", ParrotJSBuilder.class, ParrotJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("minecraft:eye_of_ender", EyeOfEnderJSBuilder.class, EyeOfEnderJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("minecraft:piglin", PiglinJSBuilder.class, PiglinJSBuilder::new);
        RegistryInfo.ENTITY_TYPE.addType("minecraft:wither", WitherJSBuilder.class, WitherJSBuilder::new);

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
