package net.liopyu.entityjs.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.liopyu.entityjs.EntityJSMod;
import net.liopyu.entityjs.util.implementation.EventBasedSpawnModifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class RegistryUtil {

    public static void init(IEventBus modBus) {
        BIOME_MODIFIERS.register(modBus);
    }

    private static final DeferredRegister<MapCodec<? extends BiomeModifier>> BIOME_MODIFIERS = DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, EntityJSMod.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends BiomeModifier>, MapCodec<EventBasedSpawnModifier>> EVENT_SPAWN_MODIFIER = BIOME_MODIFIERS.register("event_based", () -> MapCodec.assumeMapUnsafe(Codec.unit(EventBasedSpawnModifier::new)));

}
