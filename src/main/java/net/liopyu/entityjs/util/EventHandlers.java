package net.liopyu.entityjs.util;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.event.EventTargetType;
import dev.latvian.mods.kubejs.event.TargetedEventHandler;
import dev.latvian.mods.kubejs.script.data.VirtualDataPack;
import dev.latvian.mods.kubejs.util.Cast;
import dev.latvian.mods.kubejs.util.UtilsJS;
import net.liopyu.entityjs.builders.living.BaseLivingEntityBuilder;
import net.liopyu.entityjs.events.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

public class EventHandlers {
    public static EventTargetType<ResourceKey<EntityType<?>>> TARGET = EventTargetType.registryKey(Registries.ENTITY_TYPE, EntityType.class);
    public static final EventGroup EntityJSEvents = EventGroup.of("EntityJSEvents");

    public static final TargetedEventHandler<ResourceKey<EntityType<?>>> addGoalTargets = EntityJSEvents.server("addGoals", () -> AddGoalTargetsEventJS.class).requiredTarget(TARGET);
    public static final TargetedEventHandler<ResourceKey<EntityType<?>>> addGoalSelectors = EntityJSEvents.server("addGoalSelectors", () -> AddGoalSelectorsEventJS.class).requiredTarget(TARGET);
    public static final TargetedEventHandler<ResourceKey<EntityType<?>>> buildBrain = EntityJSEvents.server("buildBrain", () -> BuildBrainEventJS.class).requiredTarget(TARGET);
    public static final TargetedEventHandler<ResourceKey<EntityType<?>>> buildBrainProvider = EntityJSEvents.server("buildBrainProvider", () -> BuildBrainProviderEventJS.class).requiredTarget(TARGET);
    public static final EventHandler biomeSpawns = EntityJSEvents.server("biomeSpawns", () -> BiomeSpawnsEventJS.class);

    public static final EventHandler editAttributes = EntityJSEvents.startup("attributes", () -> ModifyAttributeEventJS.class);
    public static final EventHandler spawnPlacement = EntityJSEvents.startup("spawnPlacement", () -> RegisterSpawnPlacementsEventJS.class);
    public static final EventHandler modifyEntity = EntityJSEvents.startup("modifyEntity", () -> EntityModificationEventJS.class);


    public static void init(IEventBus modBus) {
        modBus.addListener(EventHandlers::attributeCreation);
        modBus.addListener(EventHandlers::attributeModification);
        modBus.addListener(EventPriority.LOW, EventHandlers::registerSpawnPlacements); // Low to allow REPLACE to work and addons to effect the result
    }

    private static void attributeCreation(EntityAttributeCreationEvent event) {
        for (BaseLivingEntityBuilder<?> builder : BaseLivingEntityBuilder.thisList) {
            event.put(builder.get(), builder.getAttributeBuilder().build());
        }
    }

    private static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        for (BaseLivingEntityBuilder<?> builder : BaseLivingEntityBuilder.spawnList) {
            event.register(Cast.to(builder.get()), builder.placementType, builder.heightMap, builder.spawnPredicate, RegisterSpawnPlacementsEvent.Operation.REPLACE); // Cast because the '?' generics makes the event unhappy
        }
        if (spawnPlacement.hasListeners()) {
            spawnPlacement.post(new RegisterSpawnPlacementsEventJS(event));
        }
    }

    private static void attributeModification(EntityAttributeModificationEvent event) {
        if (editAttributes.hasListeners()) {
            editAttributes.post(new ModifyAttributeEventJS(event));
        }
    }

    public static void postDataEvent(VirtualDataPack pack, MultiPackResourceManager multiManager) {
        if (pack != null && multiManager != null) {
            // Unused
        }
    }
}
