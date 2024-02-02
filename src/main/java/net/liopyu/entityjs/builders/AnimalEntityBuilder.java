package net.liopyu.entityjs.builders;

import net.liopyu.entityjs.entities.IAnimatableJS;
import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.util.Wrappers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.BlockPathTypes;

import java.util.List;
import java.util.Map;
import java.util.function.*;

public abstract class AnimalEntityBuilder<T extends Animal & IAnimatableJS> extends MobBuilder<T> {


    public transient Object getBreedOffspring;
    public transient Object[] isFood;
    public transient Predicate<ContextUtils.EntityItemStackContext> isFoodPredicate;
    public transient Predicate<LivingEntity> canBreed;
    public transient Function<ContextUtils.EntityBlockPosLevelContext, Float> walkTargetValue;

    public transient double myRidingOffset;
    public transient int ambientSoundInterval;
    public transient Predicate<ContextUtils.EntityDistanceToPlayerContext> removeWhenFarAway;

    public transient Predicate<ContextUtils.EntityAnimalContext> canMate;
    public transient BiConsumer<ServerLevel, Animal> spawnChildFromBreeding;

    @FunctionalInterface
    public interface QuinaryFunction<A, B, C, D, E, R> {
        R apply(A a, B b, C c, D d, E e);
    }

    public transient QuinaryFunction<ServerLevelAccessor, DifficultyInstance, MobSpawnType, SpawnGroupData, CompoundTag, SpawnGroupData> finalizeSpawn;

    public transient Consumer<ContextUtils.PlayerEntityContext> tickLeash;
    public transient BooleanSupplier shouldStayCloseToLeashHolder;
    public transient double followLeashSpeed;
    /*public transient Map<BlockPathTypes, Float> setPathfindingMalus;*/
    /* public transient Function<BlockPathTypes, Boolean> canCutCorner;*/

    public transient Consumer<LivingEntity> setTarget;

    public transient Consumer<LivingEntity> ate;
    public transient Consumer<Object> getAmbientSound;
    public transient List<Object> canHoldItem;
    public transient Boolean shouldDespawnInPeaceful;
    public transient Boolean canPickUpLoot;
    public transient Boolean isPersistenceRequired;
    /*public transient Consumer<ContextUtils.PlayerEntityContext> onOffspringSpawnedFromEgg;*/

/*
    public transient Function<LivingEntity, Double> meleeAttackRangeSqr;
*/


    public AnimalEntityBuilder(ResourceLocation i) {
        super(i);
        canJump = true;
        followLeashSpeed = 1.0D;
        ambientSoundInterval = 120;
        myRidingOffset = 0.14;
    }

    @Info(value = """
            Sets the breedOffspring property in the builder.
                        
            " +
            "Defaults to this entity type.
            """)
    public AnimalEntityBuilder<T> getBreedOffspring(Object breedOffspring) {
        this.getBreedOffspring = breedOffspring;
        return this;
    }

    @Info(value = """
            Sets the canBreed property in the builder.
                        
            " +
            "Defaults to true.
            """)
    public AnimalEntityBuilder<T> canBreed(Predicate<LivingEntity> canBreed) {
        this.canBreed = canBreed;
        return this;
    }

    @Info(value = """
            Sets the list of items that the entity can eat.
                        
            " +
            "Defaults to wheat.
            """)
    public AnimalEntityBuilder<T> isFood(Object... isFood) {
        this.isFood = isFood;
        return this;
    }

    @Info(value = """
            Sets a predicate for what an entity can eat.
                        
            " +
            "Defaults to wheat.
            """)
    public AnimalEntityBuilder<T> isFoodPredicate(Predicate<ContextUtils.EntityItemStackContext> isFoodPredicate) {
        this.isFoodPredicate = isFoodPredicate;
        return this;
    }


    @Info(value = """
            Sets the walkTargetValue property in the builder.
                        
            " +
            "Defaults to null.
            """)
    public AnimalEntityBuilder<T> walkTargetValue(Function<ContextUtils.EntityBlockPosLevelContext, Float> function) {
        this.walkTargetValue = function;
        return this;
    }

    @Info(value = """
            Sets the myRidingOffset property in the builder.
                        
            " +
            "Defaults to 0.0.
            """)
    public AnimalEntityBuilder<T> myRidingOffset(double myRidingOffset) {
        this.myRidingOffset = myRidingOffset;
        return this;
    }

    @Info(value = """
            Sets the ambientSoundInterval property in the builder.
                        
            " +
            "Defaults to 240.
            """)
    public AnimalEntityBuilder<T> ambientSoundInterval(int ambientSoundInterval) {
        this.ambientSoundInterval = ambientSoundInterval;
        return this;
    }

    @Info(value = """
            Sets the removeWhenFarAway property in the builder.
                        
            " +
            "Defaults to null.
            """)
    public AnimalEntityBuilder<T> removeWhenFarAway(Predicate<ContextUtils.EntityDistanceToPlayerContext> removeWhenFarAway) {
        this.removeWhenFarAway = removeWhenFarAway;
        return this;
    }

    @Info(value = """
            Sets the canMate property in the builder.
                        
            " +
            "Defaults to null.
            """)
    public AnimalEntityBuilder<T> canMate(Predicate<ContextUtils.EntityAnimalContext> predicate) {
        this.canMate = predicate;
        return this;
    }


