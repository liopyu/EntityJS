package net.liopyu.entityjs.util;

import com.google.common.collect.ImmutableList;
import com.mojang.authlib.Environment;
import com.mojang.logging.LogUtils;
import dev.architectury.platform.Platform;
import dev.architectury.registry.level.entity.EntityAttributeRegistry;
import dev.architectury.registry.level.entity.SpawnPlacementsRegistry;
import dev.latvian.mods.kubejs.bindings.event.StartupEvents;
import dev.latvian.mods.kubejs.event.*;
import dev.latvian.mods.kubejs.registry.RegistryCallback;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.script.data.VirtualKubeJSDataPack;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import dev.latvian.mods.kubejs.util.UtilsJS;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.liopyu.entityjs.EntityJSMod;
import net.liopyu.entityjs.builders.living.BaseLivingEntityBuilder;
import net.liopyu.entityjs.builders.living.entityjs.BaseLivingEntityJSBuilder;
import net.liopyu.entityjs.events.*;
import net.liopyu.entityjs.mixin.AttributeSupplierAccessor;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EventHandlers {

    public static final EventGroup EntityJSEvents = EventGroup.of("EntityJSEvents");

    public static final EventHandler addGoalTargets = EntityJSEvents.server("addGoals", () -> AddGoalTargetsEventJS.class).extra(Extra.REQUIRES_ID); // Possibly a modify goals event for editing other entities
    public static final EventHandler addGoalSelectors = EntityJSEvents.server("addGoalSelectors", () -> AddGoalSelectorsEventJS.class).extra(Extra.REQUIRES_ID);
    public static final EventHandler buildBrain = EntityJSEvents.server("buildBrain", () -> BuildBrainEventJS.class).extra(Extra.REQUIRES_ID);
    public static final EventHandler buildBrainProvider = EntityJSEvents.server("buildBrainProvider", () -> BuildBrainProviderEventJS.class).extra(Extra.REQUIRES_ID);
    //public static final EventHandler biomeSpawns = EntityJSEvents.server("biomeSpawns", () -> BiomeSpawnsEventJS.class);

    public static final EventHandler editAttributes = EntityJSEvents.startup("attributes", () -> ModifyAttributeEventJS.class);
    //public static final EventHandler spawnPlacement = EntityJSEvents.startup("spawnPlacement", () -> RegisterSpawnPlacementsEventJS.class);
    public static final EventHandler modifyEntity = EntityJSEvents.startup("modifyEntity", () -> EntityModificationEventJS.class);


    public static void init() {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            RegistryEntryAddedCallback.event(BuiltInRegistries.ENTITY_TYPE).register((rawId, id, type) -> {
                BaseLivingEntityBuilder<?> hit = null;
                for (BaseLivingEntityBuilder<?> b : BaseLivingEntityBuilder.thisList) {
                    if (b.get() == type) {
                        hit = b; break;
                    }
                }
                if (hit == null) return;

                AttributeSupplier.Builder base = hit.getAttributeBuilder(); // base only
                FabricDefaultAttributeRegistry.register(UtilsJS.cast(type), base);
            });
        }

        DynamicRegistrySetupCallback.EVENT.register(Event.DEFAULT_PHASE, ctx -> {

            if (editAttributes.hasListeners()) {
                editAttributes.post(new ModifyAttributeEventJS());
            }
            for (BaseLivingEntityBuilder<?> b : BaseLivingEntityBuilder.thisList) {
                EntityType<? extends LivingEntity> type = UtilsJS.cast(b.get());
                var id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
                AttributeSupplier.Builder merged = b.getAttributeBuilder();

                Map<Attribute, Double> adds = ModifyAttributeEventJS.pendingAdds.get(type);
                int n = adds == null ? 0 : adds.size();

                if (n > 0) {
                    for (Map.Entry<Attribute, Double> e : adds.entrySet()) {
                        Attribute a = e.getKey();
                        Double v = e.getValue();
                        if (v == null || v.isNaN()) {
                            merged.add(a);
                        } else {
                            merged.add(a, v);
                        }
                    }
                }
                FabricDefaultAttributeRegistry.register(type, merged);
            }
        });
    }
    /*private static void registerSpawnPlacements() {
        for (BaseLivingEntityBuilder<?> builder : BaseLivingEntityBuilder.spawnList) {
            SpawnPlacementsRegistry.register(() -> UtilsJS.cast(builder.get()), builder.placementType, builder.heightMap, UtilsJS.cast(builder.spawnPredicate)); // Cast because the '?' generics makes the event unhappy
        }
        if (spawnPlacement.hasListeners()) {
            spawnPlacement.post(new RegisterSpawnPlacementsEventJS());
        }
    }*/

    public static void postDataEvent(VirtualKubeJSDataPack pack, MultiPackResourceManager multiManager) {
        if (pack != null && multiManager != null) {
            // Unused
        }
    }
}