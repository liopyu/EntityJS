package net.liopyu.entityjs.util;

import com.mojang.logging.LogUtils;
import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.event.EventTargetType;
import dev.latvian.mods.kubejs.event.TargetedEventHandler;
import dev.latvian.mods.kubejs.script.data.VirtualDataPack;
import dev.latvian.mods.kubejs.util.Cast;
import dev.latvian.mods.kubejs.util.UtilsJS;
import net.liopyu.entityjs.builders.living.BaseLivingEntityBuilder;
import net.liopyu.entityjs.builders.misc.CustomEntityJSBuilder;
import net.liopyu.entityjs.builders.nonliving.BaseEntityBuilder;
import net.liopyu.entityjs.builders.nonliving.BaseNonAnimatableEntityBuilder;
import net.liopyu.entityjs.builders.nonliving.entityjs.ArrowEntityJSBuilder;
import net.liopyu.entityjs.builders.nonliving.entityjs.ProjectileAnimatableJSBuilder;
import net.liopyu.entityjs.builders.nonliving.entityjs.ProjectileEntityBuilder;
import net.liopyu.entityjs.builders.nonliving.entityjs.ProjectileEntityJSBuilder;
import net.liopyu.entityjs.builders.nonliving.vanilla.TridentJSBuilder;
import net.liopyu.entityjs.entities.nonliving.entityjs.ArrowEntityJS;
import net.liopyu.entityjs.entities.nonliving.entityjs.ProjectileAnimatableJS;
import net.liopyu.entityjs.entities.nonliving.entityjs.ProjectileEntityJS;
import net.liopyu.entityjs.entities.nonliving.vanilla.TridentEntityJS;
import net.liopyu.entityjs.events.*;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.ProjectileDispenseBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import static net.liopyu.entityjs.events.EntityModificationEventJS.getOrCreate;

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
        modBus.addListener(EventHandlers::registerDispenserBehavior);
        modBus.addListener(EventHandlers::attributeCreation);
        modBus.addListener(EventHandlers::attributeModification);
        modBus.addListener(EventPriority.LOW, EventHandlers::registerSpawnPlacements); // Low to allow REPLACE to work and addons to effect the result
    }

    private static void registerDispenserBehavior(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            for (BaseNonAnimatableEntityBuilder<?> b : BaseNonAnimatableEntityBuilder.thisList) {
                if (b instanceof ArrowEntityJSBuilder builder) {
                    if (!builder.noItem && builder.canShootFromDispenser) {
                        var item = BuiltInRegistries.ITEM.get(builder.item.id);
                        DispenserBlock.registerProjectileBehavior(item);
                    }
                }
            }
            for (ProjectileEntityBuilder<?> p : ProjectileEntityBuilder.thisList) {
                var b = (ProjectileEntityJSBuilder) p;
                if (!b.noItem && b.canShootFromDispenser) {
                    var item = BuiltInRegistries.ITEM.get(b.item.id);
                    DispenserBlock.registerProjectileBehavior(item);
                }
            }
            for (BaseEntityBuilder<?> b : BaseEntityBuilder.thisList) {
                if (b instanceof TridentJSBuilder builder) {
                    if (!builder.noItem && builder.canShootFromDispenser) {
                        var item = BuiltInRegistries.ITEM.get(builder.item.id);
                        DispenserBlock.registerProjectileBehavior(item);
                    }
                } else if (b instanceof ProjectileAnimatableJSBuilder builder) {
                    if (!builder.noItem && builder.canShootFromDispenser) {
                        var item = BuiltInRegistries.ITEM.get(builder.item.id);
                        DispenserBlock.registerProjectileBehavior(item);
                    }
                }
            }
        });
    }

    private static void attributeCreation(EntityAttributeCreationEvent event) {
        for (BaseLivingEntityBuilder<?> builder : BaseLivingEntityBuilder.thisList) {
            event.put(builder.get(), builder.getAttributeBuilder().build());
        }
        for (CustomEntityJSBuilder builder : CustomEntityJSBuilder.thisList) {
            event.put((EntityType<? extends LivingEntity>) builder.get(), builder.getAttributeBuilder().build());
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
       /* BuiltInRegistries.ENTITY_TYPE.forEach(entityType -> {
            if (EventHandlers.modifyEntity.hasListeners()) {
                var eventJS = getOrCreate(entityType, entityType.getBaseClass());
                EventHandlers.modifyEntity.post(eventJS);
                LogUtils.getLogger().info("[EntityJS] Captured builder inline in startup init: " + eventJS.getBuilder());
            }
        });*/

    }

    public static void postDataEvent(VirtualDataPack pack, MultiPackResourceManager multiManager) {
        if (pack != null && multiManager != null) {
            // Unused
        }
    }
}
