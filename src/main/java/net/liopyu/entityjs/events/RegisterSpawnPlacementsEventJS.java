package net.liopyu.entityjs.events;

import dev.architectury.registry.level.entity.SpawnPlacementsRegistry;
import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
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
    public <T extends Mob> void replace(EntityType<T> entityType, SpawnPlacements.Type placementType, Heightmap.Types heightmap, net.minecraft.world.entity.SpawnPlacements.SpawnPredicate<T> predicate) {
        // Check if the entity type is not a subtype of Mob
        if (!(Mob.class.isAssignableFrom(entityType.getBaseClass()))) {
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid entity type for spawn placement: " + entityType + ". Must be an instance of Mob.");
            return;
        }

        // Cast entityType to the appropriate type

        // Register the spawn placement using a Supplier lambda
        SpawnPlacementsRegistry.register(() -> entityType, placementType, heightmap, predicate);
    }
}
