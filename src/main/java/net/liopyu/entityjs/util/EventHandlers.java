package net.liopyu.entityjs.util;

import dev.architectury.registry.level.entity.EntityAttributeRegistry;
import dev.architectury.registry.level.entity.SpawnPlacementsRegistry;
import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.event.Extra;
import dev.latvian.mods.kubejs.script.data.VirtualKubeJSDataPack;
import dev.latvian.mods.kubejs.util.UtilsJS;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.mixin.object.builder.DefaultAttributeRegistryAccessor;
import net.liopyu.entityjs.builders.living.BaseLivingEntityBuilder;
import net.liopyu.entityjs.events.*;
import net.minecraft.core.Registry;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

public class EventHandlers {

    public static final EventGroup EntityJSEvents = EventGroup.of("EntityJSEvents");

    public static final EventHandler addGoalTargets = EntityJSEvents.server("addGoals", () -> AddGoalTargetsEventJS.class).extra(Extra.REQUIRES_ID); // Possibly a modify goals event for editing other entities
    public static final EventHandler addGoalSelectors = EntityJSEvents.server("addGoalSelectors", () -> AddGoalSelectorsEventJS.class).extra(Extra.REQUIRES_ID);
    public static final EventHandler buildBrain = EntityJSEvents.server("buildBrain", () -> BuildBrainEventJS.class).extra(Extra.REQUIRES_ID);
    public static final EventHandler buildBrainProvider = EntityJSEvents.server("buildBrainProvider", () -> BuildBrainProviderEventJS.class).extra(Extra.REQUIRES_ID);
    //public static final EventHandler biomeSpawns = EntityJSEvents.server("biomeSpawns", () -> BiomeSpawnsEventJS.class);

    public static final EventHandler editAttributes = EntityJSEvents.startup("attributes", () -> ModifyAttributeEventJS.class);
    public static final EventHandler spawnPlacement = EntityJSEvents.startup("spawnPlacement", () -> RegisterSpawnPlacementsEventJS.class);
    public static boolean registeredAttributes = false;

    public static void init() {
        RegistryEntryAddedCallback.event(Registry.ENTITY_TYPE).register((rawId, id, entityType) -> {
            if (!registeredAttributes) {
                attributeModification();
                registeredAttributes = true;
            }
        });
        registerSpawnPlacements();
    }

    public static void attributeCreation() {
        for (BaseLivingEntityBuilder<?> builder : BaseLivingEntityBuilder.thisList) {
            FabricDefaultAttributeRegistry.register(builder.get(), builder.getAttributeBuilder());
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

    private static void attributeModification() {
        if (editAttributes.hasListeners()) {
            editAttributes.post(new ModifyAttributeEventJS());
        }
    }

    public static void postDataEvent(VirtualKubeJSDataPack pack, MultiPackResourceManager multiManager) {
        if (pack != null && multiManager != null) {
            // Unused
        }
    }
}
