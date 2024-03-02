package net.liopyu.entityjs.util;

import com.mojang.serialization.Codec;
import net.liopyu.entityjs.EntityJSMod;
import net.liopyu.entityjs.util.implementation.EventBasedSpawnModifier;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class RegistryUtil {

    public static void init(IEventBus modBus) {
        BIOME_MODIFIERS.register(modBus);
        ModKeybinds.init();
    }

    private static final DeferredRegister<Codec<? extends BiomeModifier>> BIOME_MODIFIERS = DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, EntityJSMod.MOD_ID);

    public static final RegistryObject<Codec<EventBasedSpawnModifier>> EVENT_SPAWN_MODIFIER = BIOME_MODIFIERS.register("event_based", () -> Codec.unit(EventBasedSpawnModifier::new));

}
