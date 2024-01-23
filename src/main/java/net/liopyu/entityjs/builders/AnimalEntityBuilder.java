package net.liopyu.entityjs.builders;

import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.typings.Generics;
import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.entities.IAnimatableJS;
import net.liopyu.entityjs.item.SpawnEggItemBuilder;
import net.liopyu.entityjs.util.EntityTypeRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class AnimalEntityBuilder<T extends AgeableMob & IAnimatableJS> extends MobBuilder<T> {


    public transient ResourceLocation breedOffspringLocation;

    public AnimalEntityBuilder(ResourceLocation i) {
        super(i);
    }

    public AnimalEntityBuilder<T> getBreedOffspring(ResourceLocation breedOffspringLocation) {
        this.breedOffspringLocation = breedOffspringLocation;
        return this;
    }

    public EntityType<? extends AgeableMob> getBreedOffspringType() {
        return EntityTypeRegistry.getEntityType(breedOffspringLocation);
    }
}
