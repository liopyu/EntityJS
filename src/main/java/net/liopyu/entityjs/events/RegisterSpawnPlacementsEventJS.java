package net.liopyu.entityjs.events;

import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

public class RegisterSpawnPlacementsEventJS implements KubeEvent {

    private final RegisterSpawnPlacementsEvent event;

    public RegisterSpawnPlacementsEventJS(RegisterSpawnPlacementsEvent event) {
        this.event = event;
    }

    @Info(value = "Replaces the given entity type's spawn rules", params = {
            @Param(name = "entityType", value = "The entity type whose spawn placement is being replaced"),
            @Param(name = "placementType", value = "The spawn placement type to use"),
            @Param(name = "heightmap", value = "The heightmap to use"),
            @Param(name = "predicate", value = "The spawn predicate for the entity type's spawning")
    })
    public <T extends Entity> void replace(EntityType<T> entityType, EntityJSHelperClass.SpawnPlacementTypeEnum placementType, Heightmap.Types heightmap, SpawnPlacements.SpawnPredicate<T> predicate) {
        event.register(entityType, EntityJSHelperClass.getSpawnPlacementType(placementType), heightmap, predicate, RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    @Info(value = "ANDs the given spawn predicate with the existing spawn predicates of the given entity type", params = {
            @Param(name = "entityType", value = "The entity type whose spawn placement is being modified"),
            @Param(name = "predicate", value = "The spawn predicate that will be ANDed with the entity type's existing spawn predicates")
    })
    public <T extends Entity> void and(EntityType<T> entityType, SpawnPlacements.SpawnPredicate<T> predicate) {
        event.register(entityType, predicate, RegisterSpawnPlacementsEvent.Operation.AND);
    }

    @Info(value = "ORs the given spawn predicate with the existing spawn predicate of the given entity type", params = {
            @Param(name = "entityType", value = "The entity type whose spawn placement is being modified"),
            @Param(name = "predicate", value = "The spawn predicate that will be ORed with the entity type's existing spawn predicates")
    })
    public <T extends Entity> void or(EntityType<T> entityType, SpawnPlacements.SpawnPredicate<T> predicate) {
        event.register(entityType, predicate, RegisterSpawnPlacementsEvent.Operation.OR);
    }
}
