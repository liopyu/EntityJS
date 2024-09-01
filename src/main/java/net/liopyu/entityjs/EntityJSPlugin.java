package net.liopyu.entityjs;

import com.mojang.logging.LogUtils;
import dev.latvian.mods.kubejs.event.EventGroupRegistry;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
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
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.liopyu.entityjs.util.EntityJSUtils;
import net.liopyu.entityjs.util.EventHandlers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.Registries;
import org.slf4j.Logger;

public class EntityJSPlugin implements KubeJSPlugin {
    public static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void registerBuilderTypes(BuilderTypeRegistry registry) {
        registry.of(Registries.ENTITY_TYPE, reg -> {
            reg.add("entityjs:nonliving", BaseEntityJSBuilder.class, BaseEntityJSBuilder::new);
            reg.add("entityjs:living", BaseLivingEntityJSBuilder.class, BaseLivingEntityJSBuilder::new);
            reg.add("entityjs:mob", MobEntityJSBuilder.class, MobEntityJSBuilder::new);
            reg.add("entityjs:animal", AnimalEntityJSBuilder.class, AnimalEntityJSBuilder::new);
            reg.add("entityjs:watercreature", WaterEntityJSBuilder.class, WaterEntityJSBuilder::new);
            reg.add("entityjs:tamable", TameableMobJSBuilder.class, TameableMobJSBuilder::new);
            reg.add("entityjs:arrow", ArrowEntityJSBuilder.class, ArrowEntityJSBuilder::new);
            reg.add("entityjs:projectile", ProjectileEntityJSBuilder.class, ProjectileEntityJSBuilder::new);
            reg.add("entityjs:geckolib_projectile", ProjectileAnimatableJSBuilder.class, ProjectileAnimatableJSBuilder::new);
            reg.add("minecraft:zombie", ZombieJSBuilder.class, ZombieJSBuilder::new);
            reg.add("minecraft:allay", AllayJSBuilder.class, AllayJSBuilder::new);
            reg.add("minecraft:axolotl", AxolotlJSBuilder.class, AxolotlJSBuilder::new);
            reg.add("minecraft:bat", BatJSBuilder.class, BatJSBuilder::new);
            reg.add("minecraft:bee", BeeJSBuilder.class, BeeJSBuilder::new);
            reg.add("minecraft:blaze", BlazeJSBuilder.class, BlazeJSBuilder::new);
            reg.add("minecraft:boat", BoatJSBuilder.class, BoatJSBuilder::new);
            reg.add("minecraft:camel", CamelJSBuilder.class, CamelJSBuilder::new);
            reg.add("minecraft:cat", CatJSBuilder.class, CatJSBuilder::new);
            reg.add("minecraft:chicken", ChickenJSBuilder.class, ChickenJSBuilder::new);
            reg.add("minecraft:cow", CowJSBuilder.class, CowJSBuilder::new);
            reg.add("minecraft:creeper", CreeperJSBuilder.class, CreeperJSBuilder::new);
            reg.add("minecraft:dolphin", DolphinJSBuilder.class, DolphinJSBuilder::new);
            reg.add("minecraft:donkey", DonkeyJSBuilder.class, DonkeyJSBuilder::new);
            reg.add("minecraft:enderman", EnderManJSBuilder.class, EnderManJSBuilder::new);
            reg.add("minecraft:evoker", EvokerJSBuilder.class, EvokerJSBuilder::new);
            reg.add("minecraft:ghast", GhastJSBuilder.class, GhastJSBuilder::new);
            reg.add("minecraft:goat", GoatJSBuilder.class, GoatJSBuilder::new);
            reg.add("minecraft:guardian", GuardianJSBuilder.class, GuardianJSBuilder::new);
            reg.add("minecraft:horse", HorseJSBuilder.class, HorseJSBuilder::new);
            reg.add("minecraft:illusioner", IllusionerJSBuilder.class, IllusionerJSBuilder::new);
            reg.add("minecraft:iron_golem", IronGolemJSBuilder.class, IronGolemJSBuilder::new);
            reg.add("minecraft:panda", PandaJSBuilder.class, PandaJSBuilder::new);
            reg.add("minecraft:parrot", ParrotJSBuilder.class, ParrotJSBuilder::new);
            reg.add("minecraft:eye_of_ender", EyeOfEnderJSBuilder.class, EyeOfEnderJSBuilder::new);
            reg.add("minecraft:piglin", PiglinJSBuilder.class, PiglinJSBuilder::new);
            reg.add("minecraft:wither", WitherJSBuilder.class, WitherJSBuilder::new);
            reg.add("minecraft:slime", SlimeJSBuilder.class, SlimeJSBuilder::new);
        });

    }

    @Override
    public void registerBindings(BindingRegistry event) {
        event.add("EntityJSUtils", EntityJSUtils.class);
        event.add("RenderType", RenderType.class);
    }

    @Override
    public void registerEvents(EventGroupRegistry registry) {
        registry.register(EventHandlers.EntityJSEvents);
    }
}
