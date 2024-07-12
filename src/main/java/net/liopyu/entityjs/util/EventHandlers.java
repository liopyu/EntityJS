package net.liopyu.entityjs.util;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.event.EventTargetType;
import dev.latvian.mods.kubejs.util.UtilsJS;
import net.liopyu.entityjs.builders.living.BaseLivingEntityBuilder;
import net.liopyu.entityjs.builders.living.modification.ModifyLivingEntityBuilder;
import net.liopyu.entityjs.builders.living.modification.TestModifyEntityBuilder;
import net.liopyu.entityjs.events.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;

public class EventHandlers {
    public static EventTargetType<ResourceKey<EntityType<?>>> TARGET = EventTargetType.registryKey(Registries.ENTITY_TYPE, EntityType.class);
    public static final EventGroup EntityJSEvents = EventGroup.of("EntityJSEvents");

    public static final EventHandler addGoalTargets = EntityJSEvents.server("addGoals", () -> AddGoalTargetsEventJS.class).requiredTarget(TARGET);
    public static final EventHandler addGoalSelectors = EntityJSEvents.server("addGoalSelectors", () -> AddGoalSelectorsEventJS.class).requiredTarget(TARGET);
    public static final EventHandler buildBrain = EntityJSEvents.server("buildBrain", () -> BuildBrainEventJS.class).requiredTarget(TARGET);
    public static final EventHandler buildBrainProvider = EntityJSEvents.server("buildBrainProvider", () -> BuildBrainProviderEventJS.class).requiredTarget(TARGET);
    public static final EventHandler biomeSpawns = EntityJSEvents.server("biomeSpawns", () -> BiomeSpawnsEventJS.class);

    public static final EventHandler editAttributes = EntityJSEvents.startup("attributes", () -> ModifyAttributeEventJS.class);
    public static final EventHandler spawnPlacement = EntityJSEvents.startup("spawnPlacement", () -> RegisterSpawnPlacementsEventJS.class);
    public static final EventHandler modifyEntity = EntityJSEvents.startup("modifyEntity", () -> EntityModificationEventJS.class);


    public static void init() {

        final IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(EventHandlers::attributeCreation);
        modBus.addListener(EventHandlers::attributeModification);
        modBus.addListener(EventPriority.LOW, EventHandlers::registerSpawnPlacements); // Low to allow REPLACE to work and addons to effect the result
    }

    private static void attributeCreation(EntityAttributeCreationEvent event) {
        for (BaseLivingEntityBuilder<?> builder : BaseLivingEntityBuilder.thisList) {
            event.put(builder.get(), builder.getAttributeBuilder().build());
        }
    }

    private static void registerSpawnPlacements(SpawnPlacementRegisterEvent event) {
        for (BaseLivingEntityBuilder<?> builder : BaseLivingEntityBuilder.spawnList) {
            event.register(UtilsJS.cast(builder.get()), builder.placementType, builder.heightMap, builder.spawnPredicate, SpawnPlacementRegisterEvent.Operation.REPLACE); // Cast because the '?' generics makes the event unhappy
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

    public static void postDataEvent(VirtualKubeJSDataPack pack, MultiPackResourceManager multiManager) {
        if (pack != null && multiManager != null) {
            // Unused
        }
    }
}
