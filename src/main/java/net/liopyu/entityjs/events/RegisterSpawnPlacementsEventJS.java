package net.liopyu.entityjs.events;

import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;

public class RegisterSpawnPlacementsEventJS extends EventJS {

    private final SpawnPlacementRegisterEvent event;

    public RegisterSpawnPlacementsEventJS(SpawnPlacementRegisterEvent event) {
        this.event = event;
    }

    @Info(value = "Replaces the given entity type's spawn rules", params = {
            @Param(name = "entityType", value = "The entity type whose spawn placement is being replaced"),
            @Param(name = "placementType", value = "The spawn placement type to use"),
            @Param(name = "heightmap", value = "The heightmap to use"),
            @Param(name = "predicate", value = "The spawn predicate for the entity type's spawning")
    })
    public <T extends Entity> void replace(EntityType<T> entityType, SpawnPlacements.Type placementType, Heightmap.Types heightmap, SpawnPlacements.SpawnPredicate<T> predicate) {
        event.register(entityType, placementType, heightmap, predicate, SpawnPlacementRegisterEvent.Operation.REPLACE);
    }

    @Info(value = "ANDs the given spawn predicate with the existing spawn predicates of the given entity type", params = {
            @Param(name = "entityType", value = "The entity type whose spawn placement is being modified"),
            @Param(name = "predicate", value = "The spawn predicate that will be ANDed with the entity type's existing spawn predicates")
    })
    public <T extends Entity> void and(EntityType<T> entityType, SpawnPlacements.SpawnPredicate<T> predicate) {
        event.register(entityType, predicate, SpawnPlacementRegisterEvent.Operation.AND);
    }

    @Info(value = "ORs the given spawn predicate with the existing spawn predicate of the given entity type", params = {
            @Param(name = "entityType", value = "The entity type whose spawn placement is being modified"),
            @Param(name = "predicate", value = "The spawn predicate that will be ORed with the entity type's existing spawn predicates")
    })
    public <T extends Entity> void or(EntityType<T> entityType, SpawnPlacements.SpawnPredicate<T> predicate) {
        event.register(entityType, predicate, SpawnPlacementRegisterEvent.Operation.OR);
    }
}
