package net.liopyu.entityjs.util;

import com.google.common.collect.ImmutableList;
import dev.architectury.registry.level.entity.EntityAttributeRegistry;
import dev.architectury.registry.level.entity.SpawnPlacementsRegistry;
import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.event.Extra;
import dev.latvian.mods.kubejs.script.data.VirtualKubeJSDataPack;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import dev.latvian.mods.kubejs.util.UtilsJS;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.liopyu.entityjs.EntityJSMod;
import net.liopyu.entityjs.builders.living.BaseLivingEntityBuilder;
import net.liopyu.entityjs.events.*;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EventHandlers {

    public static final EventGroup EntityJSEvents = EventGroup.of("EntityJSEvents");

    public static final EventHandler addGoalTargets = EntityJSEvents.server("addGoals", () -> AddGoalTargetsEventJS.class).extra(Extra.REQUIRES_ID); // Possibly a modify goals event for editing other entities
    public static final EventHandler addGoalSelectors = EntityJSEvents.server("addGoalSelectors", () -> AddGoalSelectorsEventJS.class).extra(Extra.REQUIRES_ID);
    public static final EventHandler buildBrain = EntityJSEvents.server("buildBrain", () -> BuildBrainEventJS.class).extra(Extra.REQUIRES_ID);
    public static final EventHandler buildBrainProvider = EntityJSEvents.server("buildBrainProvider", () -> BuildBrainProviderEventJS.class).extra(Extra.REQUIRES_ID);
    //public static final EventHandler biomeSpawns = EntityJSEvents.server("biomeSpawns", () -> BiomeSpawnsEventJS.class);

    public static final EventHandler editAttributes = EntityJSEvents.startup("attributes", () -> ModifyAttributeEventJS.class);
    public static final EventHandler spawnPlacement = EntityJSEvents.startup("spawnPlacement", () -> RegisterSpawnPlacementsEventJS.class);
    public static int customEntities = 0;
    public static boolean modifiedAttributes = false;

    public static void init() {
        RegistryEntryAddedCallback.event(BuiltInRegistries.ENTITY_TYPE).register((rawId, id, entityType) -> {
            for (BaseLivingEntityBuilder<?> builder : BaseLivingEntityBuilder.thisList) {
                if (builder.getBuilderForEntityType(entityType) == builder) {
                    EntityAttributeRegistry.register(builder::get, builder::getAttributeBuilder);
                    customEntities++;
                }
            }
            ConsoleJS.STARTUP.info(customEntities + " out of total: " + BaseLivingEntityBuilder.thisList.size());
            if (customEntities == BaseLivingEntityBuilder.thisList.size() && !modifiedAttributes) {
                attributeModification();
                modifiedAttributes = true;
            }
        });
        registerSpawnPlacements();
    }

    private static void attributeModification() {
        if (editAttributes.hasListeners()) {
            editAttributes.post(new ModifyAttributeEventJS());
        }
    }

    private static void registerSpawnPlacements() {
        for (BaseLivingEntityBuilder<?> builder : BaseLivingEntityBuilder.spawnList) {
            SpawnPlacementsRegistry.register(() -> UtilsJS.cast(builder.get()), builder.placementType, builder.heightMap, UtilsJS.cast(builder.spawnPredicate)); // Cast because the '?' generics makes the event unhappy
        }
        if (spawnPlacement.hasListeners()) {
            spawnPlacement.post(new RegisterSpawnPlacementsEventJS());
        }
    }

    public static void postDataEvent(VirtualKubeJSDataPack pack, MultiPackResourceManager multiManager) {
        if (pack != null && multiManager != null) {
            // Unused
        }
    }
}