package net.liopyu.entityjs.util;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.event.Extra;
import dev.latvian.mods.kubejs.script.data.VirtualKubeJSDataPack;
import dev.latvian.mods.kubejs.util.UtilsJS;
import net.liopyu.entityjs.builders.BaseLivingEntityBuilder;
import net.liopyu.entityjs.events.*;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class EventHandlers {

    public static final EventGroup EntityJSEvents = EventGroup.of("EntityJSEvents");

    public static final EventHandler addGoalTargets = EntityJSEvents.server("addGoals", () -> AddGoalTargetsEventJS.class).extra(Extra.REQUIRES_ID); // Possibly a modify goals event for editing other entities
    public static final EventHandler addGoalSelectors = EntityJSEvents.server("addGoalSelectors", () -> AddGoalSelectorsEventJS.class).extra(Extra.REQUIRES_ID);
    public static final EventHandler buildBrain = EntityJSEvents.server("buildBrain", () -> BuildBrainEventJS.class).extra(Extra.REQUIRES_ID);
    public static final EventHandler buildBrainProvider = EntityJSEvents.server("buildBrainProvider", () -> BuildBrainProviderEventJS.class).extra(Extra.REQUIRES_ID);
    public static final EventHandler biomeSpawns = EntityJSEvents.server("modifyBiomeSpawns", () -> ModifySpawnsEventJS.class);
    public static final EventHandler editAttributes = EntityJSEvents.startup("attributes", () -> ModifyAttributeEventJS.class);

    public static void init() {

        final IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(EventHandlers::attributeCreation);
        modBus.addListener(EventHandlers::attributeModification);
        modBus.addListener(EventHandlers::registerSpawnPlacements);
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
        // TODO: Do we want an event that also handles this for other entity types with replaceSpawns and mergeSpawns methods
    }

    private static void attributeModification(EntityAttributeModificationEvent event) {
        if (editAttributes.hasListeners()) {
            editAttributes.post(new ModifyAttributeEventJS(event));
        }
    }

    public static void postDataEvent(VirtualKubeJSDataPack pack, MultiPackResourceManager multiManager) {
        if (pack != null && multiManager != null) {
            // Forge's biome modifiers are only read once during server startup, this event will be posted for every resource reload
            biomeSpawns.post(new ModifySpawnsEventJS(pack, multiManager));
            BaseLivingEntityBuilder.spawnsBiomeModifiers.forEach(pack::addData);
        }
    }
}
