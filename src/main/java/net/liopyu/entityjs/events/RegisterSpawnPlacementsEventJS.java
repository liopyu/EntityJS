package net.liopyu.entityjs.events;

import dev.architectury.registry.level.entity.SpawnPlacementsRegistry;
import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;
import dev.latvian.mods.kubejs.util.UtilsJS;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;

public class RegisterSpawnPlacementsEventJS extends EventJS {


    public RegisterSpawnPlacementsEventJS() {
    }

    @Info(value = "Replaces the given entity type's spawn rules", params = {
            @Param(name = "entityType", value = "The entity type whose spawn placement is being replaced"),
            @Param(name = "placementType", value = "The spawn placement type to use"),
            @Param(name = "heightmap", value = "The heightmap to use"),
            @Param(name = "predicate", value = "The spawn predicate for the entity type's spawning")
    })
    public <T extends Entity> void replace(EntityType<T> entityType, SpawnPlacements.Type placementType, Heightmap.Types heightmap, SpawnPlacements.SpawnPredicate<T> predicate) {
        SpawnPlacementsRegistry.register(() -> UtilsJS.cast(entityType), placementType, heightmap, UtilsJS.cast(predicate));
    }
}
