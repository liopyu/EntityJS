package net.liopyu.entityjs;

import com.mojang.logging.LogUtils;
import dev.latvian.mods.kubejs.event.EventGroupRegistry;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.registry.BuilderFactory;
import dev.latvian.mods.kubejs.registry.BuilderTypeRegistry;
import dev.latvian.mods.kubejs.script.BindingRegistry;
import net.liopyu.entityjs.builders.living.entityjs.*;
import net.liopyu.entityjs.builders.living.vanilla.*;
import net.liopyu.entityjs.builders.nonliving.entityjs.ArrowEntityJSBuilder;
import net.liopyu.entityjs.builders.nonliving.entityjs.BaseEntityJSBuilder;
import net.liopyu.entityjs.builders.nonliving.entityjs.ProjectileAnimatableJSBuilder;
import net.liopyu.entityjs.builders.nonliving.entityjs.ProjectileEntityJSBuilder;
import net.liopyu.entityjs.builders.nonliving.vanilla.BoatJSBuilder;
import net.liopyu.entityjs.builders.nonliving.vanilla.EyeOfEnderJSBuilder;
import net.liopyu.entityjs.builders.nonliving.vanilla.TridentJSBuilder;
import net.liopyu.entityjs.util.EntityJSUtils;
import net.liopyu.entityjs.util.EventHandlers;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;

import static net.liopyu.entityjs.util.EntityJSHelperClass.isLegacyKubeJS;

public class EntityJSPlugin implements KubeJSPlugin {
    public static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void registerBuilderTypes(BuilderTypeRegistry registry) {
        registry.of(Registries.ENTITY_TYPE, reg -> {
            boolean useRL = !isLegacyKubeJS();
            addType(reg, "entityjs:nonliving", BaseEntityJSBuilder.class, BaseEntityJSBuilder::new, useRL);
            addType(reg, "entityjs:living", BaseLivingEntityJSBuilder.class, BaseLivingEntityJSBuilder::new, useRL);
            addType(reg, "entityjs:mob", MobEntityJSBuilder.class, MobEntityJSBuilder::new, useRL);
            addType(reg, "entityjs:animal", AnimalEntityJSBuilder.class, AnimalEntityJSBuilder::new, useRL);
            addType(reg, "entityjs:watercreature", WaterEntityJSBuilder.class, WaterEntityJSBuilder::new, useRL);
            addType(reg, "entityjs:tamable", TameableMobJSBuilder.class, TameableMobJSBuilder::new, useRL);
            addType(reg, "entityjs:arrow", ArrowEntityJSBuilder.class, ArrowEntityJSBuilder::new, useRL);
            addType(reg, "entityjs:projectile", ProjectileEntityJSBuilder.class, ProjectileEntityJSBuilder::new, useRL);
            addType(reg, "entityjs:geckolib_projectile", ProjectileAnimatableJSBuilder.class, ProjectileAnimatableJSBuilder::new, useRL);
            addType(reg, "minecraft:zombie", ZombieJSBuilder.class, ZombieJSBuilder::new, useRL);
            addType(reg, "minecraft:allay", AllayJSBuilder.class, AllayJSBuilder::new, useRL);
            addType(reg, "minecraft:axolotl", AxolotlJSBuilder.class, AxolotlJSBuilder::new, useRL);
            addType(reg, "minecraft:bat", BatJSBuilder.class, BatJSBuilder::new, useRL);
            addType(reg, "minecraft:bee", BeeJSBuilder.class, BeeJSBuilder::new, useRL);
            addType(reg, "minecraft:blaze", BlazeJSBuilder.class, BlazeJSBuilder::new, useRL);
            addType(reg, "minecraft:boat", BoatJSBuilder.class, BoatJSBuilder::new, useRL);
            addType(reg, "minecraft:camel", CamelJSBuilder.class, CamelJSBuilder::new, useRL);
            addType(reg, "minecraft:cat", CatJSBuilder.class, CatJSBuilder::new, useRL);
            addType(reg, "minecraft:chicken", ChickenJSBuilder.class, ChickenJSBuilder::new, useRL);
            addType(reg, "minecraft:cow", CowJSBuilder.class, CowJSBuilder::new, useRL);
            addType(reg, "minecraft:creeper", CreeperJSBuilder.class, CreeperJSBuilder::new, useRL);
            addType(reg, "minecraft:dolphin", DolphinJSBuilder.class, DolphinJSBuilder::new, useRL);
            addType(reg, "minecraft:donkey", DonkeyJSBuilder.class, DonkeyJSBuilder::new, useRL);
            addType(reg, "minecraft:enderman", EnderManJSBuilder.class, EnderManJSBuilder::new, useRL);
            addType(reg, "minecraft:evoker", EvokerJSBuilder.class, EvokerJSBuilder::new, useRL);
            addType(reg, "minecraft:ghast", GhastJSBuilder.class, GhastJSBuilder::new, useRL);
            addType(reg, "minecraft:goat", GoatJSBuilder.class, GoatJSBuilder::new, useRL);
            addType(reg, "minecraft:guardian", GuardianJSBuilder.class, GuardianJSBuilder::new, useRL);
            addType(reg, "minecraft:horse", HorseJSBuilder.class, HorseJSBuilder::new, useRL);
            addType(reg, "minecraft:illusioner", IllusionerJSBuilder.class, IllusionerJSBuilder::new, useRL);
            addType(reg, "minecraft:iron_golem", IronGolemJSBuilder.class, IronGolemJSBuilder::new, useRL);
            addType(reg, "minecraft:panda", PandaJSBuilder.class, PandaJSBuilder::new, useRL);
            addType(reg, "minecraft:parrot", ParrotJSBuilder.class, ParrotJSBuilder::new, useRL);
            addType(reg, "minecraft:eye_of_ender", EyeOfEnderJSBuilder.class, EyeOfEnderJSBuilder::new, useRL);
            addType(reg, "minecraft:piglin", PiglinJSBuilder.class, PiglinJSBuilder::new, useRL);
            addType(reg, "minecraft:wither", WitherJSBuilder.class, WitherJSBuilder::new, useRL);
            addType(reg, "minecraft:slime", SlimeJSBuilder.class, SlimeJSBuilder::new, useRL);
            addType(reg, "minecraft:wolf", WolfJSBuilder.class, WolfJSBuilder::new, useRL);
            addType(reg, "minecraft:skeleton", SkeletonJSBuilder.class, SkeletonJSBuilder::new, useRL);
            addType(reg, "minecraft:trident", TridentJSBuilder.class, TridentJSBuilder::new, useRL);
        });
    }

    private static void addType(Object reg, String id, Class<?> builderClass, BuilderFactory factory, boolean useRL) {
        try {
            var m = reg.getClass().getDeclaredMethod("add", useRL ? Identifier.class : String.class, Class.class, BuilderFactory.class);
            m.setAccessible(true);
            Object key = useRL ? Identifier.parse(id) : id;
            m.invoke(reg, key, builderClass, factory);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void registerBindings(BindingRegistry event) {
        event.add("EntityJSUtils", EntityJSUtils.class);
    }

    @Override
    public void registerEvents(EventGroupRegistry registry) {
        registry.register(EventHandlers.EntityJSEvents);
    }
}