    @Info(value = """
            Sets the spawnChildFromBreeding property in the builder.
                        
            " +
            "Defaults to null.
            """)
    public AnimalEntityBuilder<T> spawnChildFromBreeding(BiConsumer<ServerLevel, Animal> consumer) {
        this.spawnChildFromBreeding = consumer;
        return this;
    }

    @Info(value = """
            Sets the finalizeSpawn property in the builder.
                        
            " +
            "Defaults to null.
            """)
    public AnimalEntityBuilder<T> finalizeSpawn(QuinaryFunction<ServerLevelAccessor, DifficultyInstance, MobSpawnType, SpawnGroupData, CompoundTag, SpawnGroupData> function) {
        this.finalizeSpawn = function;
        return this;
    }

    @Info(value = """
            Sets the tickLeash property in the builder.
                        
            " +
            "Defaults to null.
            """)
    public AnimalEntityBuilder<T> tickLeash(Consumer<ContextUtils.PlayerEntityContext> consumer) {
        this.tickLeash = consumer;
        return this;
    }

    @Info(value = """
            Sets the shouldStayCloseToLeashHolder property in the builder.
                        
            " +
            "Defaults to true for animals.
            """)
    public AnimalEntityBuilder<T> shouldStayCloseToLeashHolder(BooleanSupplier b) {
        this.shouldStayCloseToLeashHolder = b;
        return this;
    }

    @Info(value = """
            Sets the followLeashSpeed property in the builder.
                        
            " +
            "Defaults to null.
            """)
    public AnimalEntityBuilder<T> followLeashSpeed(double supplier) {
        this.followLeashSpeed = supplier;
        return this;
    }


    /*@Info(value = """
            Sets the pathfinding malus for a specific node type.
                        
            " +
            "Defaults to null.
            """)
    public AnimalEntityBuilder<T> setPathfindingMalus(Map<BlockPathTypes, Float> setPathfindingMalus) {
        this.setPathfindingMalus = setPathfindingMalus;
        return this;
    }*/

    /*@Info(value = """
            Determines if the entity can cut corners for a specific path type.
                        
            " +
            "Defaults to null.
            """)
    public AnimalEntityBuilder<T> canCutCorner(Function<BlockPathTypes, Boolean> canCutCorner) {
        this.canCutCorner = canCutCorner;
        return this;
    }*/


    @Info(value = """
            Sets the target for the entity.
                        
            " +
            "Defaults to null.
            """)
    public AnimalEntityBuilder<T> setTarget(Consumer<LivingEntity> setTarget) {
        this.setTarget = setTarget;
        return this;
    }


    @Info(value = """
            Custom behavior when the entity eats.
                        
            " +
            "Defaults to null.
            """)
    public AnimalEntityBuilder<T> ate(Consumer<LivingEntity> ate) {
        this.ate = ate;
        return this;
    }


    @Info(value = """
            Sets the ambient sound for the entity.
                        
            " +
            "Defaults to null.
            """)
    public AnimalEntityBuilder<T> getAmbientSound(Consumer<Object> getAmbientSound) {
        this.getAmbientSound = getAmbientSound;
        return this;
    }


    @Info(value = """
            Sets the condition for whether the entity can hold specific items.
                        
            " +
            "Defaults to null.
            """)
    public AnimalEntityBuilder<T> canHoldItem(List<Object> items) {
        this.canHoldItem = items;
        return this;
    }


    @Info(value = """
            Sets whether the entity should despawn in peaceful mode.
                        
            " +
            "Defaults to null.
            """)
    public AnimalEntityBuilder<T> shouldDespawnInPeaceful(Boolean shouldDespawnInPeaceful) {
        this.shouldDespawnInPeaceful = shouldDespawnInPeaceful;
        return this;
    }


    @Info(value = """
            Sets whether the entity can pick up loot.
                        
            " +
            "Defaults to null.
            """)
    public AnimalEntityBuilder<T> canPickUpLoot(Boolean canPickUpLoot) {
        this.canPickUpLoot = canPickUpLoot;
        return this;
    }

    @Info(value = """
            Sets whether the entity's persistence is required.
                        
            " +
            "Defaults to null.
            """)
    public AnimalEntityBuilder<T> isPersistenceRequired(Boolean isPersistenceRequired) {
        this.isPersistenceRequired = isPersistenceRequired;
        return this;
    }

   /* @Info(value = """
            Sets the behavior when offspring is spawned from an egg.
                        
            " +
            "Defaults to null.
            """)
    public AnimalEntityBuilder<T> onOffspringSpawnedFromEgg(Consumer<ContextUtils.PlayerEntityContext> onOffspringSpawnedFromEgg) {
        this.onOffspringSpawnedFromEgg = onOffspringSpawnedFromEgg;
        return this;
    }*/


   /* @Info(value = """
            Sets the square of the melee attack range for the entity.
                        
            " +
            "Defaults to null.
            """)
    public AnimalEntityBuilder<T> meleeAttackRangeSqr(Function<LivingEntity, Double> meleeAttackRangeSqr) {
        this.meleeAttackRangeSqr = meleeAttackRangeSqr;
        return this;
    }*/


}
